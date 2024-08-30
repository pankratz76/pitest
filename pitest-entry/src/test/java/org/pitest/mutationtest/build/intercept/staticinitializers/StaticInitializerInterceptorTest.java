package org.pitest.mutationtest.build.intercept.staticinitializers;

import com.example.staticinitializers.BrokenChain;
import com.example.staticinitializers.delayedexecution.CustomFunction;
import com.example.staticinitializers.delayedexecution.CustomFunctionNotAnnotated;
import com.example.staticinitializers.delayedexecution.EnumFieldMethodRef;
import com.example.staticinitializers.delayedexecution.EnumFieldSupplier;
import com.example.staticinitializers.EnumWithLambdaInConstructor;
import com.example.staticinitializers.MethodsCallsEachOtherInLoop;
import com.example.staticinitializers.NestedEnumWithLambdaInStaticInitializer;
import com.example.staticinitializers.SecondLevelPrivateMethods;
import com.example.staticinitializers.SingletonWithWorkInInitializer;
import com.example.staticinitializers.delayedexecution.EnumListOfSuppliers;
import com.example.staticinitializers.delayedexecution.EnumMethodReferenceNotStored;
import com.example.staticinitializers.delayedexecution.EnumMixedFields;
import com.example.staticinitializers.delayedexecution.StaticFunctionField;
import com.example.staticinitializers.delayedexecution.StaticListOfFunctionalInterface;
import com.example.staticinitializers.delayedexecution.StaticListOfFunctions;
import com.example.staticinitializers.delayedexecution.StaticListOfUnannotatedInterfaces;
import com.example.staticinitializers.delayedexecution.StaticSupplierField;
import com.example.staticinitializers.ThirdLevelPrivateMethods;
import org.junit.Ignore;
import org.junit.Test;
import org.pitest.mutationtest.FixedCodeSource;
import org.pitest.mutationtest.engine.MutationDetails;
import org.pitest.mutationtest.engine.gregor.mutators.NullMutateEverything;
import org.pitest.verifier.interceptors.InterceptorVerifier;
import org.pitest.verifier.interceptors.VerifierStart;

import java.util.function.Predicate;

public class StaticInitializerInterceptorTest {

    InterceptorVerifier v = VerifierStart.forInterceptorFactory(new StaticInitializerInterceptorFactory()
                    , new FixedCodeSource(CustomFunction.class, CustomFunctionNotAnnotated.class))
            .usingMutator(new NullMutateEverything());


    @Test
    public void doesNotFilterMutationsInClassWithoutStaticInitializer() {
        v.forClass(NoStaticInitializer.class)
                .forAnyCode()
                .mutantsAreGenerated()
                .noMutantsAreFiltered()
                .verify();
    }

    @Test
    public void filtersMutationsInStaticInitializer() {
      v.forClass(HasStaticInitializer.class)
              .forMethod("<clinit>")
              .forAnyCode()
              .mutantsAreGenerated()
              .allMutantsAreFiltered()
              .verify();
    }

    @Test
    public void filtersMutationsInPrivateMethodsCalledFromStaticInitializer() {
      v.forClass(HasPrivateCallsFromStaticInializer.class)
              .forMethod("a")
              .forAnyCode()
              .mutantsAreGenerated()
              .allMutantsAreFiltered()
              .verify();
    }

    @Test
    public void doesNotFilterMutationsInPackageDefaultMethodsCalledFromStaticInitializer() {
      v.forClass(HasDefaultCallsFromStaticInitializer.class)
              .forMethod("a")
              .forAnyCode()
              .mutantsAreGenerated()
              .noMutantsAreFiltered()
              .verify();
    }

    @Test
    public void doesNotFilterMutationsInPrivateStaticMethodsNotInvolvedInInit() {
      v.forClass(HasOtherPrivateStaticMethods.class)
              .forMethod("b")
              .forAnyCode()
              .mutantsAreGenerated()
              .noMutantsAreFiltered()
              .verify();
    }

    @Test
    public void doesNotFilterMutationsInOverriddenMethodsNotInvolvedInStaticInit() {
      v.forClass(HasOverloadedMethodsThatAreNotUsedInStaticInitialization.class)
              .forMutantsMatching(inMethod("a", "(I)V"))
              .mutantsAreGenerated()
              .noMutantsAreFiltered()
              .verify();
    }

    @Test
    public void filtersMutantsInSingletonConstructor() {
        v.forClass(SingletonWithWorkInInitializer.class)
                .forMutantsMatching(inMethod("<init>", "()V"))
                .mutantsAreGenerated()
                .allMutantsAreFiltered()
                .verify();
    }

    @Test
    public void mutatesPrivateMethodsCalledFromPublicMethodInSingleton() {
        v.forClass(SingletonWithWorkInInitializer.class)
                .forMutantsMatching(inMethod("mutateMeCalledFromPublicMethod", "()V"))
                .mutantsAreGenerated()
                .noMutantsAreFiltered()
                .verify();
    }



    @Test
    public void filtersMutantsCalledFromPrivateSingletonConstructor() {
        v.forClass(SingletonWithWorkInInitializer.class)
                .forMutantsMatching(inMethodStartingWith("doNotMutate"))
                .mutantsAreGenerated()
                .allMutantsAreFiltered()
                .verify();
    }

    @Test
    public void filtersPrivateMethodsCalledIndirectly() {
        v.forClass(SecondLevelPrivateMethods.class)
                .forMutantsMatching(inMethodStartingWith("dontMutate"))
                .mutantsAreGenerated()
                .allMutantsAreFiltered()
                .verify();
    }

    @Test
    public void filtersPrivateMethodsCalledIndirectlyInLongChain() {
        v.forClass(ThirdLevelPrivateMethods.class)
                .forMutantsMatching(inMethodStartingWith("dontMutate"))
                .mutantsAreGenerated()
                .allMutantsAreFiltered()
                .verify();
    }

    @Test
    public void doesNotFilterPrivateMethodsWhenChainBrokenByPublicMethod() {
        v.forClass(BrokenChain.class)
                .forMutantsMatching(inMethodStartingWith("mutateMe"))
                .mutantsAreGenerated()
                .noMutantsAreFiltered()
                .verify();
    }

    @Test
    public void analysisDoesNotGetStuckInInfiniteLoop() {
        v.forClass(MethodsCallsEachOtherInLoop.class)
                .forMutantsMatching(inMethodStartingWith("a"))
                .mutantsAreGenerated()
                .allMutantsAreFiltered()
                .verify();
    }

    @Test
    public void filtersMutantsInEnumPrivateMethodsCalledViaMethodRef() {
        v.forClass(EnumWithLambdaInConstructor.class)
                .forMutantsMatching(inMethodStartingWith("doStuff"))
                .mutantsAreGenerated()
                .allMutantsAreFiltered()
                .verify();
    }

    @Test
    public void filtersMutantsInLambdaCalledFromStaticInitializerInNestedEnum() {
        v.forClass(NestedEnumWithLambdaInStaticInitializer.TOYS.class)
                .forMutantsMatching(inMethodStartingWith("lambda"))
                .mutantsAreGenerated()
                .allMutantsAreFiltered()
                .verify();
    }

    @Test
    public void doesNotSuppressDownStreamMutationsForCodeStoredInSuppliers() {
        v.forClass(StaticSupplierField.class)
                .forMethod("canMutate")
                .forAnyCode()
                .mutantsAreGenerated()
                .noMutantsAreFiltered()
                .verify();
    }

    @Test
    public void doesNotSuppressDownStreamMutationsForCodeStoredInFunctions() {
        v.forClass(StaticFunctionField.class)
                .forMethod("canMutate")
                .forAnyCode()
                .mutantsAreGenerated()
                .noMutantsAreFiltered()
                .verify();
    }

    @Test
    public void doesNotSuppressDownStreamMutationsForEnumFieldSuppliers() {
        v.forClass(EnumFieldSupplier.class)
                .forMethod("canMutate")
                .forAnyCode()
                .mutantsAreGenerated()
                .noMutantsAreFiltered()
                .verify();
    }


    @Test
    public void doesNotSuppressDownStreamMutationsForMethodRefsStoredToEnumFields() {
        v.forClass(EnumFieldMethodRef.class)
                .forMethod("canMutate")
                .forAnyCode()
                .mutantsAreGenerated()
                .noMutantsAreFiltered()
                .verify();
    }

    @Test
    public void doesNotSuppressDownStreamMutationsForMethodRefsStoredToEnumFieldsWhenOtherFieldsInitialized() {
        v.forClass(EnumMixedFields.class)
                .forMethod("canMutate")
                .forAnyCode()
                .mutantsAreGenerated()
                .noMutantsAreFiltered()
                .verify();
    }

    @Test
    public void suppressesMutationsForStringsStoredToEnumFields() {
        v.forClass(EnumMixedFields.class)
                .forMethod("doNotMutate")
                .forAnyCode()
                .mutantsAreGenerated()
                .allMutantsAreFiltered()
                .verify();
    }

    @Test
    public void filtersMutationsForMethodReferencesUsedInEnumConstructor() {
        v.forClass(EnumMethodReferenceNotStored.class)
                .forMethod("doNotMutate")
                .forAnyCode()
                .mutantsAreGenerated()
                .allMutantsAreFiltered()
                .verify();
    }

    @Test
    public void mutatesMethodsStoredInListOfSuppliers() {
        v.forClass(EnumListOfSuppliers.class)
                .forMethod("canMutate")
                .forAnyCode()
                .mutantsAreGenerated()
                .noMutantsAreFiltered()
                .verify();
    }

    @Test
    public void mutatesMethodsStoredInStaticListOfFunctions() {
        v.forClass(StaticListOfFunctions.class)
                .forMethod("canMutate")
                .forAnyCode()
                .mutantsAreGenerated()
                .noMutantsAreFiltered()
                .verify();
    }

    @Test
    @Ignore("functionality disabled, may get re-introduced")
    public void recognisesCustomFunctionalInterfaces() {
        v.forClass(StaticListOfFunctionalInterface.class)
                .forMethod("canMutate")
                .forAnyCode()
                .mutantsAreGenerated()
                .noMutantsAreFiltered()
                .verify();
    }

    @Test
    public void stillMutatesIfClassUsedAsFunctionIsntAnnotated() {
        v.forClass(StaticListOfUnannotatedInterfaces.class)
                .forMethod("canMutate")
                .forAnyCode()
                .mutantsAreGenerated()
                .allMutantsAreFiltered()
                .verify();
    }


    private Predicate<MutationDetails> inMethod(String name, String desc) {
        return m -> m.getMethod().equals(name) && m.getId().getLocation().getMethodDesc().equals(desc);
    }

    private Predicate<MutationDetails> inMethodStartingWith(String stub) {
        return m -> m.getMethod().startsWith(stub);
    }


}

class NoStaticInitializer {
    {
        System.out.println("NOT static code");
    }
}

class HasStaticInitializer {
    static {
        System.out.println("static code");
    }
}

class HasPrivateCallsFromStaticInializer {
    static {
        a();
    }

    private static void a() {
        System.out.println("static code");
    }
}

class HasDefaultCallsFromStaticInitializer {
    static {
        a();
    }

    static void a() {
        System.out.println("NOT guaranteed to be static code");
    }
}

class HasOtherPrivateStaticMethods {
    static {
        a();
    }

    private static void a() {

    }

    public static void entryPoint(int i) {
        b(i);
    }


    private static void b(int i) {
        System.out.println("NOT static code");
    }
}


class HasOverloadedMethodsThatAreNotUsedInStaticInitialization {
    static {
        a();
    }

    private static void a() {

    }

    public static void entryPoint(int i) {
        a(i);
    }

    // same name, different sig
    private static void a(int i) {
        System.out.println("NOT static code");
    }
}


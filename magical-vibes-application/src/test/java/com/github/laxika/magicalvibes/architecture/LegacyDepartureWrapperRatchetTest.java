package com.github.laxika.magicalvibes.architecture;

import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.classfile.ClassFile;
import java.lang.classfile.ClassModel;
import java.lang.classfile.MethodModel;
import java.lang.classfile.instruction.InvokeInstruction;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Freezes the callers of the legacy single-permanent removal wrappers of {@link PermanentRemovalService}. Those
 * wrappers perform a one-permanent departure batch, but their callers can't be paused, so when a removal needs an
 * answer (CR 616.1) outside a window that can stop and run the caller again, it falls back to asking after the event
 * ({@code LegacyDepartureFallback}). New code must build its {@code DepartureBatch} and call
 * {@code DepartureService.depart} instead. When a caller is moved off the wrappers, remove it from
 * {@link #ALLOWED_CALLERS}; never add one.
 *
 * <p>Reads the compiled main classes of the project's modules (card modules excepted: card classes hold no engine
 * logic) with the JDK class-file API, since ArchUnit can't read this JDK's class files.
 */
class LegacyDepartureWrapperRatchetTest {

    private static final String REMOVAL_SERVICE = PermanentRemovalService.class.getName().replace('.', '/');

    private static final Set<String> WRAPPERS = Set.of(
            "removePermanentToGraveyard",
            "sacrificePermanentToGraveyard",
            "destroyPermanentToGraveyard",
            "tryDestroyPermanent",
            "removePermanentToPlayerGraveyard");

    /** The public overloads of {@link #WRAPPERS}, as name plus method descriptor. */
    private static final Set<String> WRAPPER_SIGNATURES = Arrays.stream(PermanentRemovalService.class.getDeclaredMethods())
            .filter(method -> Modifier.isPublic(method.getModifiers()) && WRAPPERS.contains(method.getName()))
            .map(LegacyDepartureWrapperRatchetTest::signature)
            .collect(Collectors.toSet());

    private static final Pattern LAMBDA = Pattern.compile("lambda\\$(.+)\\$\\d+");

    /** The current callers, as {@code OutermostClass.method} (a lambda counts as the method declaring it). */
    private static final Set<String> ALLOWED_CALLERS = Set.of(
            "AbilityActivationService.sacrificePermanent",
            "AbilityActivationService.sacrificePermanentAsCost",
            "ActivatedAbilityExecutionService.completeActivationAfterCosts",
            "ActivatedAbilityExecutionService.doResolveManaAbility",
            "ActivatedAbilityExecutionService.sacrificeCostPermanents",
            "AttackSacrificeCostService.sacrificeMatching",
            "BattlefieldPlacementService.applySacrificeOtherPermanentsWithSameName",
            "CardChoiceHandlerService.resolveUntargetedCardChoice",
            "DestroyCombatOpponentsOfTargetAtEndOfCombatEffectHandler.destroyCombatOpponents",
            "DestructionSupport.sacrificeAndLog",
            "GraveyardService.performSacrificeAuraAndGrantIndestructibleReplacement",
            "GraveyardService.performUmbraArmorReplacement",
            "LifeSupport.replaceLifeLossWithShield",
            "MayPenaltyChoiceHandlerService.handleSacrificeUnlessDiscardChoice",
            "MayPenaltyChoiceHandlerService.handleSacrificeUnlessReturnOwnPermanentChoice",
            "MayPenaltyChoiceHandlerService.handleSacrificeUnlessReturnPermanentChoice",
            "MayPenaltyChoiceHandlerService.handleSacrificeUnlessSacrificeOwnPermanentChoice",
            "MultiPermanentChoiceHandlerService.handleDestroyPermanentDefendingPlayerControlsAndAssignNoCombatDamage",
            "MultiPermanentChoiceHandlerService.handleSacrificeAttackCost",
            "MultiPermanentChoiceHandlerService.handleSacrificeDamagedPlayerControlsPermanent",
            "MultiPermanentChoiceHandlerService.handleSacrificeSelfToDestroy",
            "PermanentChoiceBattlefieldHandlerService.handleChampionDeclined",
            "PermanentChoiceBattlefieldHandlerService.handleExploitSacrifice",
            "PermanentChoiceBattlefieldHandlerService.handleLandCasualty",
            "PermanentChoiceBattlefieldHandlerService.handleLegendRule",
            "PermanentChoiceBattlefieldHandlerService.handleReattachSourceAuraAfterSacrifice",
            "PermanentChoiceBattlefieldHandlerService.handleSacrificeAnotherCreatureDealPowerDamage",
            "PermanentChoiceBattlefieldHandlerService.handleSacrificeAnotherCreatureGainLifeAndDraw",
            "PermanentChoiceBattlefieldHandlerService.handleSacrificeArtifactForDividedDamage",
            "PermanentChoiceBattlefieldHandlerService.handleSacrificeCreature",
            "PermanentChoiceBattlefieldHandlerService.handleSacrificeCreatureCreateSizedTokenEqualToPower",
            "PermanentChoiceBattlefieldHandlerService.handleSacrificeCreatureCreateTokensEqualToToughness",
            "PermanentChoiceBattlefieldHandlerService.handleSacrificeCreatureThenSearchLibrary",
            "PermanentChoiceBattlefieldHandlerService.handleSacrificeOwnPermanentOrSacrificeSelf",
            "PermanentChoiceBattlefieldHandlerService.handleSacrificePermanentAndBoostSelf",
            "PermanentChoiceBattlefieldHandlerService.handleSacrificePermanentAndGrantKeywordSelf",
            "PermanentChoiceBattlefieldHandlerService.handleSacrificePermanentThen",
            "PermanentChoiceBattlefieldHandlerService.handleSacrificePermanentToEnter",
            "PermanentRemovalService.destroyPermanentInEnclosingBatch",
            "PermanentRemovalService.resolveDelayedPermanentAction",
            "RemoveLinkedPermanentEffectHandler.resolve",
            "SacrificeCreatureSearchLibraryForCreatureToHandEffectHandler.doResolve",
            "SacrificeCreatureThenMassDamageEqualToPowerEffectHandler.resolveAfterChoice",
            "SacrificePermanentAndReturnTargetCardsFromGraveyardEffectHandler.resolveAfterChoice",
            "SacrificeTargetPermanentAtEndStepAndGainLifeEqualToToughnessEffectHandler.sacrificeAndGainLife",
            "SpellCastingService.computeAlternateCastingManaPayment",
            "SpellCastingService.payFlashbackPermanentSacrificeCosts",
            "SpellCastingService.payGraveyardCastAdditionalCosts",
            "SpellCastingService.payGraveyardCastPermanentSacrificeCosts",
            "SpellCastingService.paySacrificeAllCreaturesYouControlCost",
            "SpellCastingService.paySacrificeAllPermanentsYouControlCost",
            "SpellCastingService.paySacrificeCreaturesForCostReduction",
            "SpellCastingService.paySingleSacrificeCost",
            "StateBasedActionService.applyWorldRule",
            "StateBasedActionService.removeRedundantRoles",
            "StateBasedActionService.sacrificeCompletedSagas",
            "TargetPlayerSacrificesCreatureThenCreateTokensIfSubtypeEffectHandler.sacrificeAndMaybeCreateTokens",
            "TargetPlayerSacrificesRandomPermanentsUntilLandEffectHandler.resolve",
            "TempestEfreetAnteExchangeEffectHandler.moveSourceToOpponentsGraveyard",
            "TimmerianFiendsAnteExchangeEffectHandler.moveCardToGraveyard",
            "TimmerianFiendsAnteExchangeEffectHandler.performExchange",
            "TurnCleanupService.sacrificePermanentsFlaggedForCleanup");

    @Test
    @DisplayName("Only the frozen callers use the legacy removal wrappers")
    void onlyFrozenCallersUseTheLegacyRemovalWrappers() {
        List<Path> mainOutputs = Arrays.stream(System.getProperty("java.class.path").split(File.pathSeparator))
                .map(Path::of)
                .filter(LegacyDepartureWrapperRatchetTest::isProjectMainOutput)
                .toList();
        Set<String> callers = new TreeSet<>();
        mainOutputs.forEach(output -> classFiles(output).forEach(bytes -> collectCallers(bytes, callers)));

        assertThat(callers)
                .as("the engine's main classes are scanned")
                .anyMatch(caller -> caller.startsWith("DestructionSupport."));
        assertThat(callers)
                .withFailMessage(() -> "The callers of PermanentRemovalService's legacy removal wrappers changed.\n"
                        + "New code must build a DepartureBatch and call DepartureService.depart instead of "
                        + String.join(", ", new TreeSet<>(WRAPPERS)) + ".\nA caller moved off the wrappers must be "
                        + "removed from ALLOWED_CALLERS.\nNow calling:\n            "
                        + callers.stream().map(caller -> "\"" + caller + "\"")
                                .collect(Collectors.joining(",\n            ")))
                .isEqualTo(new TreeSet<>(ALLOWED_CALLERS));
    }

    private static boolean isProjectMainOutput(Path entry) {
        String path = entry.toString().replace('\\', '/');
        return path.contains("/magical-vibes-")
                && !path.contains("/magical-vibes-card")
                && !path.contains("test")
                && Files.exists(entry);
    }

    private static List<byte[]> classFiles(Path output) {
        try {
            if (Files.isDirectory(output)) {
                try (Stream<Path> files = Files.walk(output)) {
                    return files.filter(file -> file.toString().endsWith(".class"))
                            .map(LegacyDepartureWrapperRatchetTest::read)
                            .toList();
                }
            }
            try (JarFile jar = new JarFile(output.toFile())) {
                return jar.stream()
                        .filter(entry -> entry.getName().endsWith(".class"))
                        .map(entry -> read(jar, entry))
                        .toList();
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static void collectCallers(byte[] bytes, Set<String> callers) {
        ClassModel model = ClassFile.of().parse(bytes);
        String className = model.thisClass().asInternalName();
        for (MethodModel method : model.methods()) {
            String methodName = method.methodName().stringValue();
            if (className.equals(REMOVAL_SERVICE) && WRAPPERS.contains(methodName)) {
                // One wrapper overload delegating to another is not a caller.
                continue;
            }
            method.code().ifPresent(code -> code.forEach(element -> {
                if (element instanceof InvokeInstruction invoke
                        && invoke.owner().asInternalName().equals(REMOVAL_SERVICE)
                        && WRAPPER_SIGNATURES.contains(invoke.name().stringValue() + invoke.type().stringValue())) {
                    callers.add(callerName(className, methodName));
                }
            }));
        }
    }

    private static String signature(Method method) {
        return method.getName()
                + MethodType.methodType(method.getReturnType(), method.getParameterTypes()).toMethodDescriptorString();
    }

    private static String callerName(String internalClassName, String methodName) {
        String outermost = internalClassName.replaceAll("\\$.*", "");
        Matcher lambda = LAMBDA.matcher(methodName);
        return outermost.substring(outermost.lastIndexOf('/') + 1) + "."
                + (lambda.matches() ? lambda.group(1) : methodName);
    }

    private static byte[] read(Path file) {
        try {
            return Files.readAllBytes(file);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static byte[] read(JarFile jar, JarEntry entry) {
        try (InputStream in = jar.getInputStream(entry)) {
            return in.readAllBytes();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}

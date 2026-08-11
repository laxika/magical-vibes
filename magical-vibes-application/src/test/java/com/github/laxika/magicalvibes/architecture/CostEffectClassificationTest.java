package com.github.laxika.magicalvibes.architecture;

import com.github.laxika.magicalvibes.model.effect.CostEffect;
import com.github.laxika.magicalvibes.service.effect.cost.AdditionalSpellCostService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Guard for the additional-cast-cost system: every {@code CostEffect} implementation in the
 * domain module must be classified — either handled by
 * {@link AdditionalSpellCostService#HANDLED_SPELL_COST_TYPES} (payable as an additional cost when
 * casting a spell) or listed here as ability-only (paid by {@code AbilityActivationService}'s
 * activation flow and never placed in a card's SPELL slot).
 *
 * <p>Why: a SPELL-slot cost type that is invisible to {@code AdditionalSpellCostService} is
 * silently skipped by satisfiability, validation and payment — exactly how Seize the Spoils'
 * {@code DiscardCardTypeCost} once let the engine consume mana for a cast it then rejected. A new
 * cost type fails this test until a deliberate decision puts it in one of the two sets (and, if
 * spell-handled, wires its extract/validate/pay support).
 */
class CostEffectClassificationTest {

    /**
     * Cost types only ever used as activation costs (activated/triggered abilities, crew, etc.).
     * If one of these starts appearing in a SPELL slot, move it to
     * {@code HANDLED_SPELL_COST_TYPES} and implement its cast-time support instead.
     */
    private static final Set<String> ABILITY_ONLY_COST_TYPES = Set.of(
            "CrewCost",
            "DrawCardsCost",
            "ExileCardFromHandCost",
            "ExileInstantOrSorcerySpellCost",
            "ExileSelfCost",
            "ExileSelfFromGraveyardCost",
            "ExileTopCardOfGraveyardCost",
            "ExileTopCardOfLibraryCost",
            "IncreaseActivationCostPerCounterEffect",
            "ReduceActivationCostEffect",
            "MillControllerCost",
            "OpponentCreatesTokensCost",
            "PayManaCost",
            "PutCounterOnSourceCost",
            "PutTypedCounterOnSourceCost",
            "PutCardFromHandOnTopOfLibraryCost",
            "ReduceActivationCostPerCounterEffect",
            "RemoveAllCountersAsCostEffect",
            "RemoveCounterFromControlledCreatureCost",
            "RemoveCounterFromControlledPermanentCost",
            "RemoveCounterFromSourceCost",
            "RemoveOneOrMoreCountersFromControlledCreaturesCost",
            "RemoveOneOrMoreCountersFromSourceCost",
            "RemoveXCountersFromSourceCost",
            "ReturnMultiplePermanentsToHandCost",
            "ReturnCardFromGraveyardToHandCost",
            "ReturnSelfToHandCost",
            "RevealTwoCardsSharingColorCost",
            "SacrificePermanentsSequenceCost",
            "SacrificeSelfCost",
            "SacrificeSourceEquipmentCost",
            "SacrificeXPermanentsCost",
            "TapCreatureCost",
            "TapEnchantedPermanentCost",
            "TapTwoCreaturesSharingTypeCost",
            "TapMultiplePermanentsCost",
            "UnattachSourceEquipmentCost",
            "UntapMultiplePermanentsCost");

    /**
     * Spell-handled cost types that do not use the generic single-permanent payment field. These
     * either consume a non-permanent resource or have a dedicated selection route. Every other
     * spell-handled cost is paid via {@code PlayCardRequest.sacrificePermanentId} and must expose
     * the permanent it consumes through {@code CostEffect.consumedPermanentFilter()}.
     */
    private static final Set<String> NON_PERMANENT_SPELL_COST_TYPES = Set.of(
            "SacrificeAllCreaturesYouControlCost",
            "SacrificeAllPermanentsYouControlCost",
            "PayXLifeCost",
            "PayLifeCost",
            "ChooseXValueCost",
            "ExileCardFromGraveyardCost",
            "ExileXCardsFromGraveyardCost",
            "ExileNCardsFromGraveyardCost",
            "DiscardCardTypeCost",
            "DiscardRandomCardCost",
            "DiscardCardOrPayManaCost",
            "DiscardHandCost",
            "DiscardXCardsCost",
            "EscalateDiscardCost",
            "EscalateManaCost",
            "RepeatableAdditionalManaCost",
            "BeholdCost",
            "BeholdAndExileCost",
            "DelveCost");

    private static final String EFFECT_PKG_PATH =
            "magical-vibes-domain/src/main/java/com/github/laxika/magicalvibes/model/effect";
    private static final String EFFECT_PKG = "com.github.laxika.magicalvibes.model.effect";

    @Test
    @DisplayName("Every CostEffect type is classified as spell-handled or ability-only")
    void everyCostEffectTypeIsClassified() throws IOException {
        Set<String> handled = AdditionalSpellCostService.HANDLED_SPELL_COST_TYPES.stream()
                .map(Class::getSimpleName)
                .collect(java.util.stream.Collectors.toSet());

        List<String> unclassified = new ArrayList<>();
        List<String> doublyClassified = new ArrayList<>();
        for (String typeName : costEffectTypeNames()) {
            boolean isHandled = handled.contains(typeName);
            boolean isAbilityOnly = ABILITY_ONLY_COST_TYPES.contains(typeName);
            if (!isHandled && !isAbilityOnly) {
                unclassified.add(typeName);
            }
            if (isHandled && isAbilityOnly) {
                doublyClassified.add(typeName);
            }
        }

        assertThat(unclassified)
                .withFailMessage(() -> "Unclassified CostEffect type(s): " + unclassified + ".\n"
                        + "Every additional-cost effect must be either handled by "
                        + "AdditionalSpellCostService.HANDLED_SPELL_COST_TYPES (with extract/satisfiable/"
                        + "validate/pay support) or declared ability-only in this test — otherwise a spell "
                        + "carrying it would be advertised as castable and then rejected (or worse, cast "
                        + "without paying the cost).")
                .isEmpty();
        assertThat(doublyClassified)
                .withFailMessage(() -> "CostEffect type(s) listed both as spell-handled and ability-only: "
                        + doublyClassified)
                .isEmpty();
    }

    @Test
    @DisplayName("Every permanent-selecting spell cost declares consumedPermanentFilter")
    void permanentSelectingSpellCostsDeclareTheirFilter() {
        List<String> missingFilter = new ArrayList<>();
        for (Class<?> type : AdditionalSpellCostService.HANDLED_SPELL_COST_TYPES) {
            if (NON_PERMANENT_SPELL_COST_TYPES.contains(type.getSimpleName())) {
                continue;
            }
            try {
                type.getDeclaredMethod("consumedPermanentFilter");
            } catch (NoSuchMethodException e) {
                missingFilter.add(type.getSimpleName());
            }
        }

        assertThat(missingFilter)
                .withFailMessage(() -> "Spell cost type(s) paid with a chosen permanent but not overriding "
                        + "CostEffect.consumedPermanentFilter(): " + missingFilter + ".\n"
                        + "Without the filter every AI cast path (AiDecisionEngine.selectSacrificeTarget, "
                        + "GameSimulator.computeSacrificeTarget, the fuzzer's random selector) sends a null "
                        + "sacrificePermanentId, so the engine rejects the cast after advertising it as "
                        + "playable. Declare the filter, or list the type in NON_PERMANENT_SPELL_COST_TYPES "
                        + "if it is paid with a non-permanent resource.")
                .isEmpty();

        List<String> staleNonPermanent = NON_PERMANENT_SPELL_COST_TYPES.stream()
                .filter(name -> AdditionalSpellCostService.HANDLED_SPELL_COST_TYPES.stream()
                        .noneMatch(type -> type.getSimpleName().equals(name)))
                .sorted()
                .toList();
        assertThat(staleNonPermanent)
                .withFailMessage(() -> "NON_PERMANENT_SPELL_COST_TYPES lists type(s) that are no longer "
                        + "spell-handled costs: " + staleNonPermanent)
                .isEmpty();
    }

    @Test
    @DisplayName("Ability-only list contains no stale entries")
    void abilityOnlyListHasNoStaleEntries() throws IOException {
        List<String> existing = costEffectTypeNames();
        List<String> stale = ABILITY_ONLY_COST_TYPES.stream()
                .filter(name -> !existing.contains(name))
                .sorted()
                .toList();
        assertThat(stale)
                .withFailMessage(() -> "ABILITY_ONLY_COST_TYPES lists CostEffect type(s) that no longer exist "
                        + "(or no longer implement CostEffect): " + stale)
                .isEmpty();
    }

    /** Enumerates the simple names of every concrete CostEffect implementor in the effect package. */
    private static List<String> costEffectTypeNames() throws IOException {
        Path effectPkg = locateRepoRoot().resolve(EFFECT_PKG_PATH);
        List<String> names = new ArrayList<>();
        try (Stream<Path> files = Files.list(effectPkg)) {
            for (Path path : (Iterable<Path>) files.sorted()::iterator) {
                String fileName = path.getFileName().toString();
                if (!fileName.endsWith(".java")) {
                    continue;
                }
                String typeName = fileName.substring(0, fileName.length() - ".java".length());
                Class<?> type;
                try {
                    type = Class.forName(EFFECT_PKG + "." + typeName);
                } catch (ClassNotFoundException e) {
                    throw new IllegalStateException("Effect source file without a loadable class: " + fileName, e);
                }
                if (CostEffect.class.isAssignableFrom(type) && !type.isInterface()) {
                    names.add(typeName);
                }
            }
        }
        return names;
    }

    /** Walk up from the test working directory until a Gradle settings file is found. */
    private static Path locateRepoRoot() {
        Path dir = Path.of("").toAbsolutePath();
        for (Path p = dir; p != null; p = p.getParent()) {
            if (Files.exists(p.resolve("settings.gradle.kts")) || Files.exists(p.resolve("settings.gradle"))) {
                return p;
            }
        }
        throw new IllegalStateException(
                "Could not locate repo root (no settings.gradle[.kts]) walking up from " + dir);
    }
}

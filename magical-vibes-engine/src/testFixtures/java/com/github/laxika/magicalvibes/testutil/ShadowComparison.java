package com.github.laxika.magicalvibes.testutil;

import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenForImprintedCardOwnerEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokensForExiledCardsWithSourceEffect;
import com.github.laxika.magicalvibes.model.effect.ExileEquippedCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.ManifestTopCardsForEachDyingSourceCounterEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveLinkedPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeEnchantedCreatureOnLeaveEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerGainsLifeEffect;

import java.util.Map;
import java.util.Set;

/**
 * The slots in shadow mode while their family moves from its slot collectors to the trigger pipeline. For their
 * abilities the pipeline reports what it would trigger, and {@link ShadowTriggerRecorder} reports every trigger
 * one path has and the other lacks. Each difference that is not fixed is recorded here and in
 * effect-slot-refactor.md's Findings:
 *
 * @param slots           the slots compared
 * @param nothingToDoDeclines effects whose old-path handler puts no ability on the stack when it would have
 *                        nothing to do (no linked object, no imprinted card, nothing to count). The ability
 *                        triggers anyway (CR 603.2: none of these has an intervening "if"), so a trigger only the
 *                        pipeline reports for one of them is no difference
 * @param knownDifferences tests ({@code Class#method}) whose difference is a limit of shadow mode itself, with the
 *                        reason; their comparison is skipped
 */
public record ShadowComparison(Set<EffectSlot> slots, Set<Class<?>> nothingToDoDeclines,
                               Map<String, String> knownDifferences) {

    /** Compares nothing: no family is in shadow mode. */
    public static final ShadowComparison NONE = new ShadowComparison(Set.of(), Set.of(), Map.of());

    private static final String ONCE_EACH_TURN = "The old collector uses up a once-each-turn limit before the "
            + "pipeline matches the batch, so LegacyTriggerOptions doesn't check the limit; here the limit was "
            + "really used earlier in the turn.";
    private static final String ATTACHMENT_LOOK_BACK = "\"If it was enchanted/equipped\" reads the attachments, "
            + "which left in the same event; the departure event carries no look-back of its attachments yet "
            + "(step 11).";
    private static final String UNOBSERVED_QUEUE = "The old handler queues the trigger somewhere the recorder "
            + "doesn't watch (pendingSpellCastCostTriggers, the divided-counter assignment queue).";

    /**
     * The comparison every card test runs (see {@code BaseCardTest}): the pilot of effect-slot-refactor.md step
     * 10, the source's own zone changes.
     */
    public static final ShadowComparison CURRENT = new ShadowComparison(
            Set.of(EffectSlot.ON_DEATH,
                    EffectSlot.ON_SELF_LEAVES_BATTLEFIELD,
                    EffectSlot.ON_SELF_PUT_INTO_GRAVEYARD_FROM_ANYWHERE,
                    EffectSlot.ON_SELF_PUT_INTO_GRAVEYARD_FROM_BATTLEFIELD,
                    EffectSlot.ON_SELF_PUT_INTO_GRAVEYARD_FROM_HAND,
                    EffectSlot.ON_SELF_PUT_INTO_GRAVEYARD_FROM_LIBRARY,
                    EffectSlot.ON_SELF_MILLED),
            Set.of(RemoveLinkedPermanentEffect.class,
                    CreateTokensForExiledCardsWithSourceEffect.class,
                    CreateTokenForImprintedCardOwnerEffect.class,
                    SacrificeEnchantedCreatureOnLeaveEffect.class,
                    ExileEquippedCreatureEffect.class,
                    TargetPlayerGainsLifeEffect.class,
                    ManifestTopCardsForEachDyingSourceCounterEffect.class),
            Map.ofEntries(
                    Map.entry("MintharaOfTheAbsoluteTest#selfLeavingSharesOncePerTurnLimit", ONCE_EACH_TURN),
                    Map.entry("TerrorsOfTheTrackTest#selfDeathSharesLimitWithOtherCreatureDeaths", ONCE_EACH_TURN),
                    Map.entry("OniCultAnvilTest#selfSacrificeRespectsOncePerTurnLimit", ONCE_EACH_TURN),
                    Map.entry("GunnerConscriptTest#createsBothJunkTokensWhenAttachmentsAreDestroyedSimultaneously",
                            ATTACHMENT_LOOK_BACK),
                    Map.entry("StoneHavenOutfitterTest#drawsWhenEquipmentAndEquippedCreaturesDieSimultaneously",
                            ATTACHMENT_LOOK_BACK),
                    Map.entry("FoulEmissaryTest#createsTokenWhenSacrificedToCastEmergeSpell", UNOBSERVED_QUEUE),
                    Map.entry("FoulEmissaryTest#doesNotCreateTokenForNonEmergeSpell", UNOBSERVED_QUEUE),
                    Map.entry("JuganTheRisingStarTest#deathDistributionRejectsTooManyCounters", UNOBSERVED_QUEUE),
                    Map.entry("JuganTheRisingStarTest#deathDistributionRejectsTooFewCounters", UNOBSERVED_QUEUE),
                    Map.entry("JuganTheRisingStarTest#deathDistributeDoesNotAffectShroudedCreature",
                            UNOBSERVED_QUEUE),
                    Map.entry("TatsumasaTheDragonsFangTest#tokenDeathReturnsTatsumasaAfterLosingAbilities",
                            "Tatsumasa's return is a delayed trigger (CR 603.7) but is printed on the token as an "
                                    + "ON_DEATH ability; the old death check misses Humility's ability loss, the "
                                    + "departure event's layer-aware flag doesn't."),
                    Map.entry("SerraParagonTest#landWithCopyChoiceRetainsGrantedTrigger",
                            "For a land that entered as a copy (Echoing Deeps) the old collector names the physical "
                                    + "card as the source, though for a creature copy (Phantasmal Image) it names the "
                                    + "copied card; the pipeline always names the effective card it left as.")));

    public ShadowComparison {
        slots = Set.copyOf(slots);
        nothingToDoDeclines = Set.copyOf(nothingToDoDeclines);
        knownDifferences = Map.copyOf(knownDifferences);
    }
}

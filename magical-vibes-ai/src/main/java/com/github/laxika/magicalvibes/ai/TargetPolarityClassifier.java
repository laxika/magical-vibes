package com.github.laxika.magicalvibes.ai;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.effect.CantBlockThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalReplacementEffect;
import com.github.laxika.magicalvibes.model.effect.CreatureBoostEffect;
import com.github.laxika.magicalvibes.model.effect.DamageDealingEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureOrPlaneswalkerEffect;
import com.github.laxika.magicalvibes.model.effect.DistributeCountersAmongTargetsEffect;
import com.github.laxika.magicalvibes.model.effect.GainControlOfTargetEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.KeywordGrantingEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.MustAttackThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.RegenerationEffect;
import com.github.laxika.magicalvibes.model.effect.RemovalEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveAllCountersFromTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.SkipNextUntapEffect;
import com.github.laxika.magicalvibes.model.effect.TapOrUntapTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreaturePerChosenTypeCountEffect;
import com.github.laxika.magicalvibes.model.effect.SetBasePowerToughnessEffect;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static java.util.Map.entry;

/**
 * The single source of truth for {@link TargetPolarity}: which board the AI should aim a
 * permanent-targeting spell or ETB effect at. Every permanent-targeting effect that appears
 * in a card's SPELL or ON_ENTER_BATTLEFIELD slot must classify to a non-null polarity —
 * {@code TargetPolarityGuardTest} enforces this over the whole card pool, so a new effect
 * shape cannot silently fall into {@code AiTargetSelector}'s own-battlefield-first fallback
 * (the bug family behind Quicksilver Geyser bouncing the AI's own artifact and Stun tapping
 * the AI's own blocker).
 *
 * <p>Classification is structural where the shape carries the answer (removal kind, damage
 * capability, boost sign, tap/untap scope) and falls back to an explicit per-class mapping
 * for the long tail. When you add a new permanent-targeting effect, add a rule or mapping
 * here — the guard test's failure message points back to this class.
 */
public class TargetPolarityClassifier {

    private final AmountEvaluationService amountEvaluationService;

    public TargetPolarityClassifier(AmountEvaluationService amountEvaluationService) {
        this.amountEvaluationService = amountEvaluationService;
    }

    /**
     * Card-level polarity over the SPELL and ON_ENTER_BATTLEFIELD slots, collapsed by
     * priority: removal beats damage beats other harm beats beneficial beats neutral.
     * This mirrors (and must preserve) the branch order {@code chooseTarget} routed with
     * before the consolidation. Returns null when no effect classifies.
     */
    public TargetPolarity classifyCard(GameData gameData, Card card, UUID aiPlayerId) {
        TargetPolarity best = null;
        for (EffectSlot slot : new EffectSlot[]{EffectSlot.ON_ENTER_BATTLEFIELD, EffectSlot.SPELL}) {
            for (CardEffect effect : card.getEffects(slot)) {
                best = higherPriority(best, classify(gameData, effect, aiPlayerId));
            }
        }
        return best;
    }

    /**
     * Group-level polarity for one multi-target group's effects, collapsed by the same
     * priority as {@link #classifyCard}. Returns null when no effect classifies.
     */
    TargetPolarity classifyGroup(GameData gameData, List<CardEffect> groupEffects, UUID aiPlayerId) {
        TargetPolarity best = null;
        for (CardEffect effect : groupEffects) {
            best = higherPriority(best, classify(gameData, effect, aiPlayerId));
        }
        return best;
    }

    /**
     * Effect-level polarity. Returns null for shapes this classifier does not know —
     * the guard test keeps that set empty for the card pool's spell/ETB surface.
     */
    TargetPolarity classify(GameData gameData, CardEffect effect, UUID aiPlayerId) {
        // Wrappers: classify what actually happens to the target. Kicker-style replacements
        // use the base mode only — the AI never kicks (mirrors computeBaseAllowedTargets).
        if (effect instanceof ConditionalEffect conditional) {
            return classify(gameData, conditional.wrapped(), aiPlayerId);
        }
        if (effect instanceof MayEffect may) {
            return classify(gameData, may.wrapped(), aiPlayerId);
        }
        if (effect instanceof ConditionalReplacementEffect replacement) {
            return classify(gameData, replacement.baseEffect(), aiPlayerId);
        }
        if (effect instanceof SequenceEffect sequence) {
            TargetPolarity best = null;
            for (CardEffect step : sequence.steps()) {
                best = higherPriority(best, classify(gameData, step, aiPlayerId));
            }
            return best;
        }

        // Removal: the target leaves the battlefield. removalKind() is non-null exactly for
        // single-target destroy/exile/bounce configurations (mirrors SpellEvaluator.removalScore);
        // kind-less configurations (multi-target/X-target removal) are equally harmful to any
        // permanent they do target.
        if (effect instanceof RemovalEffect removal) {
            if (removal.removalKind() != null || effect.targetSpec().category().includesPermanents()) {
                return TargetPolarity.HARMFUL_REMOVAL;
            }
            return null;
        }

        // Damage that can hit a chosen permanent. Player-only damage keyed to a targeted
        // permanent's controller (Fodder Launch's "deals 5 damage to that creature's
        // controller") is still harmful to whoever the card targets; damage with no
        // permanent in its target spec carries no permanent polarity — the player-targeting
        // branch handles it.
        if (effect instanceof DamageDealingEffect damage) {
            if (!effect.targetSpec().category().includesPermanents()) {
                return null;
            }
            return damage.canDamageCreatures() ? TargetPolarity.HARMFUL_DAMAGE : TargetPolarity.HARMFUL;
        }
        if (effect instanceof DealDamageToTargetCreatureOrPlaneswalkerEffect) {
            return TargetPolarity.HARMFUL_DAMAGE;
        }

        // Tap/untap family: scope decides whether a permanent is targeted at all.
        if (effect instanceof TapPermanentsEffect tap) {
            return tap.scope() == TapUntapScope.TARGET ? TargetPolarity.HARMFUL : null;
        }
        if (effect instanceof SkipNextUntapEffect skip) {
            return skip.scope() == TapUntapScope.TARGET ? TargetPolarity.HARMFUL : null;
        }
        if (effect instanceof CantBlockThisTurnEffect cantBlock) {
            return cantBlock.scope() == TapUntapScope.TARGET ? TargetPolarity.HARMFUL : null;
        }
        if (effect instanceof UntapPermanentsEffect untap) {
            return untap.scope() == TapUntapScope.TARGET || untap.scope() == TapUntapScope.ALL_TARGETS
                    ? TargetPolarity.BENEFICIAL
                    : null;
        }
        if (effect instanceof TapOrUntapTargetPermanentEffect) {
            return TargetPolarity.NEUTRAL;
        }

        if (effect instanceof MustAttackThisTurnEffect || effect instanceof GainControlOfTargetEffect) {
            return TargetPolarity.HARMFUL;
        }

        // Counters: -1/-1 hurts, +1/+1 helps, anything else carries no direction.
        if (effect instanceof PutCounterOnTargetPermanentEffect counter) {
            if (counter.counterType() == CounterType.MINUS_ONE_MINUS_ONE) {
                return TargetPolarity.HARMFUL;
            }
            return counter.counterType() == CounterType.PLUS_ONE_PLUS_ONE
                    ? TargetPolarity.BENEFICIAL
                    : TargetPolarity.NEUTRAL;
        }
        if (effect instanceof DistributeCountersAmongTargetsEffect distribute) {
            if (distribute.counterType() == CounterType.MINUS_ONE_MINUS_ONE) {
                return TargetPolarity.HARMFUL;
            }
            return distribute.counterType() == CounterType.PLUS_ONE_PLUS_ONE
                    ? TargetPolarity.BENEFICIAL
                    : TargetPolarity.NEUTRAL;
        }
        // Removing counters inverts the sign: stripping -1/-1 counters helps the target,
        // stripping +1/+1 counters hurts it.
        if (effect instanceof RemoveAllCountersFromTargetPermanentEffect removeAll) {
            if (removeAll.counterType() == CounterType.MINUS_ONE_MINUS_ONE) {
                return TargetPolarity.BENEFICIAL;
            }
            return removeAll.counterType() == CounterType.PLUS_ONE_PLUS_ONE
                    ? TargetPolarity.HARMFUL
                    : TargetPolarity.NEUTRAL;
        }

        // Targeted P/T boosts are sign-aware: any negative component makes the shape a
        // debuff. Dynamic amounts evaluate in estimation context, matching SpellEvaluator.
        if (effect instanceof CreatureBoostEffect boost) {
            AmountContext ctx = AmountContext.forEstimation(aiPlayerId);
            boolean negative = amountEvaluationService.evaluate(gameData, boost.powerBoost(), ctx) < 0
                    || amountEvaluationService.evaluate(gameData, boost.toughnessBoost(), ctx) < 0;
            return negative ? TargetPolarity.HARMFUL : TargetPolarity.BENEFICIAL;
        }

        if (effect instanceof RegenerationEffect) {
            return TargetPolarity.BENEFICIAL;
        }
        if (effect instanceof KeywordGrantingEffect grant) {
            return grant.scope() == GrantScope.TARGET || grant.scope() == GrantScope.ENCHANTED_CREATURE
                    ? TargetPolarity.BENEFICIAL
                    : null;
        }

        // Base-P/T setters swing both ways: Diminish (1/1) shrinks the opponent's fatty,
        // Wings of Velis Vel (4/4 flying) upgrades the AI's own weenie. A small stat line
        // is a shrink, a large one a pump.
        if (effect instanceof SetBasePowerToughnessEffect setStats) {
            return setStats.power() + setStats.toughness() <= 4
                    ? TargetPolarity.HARMFUL
                    : TargetPolarity.BENEFICIAL;
        }

        if (effect instanceof BoostTargetCreaturePerChosenTypeCountEffect scaled) {
            return scaled.powerPer() < 0 || scaled.toughnessPer() < 0
                    ? TargetPolarity.HARMFUL
                    : TargetPolarity.BENEFICIAL;
        }

        return FIXED_BY_CLASS_NAME.get(effect.getClass().getSimpleName());
    }

    /**
     * Fixed polarity for shapes whose direction never depends on instance state. Keyed by
     * simple class name to keep the table readable; this trades compile-time rename safety
     * for legibility, and {@code TargetPolarityGuardTest} makes the trade safe — a renamed
     * or newly added shape with no live entry here fails that test loudly.
     */
    private static final Map<String, TargetPolarity> FIXED_BY_CLASS_NAME = Map.ofEntries(
            // The target leaves the battlefield (or the board position it holds).
            entry("DestroyEachTargetPermanentEffect", TargetPolarity.HARMFUL_REMOVAL),
            entry("DestroyTargetPermanentThenEffect", TargetPolarity.HARMFUL_REMOVAL),
            entry("ExileTargetCreatureAndAllWithSameNameEffect", TargetPolarity.HARMFUL_REMOVAL),
            entry("ExileTargetPermanentAndImprintEffect", TargetPolarity.HARMFUL_REMOVAL),
            entry("ExileTargetPermanentMayPlayUntilNextTurnEffect", TargetPolarity.HARMFUL_REMOVAL),
            entry("ExileTargetPermanentThenEffect", TargetPolarity.HARMFUL_REMOVAL),
            entry("ExileTargetPermanentUntilSourceLeavesEffect", TargetPolarity.HARMFUL_REMOVAL),
            entry("PutTargetOnBottomOfLibraryEffect", TargetPolarity.HARMFUL_REMOVAL),
            entry("PutTargetOnTopOfLibraryEffect", TargetPolarity.HARMFUL_REMOVAL),
            entry("ReturnTargetPermanentToHandOrLibraryTopByPredicateEffect", TargetPolarity.HARMFUL_REMOVAL),
            entry("ReturnTargetPermanentToHandThenEffect", TargetPolarity.HARMFUL_REMOVAL),
            entry("SacrificeTargetCreatureThenCreateTokensEqualToPowerEffect", TargetPolarity.HARMFUL_REMOVAL),
            // Slave of Bolas: the stolen target is sacrificed at end step — net removal. Own-board
            // uses (Hazoret's Favor) are safe: their target filters restrict candidates anyway.
            entry("SacrificeTargetPermanentAtEndStepEffect", TargetPolarity.HARMFUL_REMOVAL),
            entry("ShuffleTargetPermanentIntoLibraryEffect", TargetPolarity.HARMFUL_REMOVAL),

            // The target (or a permanent tied to it) takes damage.
            entry("DealDamageToAnyTargetEqualToChosenTypeCountEffect", TargetPolarity.HARMFUL_DAMAGE),
            entry("DealDamageToEachTargetEffect", TargetPolarity.HARMFUL_DAMAGE),
            entry("DealDamageToTargetAndTheirCreaturesEffect", TargetPolarity.HARMFUL_DAMAGE),
            entry("DealDamageToTargetControllerIfTargetHasKeywordEffect", TargetPolarity.HARMFUL_DAMAGE),
            entry("DealDamageToTargetCreatureEqualToChosenTypeCountEffect", TargetPolarity.HARMFUL_DAMAGE),
            entry("DealDamageToTargetOpponentOrPlaneswalkerEffect", TargetPolarity.HARMFUL_DAMAGE),
            entry("DealDamageToTargetPlayerOrPlaneswalkerEffect", TargetPolarity.HARMFUL_DAMAGE),
            entry("DealDividedDamageEffect", TargetPolarity.HARMFUL_DAMAGE),
            entry("DiscardRandomCardDealDiscardedPowerToTargetPlayerOrPlaneswalkerEffect", TargetPolarity.HARMFUL_DAMAGE),
            entry("RevealTopCardsBottomThenDamageIfCopyRevealedEffect", TargetPolarity.HARMFUL_DAMAGE),
            entry("SacrificeAnotherCreatureDealPowerDamageToAnyTargetEffect", TargetPolarity.HARMFUL_DAMAGE),
            entry("TargetCreatureDealsPowerDamageToSelfEffect", TargetPolarity.HARMFUL_DAMAGE),
            entry("ControlledCreaturesDealPowerDamageToTargetEffect", TargetPolarity.HARMFUL_DAMAGE),

            // Other harm: fights, steals, strips, debuffs, forced blocks.
            entry("DestroyAttachmentsOnTargetCreatureEffect", TargetPolarity.HARMFUL),
            entry("EnchantedCreatureFightsTargetCreatureEffect", TargetPolarity.HARMFUL),
            entry("ExileOwnGraveyardCardThenDamageTargetCreatureControllerEffect", TargetPolarity.HARMFUL),
            entry("FightTargetsEffect", TargetPolarity.HARMFUL),
            entry("SourceFightsTargetCreatureEffect", TargetPolarity.HARMFUL),
            entry("GainControlOfTargetAuraEffect", TargetPolarity.HARMFUL),
            entry("IllicitAuctionEffect", TargetPolarity.HARMFUL),
            entry("LoseAllCreatureTypesEffect", TargetPolarity.HARMFUL),
            entry("LosesAllAbilitiesEffect", TargetPolarity.HARMFUL),
            entry("EnchantedPermanentBecomesOnlyLandEffect", TargetPolarity.HARMFUL),
            entry("BecomeColorlessEffect", TargetPolarity.HARMFUL),
            entry("MarkTargetCreatureExileInsteadOfDieThisTurnEffect", TargetPolarity.HARMFUL),
            entry("MassFightTargetCreatureEffect", TargetPolarity.HARMFUL),
            entry("MustBlockSourceEffect", TargetPolarity.HARMFUL),
            entry("MustBlockTargetCreatureEffect", TargetPolarity.HARMFUL),
            entry("RemoveKeywordEffect", TargetPolarity.HARMFUL),
            entry("RemoveTargetFromCombatEffect", TargetPolarity.HARMFUL),
            entry("TargetCreatureDealsPowerDamageToAnyTargetEffect", TargetPolarity.HARMFUL),
            entry("TargetCreatureDealsPowerDamageToControllerEffect", TargetPolarity.HARMFUL),
            entry("TargetDealsPowerDamageToTargetEffect", TargetPolarity.HARMFUL),
            entry("UnattachEquipmentFromTargetPermanentsEffect", TargetPolarity.HARMFUL),

            // The target's side comes out ahead: pumps, shields, blinks, lure, animation.
            entry("AnimatePermanentsEffect", TargetPolarity.BENEFICIAL),
            entry("AttachSourceEquipmentToTargetCreatureEffect", TargetPolarity.BENEFICIAL),
            entry("BuffTargetCreatureIndefinitelyEffect", TargetPolarity.BENEFICIAL),
            entry("FlickerEffect", TargetPolarity.BENEFICIAL),
            entry("GrantActivatedAbilityEffect", TargetPolarity.BENEFICIAL),
            entry("GrantAdditionalBlockToTargetUntilEndOfTurnEffect", TargetPolarity.BENEFICIAL),
            entry("GrantChosenKeywordToTargetEffect", TargetPolarity.BENEFICIAL),
            entry("GrantEffectToTargetUntilEndOfTurnEffect", TargetPolarity.BENEFICIAL),
            entry("GrantProtectionChoiceUntilEndOfTurnEffect", TargetPolarity.BENEFICIAL),
            entry("GrantProtectionFromCardTypeUntilEndOfTurnEffect", TargetPolarity.BENEFICIAL),
            entry("MakeCreatureUnblockableEffect", TargetPolarity.BENEFICIAL),
            entry("MustBeBlockedByAllCreaturesThisTurnEffect", TargetPolarity.BENEFICIAL),
            entry("MustBeBlockedIfAbleThisTurnEffect", TargetPolarity.BENEFICIAL),
            entry("PreventDamageEffect", TargetPolarity.BENEFICIAL),
            entry("PreventDamageFromChosenSourceAndRedirectToAnyTargetEffect", TargetPolarity.BENEFICIAL),
            entry("PreventDamageToTargetFromChosenSourceEffect", TargetPolarity.BENEFICIAL),
            entry("PreventDividedDamageEffect", TargetPolarity.BENEFICIAL),
            entry("PreventNextDamageToTargetAndAddToughnessCountersEffect", TargetPolarity.BENEFICIAL),

            // Deliberately directionless: copies, color/type tweaks, symmetric moves — and
            // Polymorph/Shape Anew-style upgrades that are usually aimed at the AI's own
            // permanents (NEUTRAL keeps the own-battlefield-first fallback for them).
            entry("AddCardTypeToTargetPermanentEffect", TargetPolarity.NEUTRAL),
            // Quarry Hauler chooses add-or-remove per counter kind at resolution — no fixed direction.
            entry("AdjustEachCounterKindOnTargetEffect", TargetPolarity.NEUTRAL),
            entry("AttachAllAurasToAnotherPermanentEffect", TargetPolarity.NEUTRAL),
            entry("BecomeChosenColorsUntilEndOfTurnEffect", TargetPolarity.NEUTRAL),
            entry("ChangeColorTextEffect", TargetPolarity.NEUTRAL),
            entry("CreateTokenCopyAndLinkToSourceEffect", TargetPolarity.NEUTRAL),
            entry("CreateTokenCopyOfTargetCreatureForTargetPlayerEffect", TargetPolarity.NEUTRAL),
            entry("CreateTokenCopyOfTargetPermanentEffect", TargetPolarity.NEUTRAL),
            entry("DestroyTargetThenRevealUntilTypeToBattlefieldEffect", TargetPolarity.NEUTRAL),
            entry("EachOtherCreatureBecomesCopyOfTargetCreatureUntilEndOfTurnEffect", TargetPolarity.NEUTRAL),
            entry("GrantBasicLandTypeToTargetEffect", TargetPolarity.NEUTRAL),
            entry("GrantColorUntilEndOfTurnEffect", TargetPolarity.NEUTRAL),
            entry("MoveCounterFromTargetCreatureToTargetCreatureEffect", TargetPolarity.NEUTRAL),
            entry("RemoveCounterFromTargetPermanentEffect", TargetPolarity.NEUTRAL),
            entry("SacrificeTargetThenRevealUntilTypeToBattlefieldEffect", TargetPolarity.NEUTRAL),
            entry("SetTargetColorEffect", TargetPolarity.NEUTRAL),
            entry("SwitchPowerToughnessEffect", TargetPolarity.NEUTRAL),
            entry("TargetCreatureBecomesSubtypeUntilEndOfTurnEffect", TargetPolarity.NEUTRAL)
    );

    /**
     * Collapses two polarities by routing priority (enum declaration order; null loses to
     * anything). Removal > damage > other harm > beneficial > neutral, preserving the
     * pre-consolidation branch order of {@code chooseTarget}.
     */
    private static TargetPolarity higherPriority(TargetPolarity a, TargetPolarity b) {
        if (a == null) return b;
        if (b == null) return a;
        return a.ordinal() <= b.ordinal() ? a : b;
    }
}

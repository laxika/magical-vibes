package com.github.laxika.magicalvibes.ai;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.effect.CantAttackThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.CantBlockThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalReplacementEffect;
import com.github.laxika.magicalvibes.model.effect.CounterRemovalSubject;
import com.github.laxika.magicalvibes.model.effect.CreateTokenAttachedToTargetEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenAttachedToTargetThenEffect;
import com.github.laxika.magicalvibes.model.effect.CreatureBoostEffect;
import com.github.laxika.magicalvibes.model.effect.DamageDealingEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureOrPlaneswalkerEffect;
import com.github.laxika.magicalvibes.model.effect.DistributeCountersAmongTargetsEffect;
import com.github.laxika.magicalvibes.model.effect.FlipCoinWinEffect;
import com.github.laxika.magicalvibes.model.effect.FlipUntilLoseOrStopEffect;
import com.github.laxika.magicalvibes.model.effect.ExchangeControlOfTargetPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.ExploreEffect;
import com.github.laxika.magicalvibes.model.effect.ExileCardFromGraveyardThenEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetCreaturesUntilSourceLeavesWithCounterEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentUntilSourceLeavesAndReturnOthersEffect;
import com.github.laxika.magicalvibes.model.effect.GainControlOfTargetEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.KeywordGrantingEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.effect.PhaseOutEffect;
import com.github.laxika.magicalvibes.model.effect.PhaseOutSubject;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.PutTargetSpellOrPermanentOrGraveyardCardOnTopOrBottomOfLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.RedirectNextDamageEffect;
import com.github.laxika.magicalvibes.model.effect.RedirectRole;
import com.github.laxika.magicalvibes.model.effect.RegisterDelayedWatchedCreatureDealsDamageEffect;
import com.github.laxika.magicalvibes.model.effect.RegenerationEffect;
import com.github.laxika.magicalvibes.model.effect.RemovalEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveAllCountersEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsOrElseEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.SetCombatRequirementThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.SkipNextUntapEffect;
import com.github.laxika.magicalvibes.model.effect.SuspectEffect;
import com.github.laxika.magicalvibes.model.effect.TapOrUntapTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.TargetCreatureMustAttackSourcePermanentNextTurnEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPredicate;
import com.github.laxika.magicalvibes.model.effect.TributeNotPaidEffect;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreaturePerChosenTypeCountEffect;
import com.github.laxika.magicalvibes.model.effect.SetBasePowerToughnessEffect;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
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
        // "You may pay {X}. If you do / don't, …" — classify whichever branch targets a permanent
        // (Knight of the Mists: wrapped null, elseEffect destroys; Spellbomb: wrapped does work).
        if (effect instanceof MayPayManaEffect mayPay) {
            TargetPolarity paid = mayPay.wrapped() == null
                    ? null
                    : classify(gameData, mayPay.wrapped(), aiPlayerId);
            TargetPolarity declined = mayPay.elseEffect() == null
                    ? null
                    : classify(gameData, mayPay.elseEffect(), aiPlayerId);
            return higherPriority(paid, declined);
        }
        if (effect instanceof ConditionalReplacementEffect replacement) {
            return classify(gameData, replacement.baseEffect(), aiPlayerId);
        }
        if (effect instanceof FlipCoinWinEffect flip) {
            TargetPolarity won = flip.wrapped() == null
                    ? null
                    : classify(gameData, flip.wrapped(), aiPlayerId);
            TargetPolarity lost = flip.lost() == null
                    ? null
                    : classify(gameData, flip.lost(), aiPlayerId);
            return higherPriority(won, lost);
        }
        if (effect instanceof FlipUntilLoseOrStopEffect flip) {
            TargetPolarity best = null;
            for (CardEffect reward : flip.rewards()) {
                best = higherPriority(best, classify(gameData, reward, aiPlayerId));
            }
            return best;
        }
        if (effect instanceof SequenceEffect sequence) {
            TargetPolarity best = null;
            for (CardEffect step : sequence.steps()) {
                best = higherPriority(best, classify(gameData, step, aiPlayerId));
            }
            return best;
        }
        if (effect instanceof ExileCardFromGraveyardThenEffect exileThen) {
            return classify(gameData, exileThen.thenEffect(), aiPlayerId);
        }
        if (effect instanceof TributeNotPaidEffect tributeNotPaid) {
            return classify(gameData, tributeNotPaid.wrapped(), aiPlayerId);
        }
        if (effect instanceof SacrificePermanentsOrElseEffect sacrificeOrElse) {
            TargetPolarity sacrificed = classify(gameData, sacrificeOrElse.sacrificedEffect(), aiPlayerId);
            TargetPolarity otherwise = classify(gameData, sacrificeOrElse.elseEffect(), aiPlayerId);
            return higherPriority(sacrificed, otherwise);
        }

        if (effect instanceof CreateTokenAttachedToTargetEffect attachToken) {
            return attachToken.targetControllerRelation() == PlayerRelation.OPPONENT
                    ? TargetPolarity.HARMFUL
                    : TargetPolarity.BENEFICIAL;
        }
        if (effect instanceof CreateTokenAttachedToTargetThenEffect attachTokenThen) {
            return attachTokenThen.targetControllerRelation() == PlayerRelation.OPPONENT
                    ? TargetPolarity.HARMFUL
                    : TargetPolarity.BENEFICIAL;
        }

        if (effect instanceof ExileTargetCreaturesUntilSourceLeavesWithCounterEffect
                || effect instanceof ExileTargetPermanentUntilSourceLeavesAndReturnOthersEffect
                || effect instanceof PutTargetSpellOrPermanentOrGraveyardCardOnTopOrBottomOfLibraryEffect) {
            return TargetPolarity.HARMFUL_REMOVAL;
        }

        // Removal: the target leaves the battlefield. removalKind() is non-null exactly for
        // single-target destroy/exile/bounce configurations (mirrors SpellEvaluator.removalScore);
        // kind-less configurations (multi-target/X-target removal) are equally harmful to any
        // permanent they do target.
        if (effect instanceof RemovalEffect removal) {
            if (removal.removalKind() != null || effect.targetSpec().admits(TargetPredicate.Kind.PERMANENT)) {
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
            if (!effect.targetSpec().admits(TargetPredicate.Kind.PERMANENT)) {
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
        if (effect instanceof CantAttackThisTurnEffect cantAttack) {
            return cantAttack.scope() == TapUntapScope.TARGET ? TargetPolarity.HARMFUL : null;
        }
        if (effect instanceof CantBlockThisTurnEffect cantBlock) {
            // TARGET_CONTROLLERS_OTHER_CREATURES (Mark for Death) shuts down the rest of the
            // target's controller's defence, so it aims at the opponent just like TARGET does.
            return cantBlock.scope() == TapUntapScope.TARGET
                    || cantBlock.scope() == TapUntapScope.TARGET_CONTROLLERS_OTHER_CREATURES
                    ? TargetPolarity.HARMFUL
                    : null;
        }
        if (effect instanceof UntapPermanentsEffect untap) {
            return untap.scope() == TapUntapScope.TARGET || untap.scope() == TapUntapScope.ALL_TARGETS
                    ? TargetPolarity.BENEFICIAL
                    : null;
        }
        if (effect instanceof TapOrUntapTargetPermanentEffect) {
            return TargetPolarity.NEUTRAL;
        }

        // Phasing: only the targeted form aims at a permanent the AI chose (Reality Ripple, Vision
        // Charm) — the source and attached forms carry no target and no polarity.
        if (effect instanceof PhaseOutEffect phaseOut) {
            return phaseOut.subject() == PhaseOutSubject.TARGET ? TargetPolarity.HARMFUL_REMOVAL : null;
        }

        // Redirect-next-damage: the target is either the object the redirected damage lands on
        // (Zhalfirin Crusader — aim at the opponent) or the object being shielded (Martyrdom,
        // Hazduhr the Abbot — aim at the AI's own board).
        if (effect instanceof RedirectNextDamageEffect redirect) {
            return redirect.destinationRole() == RedirectRole.TARGET
                    ? TargetPolarity.HARMFUL_DAMAGE
                    : TargetPolarity.BENEFICIAL;
        }

        // Conjured Currency's source-mode exchange hands the source away but takes the target, so
        // the target should be an opponent's permanent.
        if (effect instanceof ExchangeControlOfTargetPermanentsEffect) {
            return TargetPolarity.HARMFUL;
        }

        if (effect instanceof TargetCreatureMustAttackSourcePermanentNextTurnEffect
                || effect instanceof GainControlOfTargetEffect) {
            return TargetPolarity.HARMFUL;
        }

        // One-shot combat requirements split by who benefits: forcing a creature to attack or block is a
        // punisher aimed at an opponent's creature, while the "must BE blocked" lures help the AI's own.
        if (effect instanceof SetCombatRequirementThisTurnEffect combatRequirement) {
            return switch (combatRequirement.requirement()) {
                case MUST_ATTACK, MUST_ATTACK_EFFECT_CONTROLLER, MUST_BLOCK, MUST_ATTACK_OR_BLOCK -> TargetPolarity.HARMFUL;
                case MUST_BE_BLOCKED, MUST_BE_BLOCKED_BY_ALL -> TargetPolarity.BENEFICIAL;
            };
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
        if (effect instanceof RemoveAllCountersEffect removeAll
                && removeAll.subject() == CounterRemovalSubject.TARGET) {
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
        if (effect instanceof ExploreEffect explore) {
            return explore.targeted() ? TargetPolarity.BENEFICIAL : null;
        }
        if (effect instanceof RegisterDelayedWatchedCreatureDealsDamageEffect) {
            return TargetPolarity.BENEFICIAL;
        }
        if (effect instanceof KeywordGrantingEffect grant) {
            return grant.scope() == GrantScope.TARGET
                    || grant.scope() == GrantScope.TARGET_AND_SHARING_CREATURES
                    || grant.scope() == GrantScope.ENCHANTED_CREATURE
                    ? TargetPolarity.BENEFICIAL
                    : null;
        }
        if (effect instanceof SuspectEffect suspect) {
            return suspect.scope() == GrantScope.TARGET ? TargetPolarity.NEUTRAL : null;
        }

        // Base-P/T setters swing both ways: Diminish (1/1) shrinks the opponent's fatty,
        // Wings of Velis Vel (4/4 flying) upgrades the AI's own weenie. A small stat line
        // is a shrink, a large one a pump.
        if (effect instanceof SetBasePowerToughnessEffect setStats) {
            // A partial setter leaves the other component alone, so score only what it writes.
            int written = (setStats.power() == null ? 2 : setStats.power())
                    + (setStats.toughness() == null ? 2 : setStats.toughness());
            return written <= 4
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
            entry("DestroyTwoTargetCreaturesIfSameColorsEffect", TargetPolarity.HARMFUL_REMOVAL),
            // Blood Frenzy: the pump rides along, but the target still dies at the next end
            // step, so removal outranks the boost's BENEFICIAL and aims at the opponent.
            entry("DestroyTargetPermanentAtEndStepEffect", TargetPolarity.HARMFUL_REMOVAL),
            entry("DestroyTargetPermanentThenEffect", TargetPolarity.HARMFUL_REMOVAL),
            entry("ExileTargetCreatureAndAllWithSameNameEffect", TargetPolarity.HARMFUL_REMOVAL),
            entry("ExileTargetPermanentAndAllWithSameNameUntilSourceLeavesEffect", TargetPolarity.HARMFUL_REMOVAL),
            entry("ExileTargetPermanentAndImprintEffect", TargetPolarity.HARMFUL_REMOVAL),
            entry("ExileTargetPermanentAndTrackWithSourceEffect", TargetPolarity.HARMFUL_REMOVAL),
            entry("ExileTargetPermanentMayPlayUntilNextTurnEffect", TargetPolarity.HARMFUL_REMOVAL),
            entry("ExileTargetPermanentThenEffect", TargetPolarity.HARMFUL_REMOVAL),
            entry("ExileTargetPermanentUntilSourceLeavesEffect", TargetPolarity.HARMFUL_REMOVAL),
            entry("PutTargetOnBottomOfLibraryEffect", TargetPolarity.HARMFUL_REMOVAL),
            entry("PutTargetOnTopOfLibraryEffect", TargetPolarity.HARMFUL_REMOVAL),
            entry("PutTargetCreatureOnTopOrOptionalBottomOfLibraryEffect", TargetPolarity.HARMFUL_REMOVAL),
            entry("PutTargetPermanentIntoLibraryNFromTopOrBottomEffect", TargetPolarity.HARMFUL_REMOVAL),
            entry("PutTargetPermanentIntoLibraryNFromTopThenEffect", TargetPolarity.HARMFUL_REMOVAL),
            // Chronostutter: library tuck of a permanent at a fixed depth.
            entry("PutTargetPermanentIntoLibraryNFromTopEffect", TargetPolarity.HARMFUL_REMOVAL),
            entry("PutTargetSpellOrCreatureOnTopOrBottomOfLibraryEffect", TargetPolarity.HARMFUL_REMOVAL),
            entry("PutTargetSpellOrPermanentOnTopOrBottomOfLibraryEffect", TargetPolarity.HARMFUL_REMOVAL),
            // Commit // Memory: library tuck of a spell or nonland permanent.
            entry("PutTargetSpellOrPermanentIntoLibraryNFromTopEffect", TargetPolarity.HARMFUL_REMOVAL),
            entry("ReturnTargetPermanentToHandOrLibraryTopByPredicateEffect", TargetPolarity.HARMFUL_REMOVAL),
            entry("ReturnTargetPermanentToHandThenEffect", TargetPolarity.HARMFUL_REMOVAL),
            entry("SacrificeTargetCreatureThenCreateTokensEqualToPowerEffect", TargetPolarity.HARMFUL_REMOVAL),
            // Slave of Bolas: the stolen target is sacrificed at end step — net removal. Own-board
            // uses (Hazoret's Favor) are safe: their target filters restrict candidates anyway.
            entry("SacrificeTargetPermanentAtEndStepEffect", TargetPolarity.HARMFUL_REMOVAL),
            entry("SacrificeTargetPermanentAtEndStepAndGainLifeEqualToToughnessEffect", TargetPolarity.HARMFUL_REMOVAL),
            entry("ShuffleTargetPermanentIntoLibraryEffect", TargetPolarity.HARMFUL_REMOVAL),
            entry("ShuffleTargetPermanentIntoLibraryThenDiscoverEffect", TargetPolarity.HARMFUL_REMOVAL),
            entry("EquipoiseEffect", TargetPolarity.HARMFUL_REMOVAL),
            entry("WintersChillEffect", TargetPolarity.HARMFUL_REMOVAL),

            // The target (or a permanent tied to it) takes damage.
            entry("DealDamageEqualToChosenTypeCountEffect", TargetPolarity.HARMFUL_DAMAGE),
            entry("DealDamageToOtherCreaturesControlledByTargetEffect", TargetPolarity.HARMFUL_DAMAGE),
            entry("DealDamageToEachTargetEffect", TargetPolarity.HARMFUL_DAMAGE),
            entry("DealDamageToTargetAndTheirCreaturesEffect", TargetPolarity.HARMFUL_DAMAGE),
            entry("DealDamageToTargetControllerIfTargetHasKeywordEffect", TargetPolarity.HARMFUL_DAMAGE),
            entry("DealDamageToTargetPlayerOrPlaneswalkerEffect", TargetPolarity.HARMFUL_DAMAGE),
            entry("DealDividedDamageEffect", TargetPolarity.HARMFUL_DAMAGE),
            entry("FlipUntilLoseOrStopEffect", TargetPolarity.HARMFUL_DAMAGE),
            entry("DiscardRandomCardDealDiscardedPowerToTargetPlayerOrPlaneswalkerEffect", TargetPolarity.HARMFUL_DAMAGE),
            // Divine Deflection prevents damage to its controller, but the target is who the
            // prevented damage is then dealt to.
            entry("PreventXDamageToControllerAndPermanentsAndRedirectToAnyTargetEffect", TargetPolarity.HARMFUL_DAMAGE),
            entry("PreventXDamageToTargetCreatureAndRedirectToAnyTargetEffect", TargetPolarity.HARMFUL_DAMAGE),
            entry("RedirectCombatDamageFromTargetUnblockedCreatureToItsControllerEffect", TargetPolarity.HARMFUL_DAMAGE),
            entry("RedirectCombatDamageToTargetAttackingCreatureEffect", TargetPolarity.HARMFUL_DAMAGE),
            entry("RedirectNextDamageFromTargetToAnotherTargetEffect", TargetPolarity.HARMFUL_DAMAGE),
            entry("RevealTopCardsBottomThenDamageIfCopyRevealedEffect", TargetPolarity.HARMFUL_DAMAGE),
            entry("SacrificeAnotherCreatureDealPowerDamageToAnyTargetEffect", TargetPolarity.HARMFUL_DAMAGE),
            entry("DoubleDamageFromTargetPermanentThisTurnEffect", TargetPolarity.HARMFUL_DAMAGE),
            entry("TargetCreatureDealsPowerDamageToSelfEffect", TargetPolarity.HARMFUL_DAMAGE),
            entry("ChannelHarmEffect", TargetPolarity.HARMFUL_DAMAGE),
            entry("ControlledCreaturesDealPowerDamageToTargetEffect", TargetPolarity.HARMFUL_DAMAGE),
            entry("ExileTopCardMayDealDamageOrPlayEffect", TargetPolarity.HARMFUL_DAMAGE),
            entry("ExileTopUntilNonlandDealManaValueDamageToAnyTargetEffect", TargetPolarity.HARMFUL_DAMAGE),
            entry("RevealTopCardDealManaValueDamageToAnyTargetEffect", TargetPolarity.HARMFUL_DAMAGE),
            entry("RevealUntilNonlandBottomThenDealManaValueDamageEffect", TargetPolarity.HARMFUL_DAMAGE),
            entry("RevealUntilNonlandToHandRestToBottomDealManaValueDamageEffect", TargetPolarity.HARMFUL_DAMAGE),

            // Other harm: fights, steals, strips, debuffs, forced blocks.
            entry("DestroyAttachmentsOnTargetCreatureEffect", TargetPolarity.HARMFUL),
            entry("CantBlockTargetAndSharingCreaturesUntilEndOfTurnEffect", TargetPolarity.HARMFUL),
            entry("EnchantedCreatureFightsTargetCreatureEffect", TargetPolarity.HARMFUL),
            entry("EnteringCreatureFightsTargetCreatureEffect", TargetPolarity.HARMFUL),
            entry("ExileOwnGraveyardCardThenDamageTargetCreatureControllerEffect", TargetPolarity.HARMFUL),
            entry("FightTargetsEffect", TargetPolarity.HARMFUL),
            entry("SourceFightsTargetCreatureEffect", TargetPolarity.HARMFUL),
            entry("GainControlOfTargetAuraEffect", TargetPolarity.HARMFUL),
            entry("IllicitAuctionEffect", TargetPolarity.HARMFUL),
            entry("LockTargetPermanentEffect", TargetPolarity.HARMFUL),
            entry("LoseAllCreatureTypesEffect", TargetPolarity.HARMFUL),
            entry("RemoveAllCountersAndLockPermanentEffect", TargetPolarity.HARMFUL),
            entry("LosesAllAbilitiesEffect", TargetPolarity.HARMFUL),
            entry("EnchantedPermanentBecomesOnlyLandEffect", TargetPolarity.HARMFUL),
            entry("BecomeColorlessEffect", TargetPolarity.HARMFUL),
            entry("MarkTargetCreatureExileInsteadOfDieThisTurnEffect", TargetPolarity.HARMFUL),
            entry("MassFightTargetCreatureEffect", TargetPolarity.HARMFUL),
            entry("MakeTargetAttackingCreatureBlockedEffect", TargetPolarity.HARMFUL),
            entry("MustBlockSourceEffect", TargetPolarity.HARMFUL),
            entry("MustBlockTargetCreatureEffect", TargetPolarity.HARMFUL),
            entry("PreventTargetCreatureRegenerationThisTurnEffect", TargetPolarity.HARMFUL),
            entry("RemoveKeywordEffect", TargetPolarity.HARMFUL),
            entry("RemoveTargetFromCombatEffect", TargetPolarity.HARMFUL),
            entry("TargetCreatureDealsPowerDamageToAnyTargetEffect", TargetPolarity.HARMFUL),
            entry("TargetCreatureDealsPowerDamageToControllerEffect", TargetPolarity.HARMFUL),
            entry("TargetCreaturesDealPowerDamageToTargetEffect", TargetPolarity.HARMFUL_DAMAGE),
            entry("TargetCreaturesDealToughnessDamageToEachOtherEffect", TargetPolarity.HARMFUL_DAMAGE),
            entry("TargetDealsPowerDamageToTargetEffect", TargetPolarity.HARMFUL),
            entry("EachTargetCreatureDealsPowerDamageToTargetCreatureEffect", TargetPolarity.HARMFUL_DAMAGE),
            entry("RemoveUpToCountersFromTargetEffect", TargetPolarity.HARMFUL),
            entry("TargetPlayerGainsControlOfTargetPermanentEffect", TargetPolarity.HARMFUL),
            entry("UnattachEquipmentFromTargetPermanentsEffect", TargetPolarity.HARMFUL),
            entry("PayAnyAmountOfEnergyToBoostTargetCreatureEffect", TargetPolarity.HARMFUL),
            entry("PayEnergyToGainControlOfTargetEffect", TargetPolarity.HARMFUL),
            entry("PutCounterOnEitherTargetPermanentEffect", TargetPolarity.HARMFUL),
            entry("RemoveChosenCountersFromTargetPermanentEffect", TargetPolarity.HARMFUL),

            // Arcbond's watched creature is a strategic choice: either player's creature may be
            // the best center for the later symmetric damage event.
            entry("RegisterDelayedWatchedCreatureDealtDamageEffect", TargetPolarity.NEUTRAL),
            entry("RegisterDelayedWatchedCreatureDealtDamageByAttackingCreatureEffect", TargetPolarity.NEUTRAL),
            // The delayed combat-damage reward belongs to the effect's controller, but either
            // player's creature can be the strategically useful watched source.
            entry("RegisterDelayedWatchedCreaturesCombatDamageEffect", TargetPolarity.NEUTRAL),
            // Feint can save either an attacker or its blockers depending on the combat state.
            entry("TapAndPreventCombatDamageByTargetAndBlockersEffect", TargetPolarity.NEUTRAL),
            entry("TurnTargetCreatureFaceDownEffect", TargetPolarity.NEUTRAL),
            entry("TurnTargetCreatureFaceUpEffect", TargetPolarity.NEUTRAL),

            // The target's side comes out ahead: pumps, shields, blinks, lure, animation.
            entry("AnimatePermanentsEffect", TargetPolarity.BENEFICIAL),
            entry("EarthbendTargetLandEffect", TargetPolarity.BENEFICIAL),
            entry("EarthbendTargetLandThenFightEffect", TargetPolarity.BENEFICIAL),
            entry("AttachOneOfControlledEquipmentToTargetCreatureEffect", TargetPolarity.BENEFICIAL),
            entry("AttachTargetEquipmentToTargetCreatureEffect", TargetPolarity.BENEFICIAL),
            entry("AttachTargetAuraOrEquipmentToTargetCreatureEffect", TargetPolarity.BENEFICIAL),
            entry("AttachSourceEquipmentToTargetCreatureEffect", TargetPolarity.BENEFICIAL),
            entry("BuffTargetCreatureIndefinitelyEffect", TargetPolarity.BENEFICIAL),
            entry("DestroyCreaturesBlockedByTargetWallThenReturnFromGraveyardEffect", TargetPolarity.BENEFICIAL),
            entry("DoubleCountersOnTargetPermanentEffect", TargetPolarity.BENEFICIAL),
            entry("DoublePlusOneCountersOnTargetCreatureEffect", TargetPolarity.BENEFICIAL),
            entry("DoubleTargetCreaturePowerEffect", TargetPolarity.BENEFICIAL),
            entry("DrawDiscardAndConniveEffect", TargetPolarity.BENEFICIAL),
            entry("FlickerEffect", TargetPolarity.BENEFICIAL),
            // Predator's Rapport: targets a creature you control and only reads its stats.
            entry("GainLifeEqualToTargetCreatureStatEffect", TargetPolarity.BENEFICIAL),
            // Chandra's Ignition: the target is a creature you control and is only the damage
            // source — it takes no damage itself, so the AI should aim at its own board.
            entry("TargetCreatureDealsPowerDamageToEachOtherCreatureAndEachOpponentEffect", TargetPolarity.BENEFICIAL),
            entry("TargetCreatureDealsPowerDamageToEachOtherCreatureEffect", TargetPolarity.BENEFICIAL),
            entry("TapTargetThenEffect", TargetPolarity.BENEFICIAL),
            entry("GrantActivatedAbilityEffect", TargetPolarity.BENEFICIAL),
            entry("CanBlockAnyNumberOfCreaturesUntilEndOfTurnEffect", TargetPolarity.BENEFICIAL),
            entry("GrantProtectionChoiceToTargetAndSharingCreaturesUntilEndOfTurnEffect", TargetPolarity.BENEFICIAL),
            entry("GrantAdditionalBlockToTargetUntilEndOfTurnEffect", TargetPolarity.BENEFICIAL),
            entry("GrantChosenKeywordEffect", TargetPolarity.BENEFICIAL),
            entry("GrantEffectToTargetUntilEndOfTurnEffect", TargetPolarity.BENEFICIAL),
            entry("GrantEffectToTargetEffect", TargetPolarity.BENEFICIAL),
            entry("GrantProtectionChoiceUntilEndOfTurnEffect", TargetPolarity.BENEFICIAL),
            entry("GrantProtectionFromCardTypeUntilEndOfTurnEffect", TargetPolarity.BENEFICIAL),
            entry("GrantProtectionFromOpponentCreaturesUntilEndOfTurnEffect", TargetPolarity.BENEFICIAL),
            entry("GuardianAngelPermissionEffect", TargetPolarity.BENEFICIAL),
            entry("GrantTargetingRestrictionToTargetUntilEndOfTurnEffect", TargetPolarity.BENEFICIAL),
            entry("MakeCreatureUnblockableEffect", TargetPolarity.BENEFICIAL),
            entry("TapCombatOpponentsOfTargetAtEndOfCombatEffect", TargetPolarity.BENEFICIAL),
            entry("TransformTargetPermanentEffect", TargetPolarity.BENEFICIAL),
            entry("PreventDamageEffect", TargetPolarity.BENEFICIAL),
            entry("PreventDamageFromChosenSourceAndRedirectToAnyTargetEffect", TargetPolarity.BENEFICIAL),
            entry("PreventDamageToTargetCreatureFromTargetingSpellOrAbilityEffect", TargetPolarity.BENEFICIAL),
            entry("PreventDamageToTargetFromChosenSourceEffect", TargetPolarity.BENEFICIAL),
            entry("PreventDividedDamageEffect", TargetPolarity.BENEFICIAL),
            entry("PreventNextDamageByTargetCreatureEffect", TargetPolarity.BENEFICIAL),
            entry("PreventNextDamageToTargetAndAddPlusOnePlusOneCountersEffect", TargetPolarity.BENEFICIAL),
            entry("PreventNextDamageToTargetAndAddToughnessCountersEffect", TargetPolarity.BENEFICIAL),
            entry("RedirectAllDamageToTargetCreatureToControllerEffect", TargetPolarity.BENEFICIAL),
            entry("RedirectAllDamageToChosenCreatureUntilNextTurnEffect", TargetPolarity.BENEFICIAL),
            entry("RedirectTargetCreatureDamageFromChosenSourceToTargetEffect", TargetPolarity.BENEFICIAL),
            entry("ReturnTargetCardOnDeathThisTurnEffect", TargetPolarity.BENEFICIAL),
            entry("UntapTargetAndSharingCreaturesEffect", TargetPolarity.BENEFICIAL),

            // Pack Hunt searches for cards named after the opposing creature it targets.
            entry("SearchLibraryForCardsWithTargetCreatureNameEffect", TargetPolarity.HARMFUL),
            entry("SearchTargetPermanentControllerLibraryForSameNameToBattlefieldEffect", TargetPolarity.HARMFUL),

            // Deliberately directionless: copies, color/type tweaks, symmetric moves — and
            // Polymorph/Shape Anew-style upgrades that are usually aimed at the AI's own
            // permanents (NEUTRAL keeps the own-battlefield-first fallback for them).
            entry("AddCardTypeToTargetPermanentEffect", TargetPolarity.NEUTRAL),
            entry("AddAnotherCounterOfEachKindToTargetEffect", TargetPolarity.NEUTRAL),
            entry("AdjustChosenCounterOnTargetEffect", TargetPolarity.NEUTRAL),
            // Quarry Hauler chooses add-or-remove per counter kind at resolution — no fixed direction.
            entry("AdjustEachCounterKindOnTargetEffect", TargetPolarity.NEUTRAL),
            entry("AttachAllAurasToAnotherPermanentEffect", TargetPolarity.NEUTRAL),
            entry("AttachTargetAuraToAnotherPermanentOfSameTypeEffect", TargetPolarity.NEUTRAL),
            entry("AttachTargetAuraToTargetCreatureEffect", TargetPolarity.NEUTRAL),
            entry("BecomeChosenColorsUntilEndOfTurnEffect", TargetPolarity.NEUTRAL),
            entry("BecomeChosenColorsIndefinitelyEffect", TargetPolarity.NEUTRAL),
            entry("ChangeColorTextEffect", TargetPolarity.NEUTRAL),
            entry("ChooseOneForTargetPermanentEffect", TargetPolarity.NEUTRAL),
            entry("CreateTokenCopyAndLinkToSourceEffect", TargetPolarity.NEUTRAL),
            entry("CreateTokenCopyOfTargetCreatureForTargetPlayerEffect", TargetPolarity.NEUTRAL),
            entry("CreateTokenCopyOfTargetPermanentEffect", TargetPolarity.NEUTRAL),
            entry("RegisterMysticReflectionEffect", TargetPolarity.NEUTRAL),
            entry("DestroyTargetThenRevealUntilTypeToBattlefieldEffect", TargetPolarity.NEUTRAL),
            entry("ExileTargetThenRevealUntilTypeToBattlefieldEffect", TargetPolarity.NEUTRAL),
            entry("EachControlledPermanentBecomesCopyOfTargetNonAuraPermanentEffect", TargetPolarity.NEUTRAL),
            entry("EachOtherCreatureBecomesCopyOfTargetCreatureUntilEndOfTurnEffect", TargetPolarity.NEUTRAL),
            entry("MakeTargetCopyOfTargetCreatureUntilEndOfTurnEffect", TargetPolarity.BENEFICIAL),
            entry("MakeTargetCreaturesCopiesOfChosenCreatureUntilEndOfTurnEffect", TargetPolarity.NEUTRAL),
            entry("GrantBasicLandTypeToTargetEffect", TargetPolarity.NEUTRAL),
            entry("GrantColorEffect", TargetPolarity.NEUTRAL),
            entry("GrantColorUntilEndOfTurnEffect", TargetPolarity.NEUTRAL),
            entry("GrantSubtypeEffect", TargetPolarity.NEUTRAL),
            entry("GrantSubtypeUntilEndOfTurnEffect", TargetPolarity.NEUTRAL),
            entry("GrantSubtypeToTargetCreatureEffect", TargetPolarity.HARMFUL),
            entry("MoveCounterFromTargetCreatureToTargetCreatureEffect", TargetPolarity.NEUTRAL),
            entry("RemoveAllCountersFromTargetPermanentEffect", TargetPolarity.NEUTRAL),
            entry("RemoveCounterFromTargetPermanentEffect", TargetPolarity.NEUTRAL),
            entry("RegisterControlLossUnattachTriggerEffect", TargetPolarity.NEUTRAL),
            entry("SearchLibraryForTargetCreatureNameToBattlefieldEffect", TargetPolarity.NEUTRAL),
            entry("SacrificeTargetThenRevealUntilTypeToBattlefieldEffect", TargetPolarity.NEUTRAL),
            entry("SetTargetColorEffect", TargetPolarity.NEUTRAL),
            entry("SetTargetPermanentNameEffect", TargetPolarity.NEUTRAL),
            entry("SetTargetPermanentSupertypeEffect", TargetPolarity.NEUTRAL),
            entry("SetCardTypesEffect", TargetPolarity.HARMFUL),
            entry("SetChosenColorForTargetCreaturesUntilEndOfTurnEffect", TargetPolarity.NEUTRAL),
            entry("BecomeColorlessUntilEndOfTurnEffect", TargetPolarity.NEUTRAL),
            entry("SwitchPowerToughnessEffect", TargetPolarity.NEUTRAL),
            entry("SuspectTargetCreatureEffect", TargetPolarity.NEUTRAL),
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

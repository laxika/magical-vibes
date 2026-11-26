package com.github.laxika.magicalvibes.ai;

import com.github.laxika.magicalvibes.model.StaticAbility;
import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Scaled;
import com.github.laxika.magicalvibes.model.effect.BecomeChosenColorsUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.ExileFaceDownPileEffect;
import com.github.laxika.magicalvibes.model.effect.GrantEffectToTargetUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.GrantTargetingRestrictionToTargetUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.PermanentSubject;
import com.github.laxika.magicalvibes.model.effect.PreventNextDamageByTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.Ability;
import com.github.laxika.magicalvibes.model.AbilityEffects;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.effect.BidLifeEffect;
import com.github.laxika.magicalvibes.model.effect.CantAttackThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.CantBlockThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreaturesByPositionEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalReplacementEffect;
import com.github.laxika.magicalvibes.model.effect.AttachSourceEquipmentToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenAttachedToTargetEffect;
import com.github.laxika.magicalvibes.model.effect.ConjureCardsEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.CreatureBoostEffect;
import com.github.laxika.magicalvibes.model.effect.DamageDealingEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.effect.EnterWithCountersEffect;
import com.github.laxika.magicalvibes.model.effect.DistributeCountersAmongTargetsEffect;
import com.github.laxika.magicalvibes.model.effect.FlipCoinWinEffect;
import com.github.laxika.magicalvibes.model.effect.FlipUntilLoseOrStopEffect;
import com.github.laxika.magicalvibes.model.effect.ExchangeControlOfTargetPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.ExploreEffect;
import com.github.laxika.magicalvibes.model.effect.GainControlOfTargetEffect;
import com.github.laxika.magicalvibes.model.effect.GrantStaticEffectToTargetEffect;
import com.github.laxika.magicalvibes.model.effect.GrantTriggeredAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.KeywordGrantingEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.UnlessPaysEffect;
import com.github.laxika.magicalvibes.model.effect.PhaseOutEffect;
import com.github.laxika.magicalvibes.model.effect.DoubleSelfPowerToughnessEffect;
import com.github.laxika.magicalvibes.model.effect.RedirectTargetCreatureDamageFromChosenSourceToTargetEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveAllCountersFromTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.MoveCounterFromSourceToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveTargetFromCombatEffect;
import com.github.laxika.magicalvibes.model.effect.PerpetuallyBoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.CardSubject;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.PerpetuallyBoostCardEffect;
import com.github.laxika.magicalvibes.model.effect.PerpetuallyGrantStaticEffectToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.PerpetuallyGrantTriggeredAbilityToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersEffect;
import com.github.laxika.magicalvibes.model.effect.PutTargetSpellOrPermanentOrGraveyardCardOnTopOrBottomOfLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.RedirectNextDamageEffect;
import com.github.laxika.magicalvibes.model.effect.RedirectYourDamageToTargetCreatureThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.RedirectRole;
import com.github.laxika.magicalvibes.model.effect.RegenerationEffect;
import com.github.laxika.magicalvibes.model.effect.RemovalEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveCountersEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveUpToCountersFromAllPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.RevealUntilCardPredicateRestOnBottomRandomEffect;
import com.github.laxika.magicalvibes.model.effect.RollD20Effect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterDelayedStepTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.effect.SetCombatRequirementThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.SkipNextUntapEffect;
import com.github.laxika.magicalvibes.model.effect.SuspectEffect;
import com.github.laxika.magicalvibes.model.effect.TapOrUntapTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TargetCreatureBecomesSubtypeUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.TargetCreatureMustAttackSourcePermanentNextTurnEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPredicate;
import com.github.laxika.magicalvibes.model.effect.TransformSelfEffect;
import com.github.laxika.magicalvibes.model.effect.TributeNotPaidEffect;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.SetBasePowerToughnessEffect;
import com.github.laxika.magicalvibes.model.effect.SetBasePowerToughnessToAmountEffect;
import com.github.laxika.magicalvibes.model.effect.PlayerSubject;
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
 * in a card's SPELL or "when this enters" abilities must classify to a non-null polarity —
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
     * Card-level polarity over the "when this enters" abilities and the spell ability, collapsed by
     * priority: removal beats damage beats other harm beats beneficial beats neutral.
     * This mirrors (and must preserve) the branch order {@code chooseTarget} routed with
     * before the consolidation. Returns null when no effect classifies.
     */
    public TargetPolarity classifyCard(GameData gameData, Card card, UUID aiPlayerId) {
        if (tradesTheTargetForAnother(card.getCastingCostsAndSpellEffects())) {
            return TargetPolarity.NEUTRAL;
        }
        TargetPolarity best = null;
        for (CardEffect effect : card.getOwnEntryEffects()) {
            best = higherPriority(best, classify(gameData, effect, aiPlayerId));
        }
        for (CardEffect effect : card.getCastingCostsAndSpellEffects()) {
            best = higherPriority(best, classify(gameData, effect, aiPlayerId));
        }
        return best;
    }

    /**
     * Group-level polarity for one multi-target group's effects, collapsed by the same
     * priority as {@link #classifyCard}. Returns null when no effect classifies.
     */
    TargetPolarity classifyGroup(GameData gameData, List<? extends CardEffect> groupEffects, UUID aiPlayerId) {
        if (groupEffects.stream().anyMatch(TargetPolarityClassifier::replacesTheTargetForItsController)) {
            return TargetPolarity.NEUTRAL;
        }
        TargetPolarity best = null;
        for (CardEffect effect : groupEffects) {
            best = higherPriority(best, classify(gameData, effect, aiPlayerId));
        }
        return best;
    }

    /** Whether a delayed ability's effect removes the permanent it is about: destroys or sacrifices it, maybe. */
    private static boolean removesItLater(CardEffect effect) {
        return switch (effect) {
            case SacrificeSelfEffect ignored -> true;
            case RemovalEffect ignored -> true;
            case ConditionalEffect conditional -> removesItLater(conditional.wrapped());
            case FlipCoinWinEffect flip -> flip.wrapped() != null && removesItLater(flip.wrapped())
                    || flip.lost() != null && removesItLater(flip.lost());
            default -> false;
        };
    }

    /**
     * Effect-level polarity. Returns null for shapes this classifier does not know —
     * the guard test keeps that set empty for the card pool's spell/ETB surface.
     */
    TargetPolarity classify(GameData gameData, CardEffect effect, UUID aiPlayerId) {
        if (effect instanceof com.github.laxika.magicalvibes.model.effect.ChooseOneAtResolutionEffect modal) {
            return classifyGroup(gameData, modal.choice().options().stream()
                    .flatMap(option -> option.effects().stream()).toList(), aiPlayerId);
        }
        // Wrappers: classify what actually happens to the target. Kicker-style replacements
        // use the base mode only — the AI never kicks (mirrors computeBaseAllowedTargets).
        if (effect instanceof ConditionalEffect conditional) {
            return classify(gameData, conditional.wrapped(), aiPlayerId);
        }
        if (effect instanceof MayEffect may) {
            return classify(gameData, may.wrapped(), aiPlayerId);
        }
        // "Conjure a duplicate of target nontoken creature you control into your hand" helps its own board; a duplicate
        // of any permanent stays the name entry's NEUTRAL.
        if (effect instanceof ConjureCardsEffect conjure
                && conjure.cards() instanceof ConjureCardsEffect.DuplicatesOf of
                && of.targetFilter() instanceof PermanentAllOfPredicate all
                && all.predicates().stream().anyMatch(PermanentControlledBySourceControllerPredicate.class::isInstance)) {
            return TargetPolarity.BENEFICIAL;
        }
        // A permanent put into a face-down pile comes back to the effect's controller.
        if (effect instanceof ExileFaceDownPileEffect pile
                && pile.pile() instanceof ExileFaceDownPileEffect.PermanentsAndTopCards) {
            return TargetPolarity.BENEFICIAL;
        }
        // "You may pay {X}. If you do / don't, …" — classify whichever branch targets a permanent
        // (Knight of the Mists: wrapped null, elseEffect destroys; Spellbomb: wrapped does work).
        if (effect instanceof UnlessPaysEffect unless) {
            TargetPolarity paid = unless.ifPaid() == null ? null : classify(gameData, unless.ifPaid(), aiPlayerId);
            TargetPolarity declined = unless.ifNotPaid() == null
                    ? null
                    : classify(gameData, unless.ifNotPaid(), aiPlayerId);
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
        if (effect instanceof RollD20Effect roll) {
            TargetPolarity best = null;
            for (CardEffect branch : new CardEffect[]{roll.zeroOrLess(), roll.oneToNine(),
                    roll.tenToNineteen(), roll.twenty()}) {
                if (branch != null) {
                    best = higherPriority(best, classify(gameData, branch, aiPlayerId));
                }
            }
            return best;
        }
        if (effect instanceof BoostTargetCreaturesByPositionEffect boosts) {
            return classifyGroup(gameData, boosts.boosts(), aiPlayerId);
        }
        // "Destroy / sacrifice it at the beginning of the next end step" about the target: the target still leaves, so
        // removal outranks a pump riding along (Blood Frenzy) and aims at the opponent; own-board uses (Hazoret's Favor,
        // Slave of Bolas's stolen creature) are kept to their boards by their target filters.
        if (effect instanceof RegisterDelayedStepTriggerEffect delayed && delayed.permanent() != null
                && delayed.permanent().declaresTarget()) {
            TargetPolarity best = null;
            for (CardEffect later : delayed.effects()) {
                best = higherPriority(best, removesItLater(later) ? TargetPolarity.HARMFUL_REMOVAL
                        : classify(gameData, later, aiPlayerId));
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
        if (effect instanceof GrantStaticEffectToTargetEffect grant) {
            return classify(gameData, grant.staticEffect(), aiPlayerId);
        }
        if (effect instanceof GrantEffectToTargetUntilEndOfTurnEffect grant) {
            if (!grant.subject().declaresTarget()) {
                return null;
            }
            // A granted static ability is as good or bad as what it says ("can't attack unless its controller pays").
            TargetPolarity granted = grant.grantedAbility() instanceof StaticAbility
                    ? classify(gameData, grant.grantedEffect(), aiPlayerId) : null;
            return granted != null ? granted : TargetPolarity.BENEFICIAL;
        }
        if (effect instanceof PerpetuallyGrantStaticEffectToTargetCreatureEffect grant) {
            return classify(gameData, grant.staticEffect(), aiPlayerId);
        }
        if (effect instanceof PerpetuallyGrantTriggeredAbilityToTargetCreatureEffect grant) {
            if (!grant.subject().declaresTarget()) {
                return null;
            }
            return classifyGrantedAbility(grant.grantedAbility());
        }
        if (effect instanceof GrantTriggeredAbilityEffect grant) {
            return classifyGrantedAbility(grant.grantedAbility());
        }
        if (effect instanceof EnterWithCountersEffect counters) {
            if (counters.type() == CounterType.MINUS_ONE_MINUS_ONE) {
                return TargetPolarity.HARMFUL;
            }
            return counters.type() == CounterType.PLUS_ONE_PLUS_ONE || counters.type() == CounterType.SHIELD
                    ? TargetPolarity.BENEFICIAL
                    : TargetPolarity.NEUTRAL;
        }
        if (effect instanceof TributeNotPaidEffect tributeNotPaid) {
            return classify(gameData, tributeNotPaid.wrapped(), aiPlayerId);
        }

        // Attaching an Equipment helps the creature it lands on; the absorbed "attach that Equipment to target creature"
        // form (an Equipment that triggered) had no polarity.
        if (effect instanceof AttachSourceEquipmentToTargetCreatureEffect attach) {
            return attach.equipment() instanceof PermanentSubject.Triggering ? null : TargetPolarity.BENEFICIAL;
        }
        // A token copy of an Aura's or Equipment's host or of a replacement's creature (formerly their own records) had no
        // polarity; the target and triggering forms kept the name entry's NEUTRAL.
        if (effect instanceof CreateTokenCopyOfTargetPermanentEffect copy) {
            return switch (copy.subject()) {
                case PermanentSubject.Attached ignored -> null;
                case PermanentSubject.Named ignored -> null;
                // The copies of a player's creatures, and of the returned creature, had no polarity either.
                case PermanentSubject.ControlledBy ignored -> null;
                case PermanentSubject.Returned ignored -> null;
                case PermanentSubject.Except ignored -> null;
                default -> TargetPolarity.NEUTRAL;
            };
        }
        if (effect instanceof CreateTokenAttachedToTargetEffect attachToken) {
            if (attachToken.subject() instanceof PermanentSubject.Source
                    || attachToken.subject() instanceof PermanentSubject.Returned
                    || attachToken.subject() instanceof PermanentSubject.ReturnedThisResolution) {
                return null;
            }
            return attachToken.targetControllerRelation() == PlayerRelation.OPPONENT
                    ? TargetPolarity.HARMFUL
                    : TargetPolarity.BENEFICIAL;
        }

        if (effect instanceof PutTargetSpellOrPermanentOrGraveyardCardOnTopOrBottomOfLibraryEffect) {
            return TargetPolarity.HARMFUL_REMOVAL;
        }

        // A search whose filter reads a target permanent ("cards with the same name as target creature", Pack Hunt) finds
        // cards of the AI's own library, so it names one of the AI's own creatures.
        if (effect instanceof com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect search
                && search.targetSpec().admits(TargetPredicate.Kind.PERMANENT)) {
            return TargetPolarity.BENEFICIAL;
        }

        // "Airbend all other creatures" after "up to one target creature" (Avatar's Wrath): the target is the one spared,
        // the AI's own creature.
        if (effect instanceof com.github.laxika.magicalvibes.model.effect.AirbendTargetPermanentEffect airbend
                && airbend.subject() instanceof PermanentSubject.Except except
                && except.excluded() instanceof PermanentSubject.TargetDeclaredElsewhere) {
            return TargetPolarity.BENEFICIAL;
        }

        // A wipe that reads the target's power as a threshold and spares the target itself (Fell the Mighty, Mandate of
        // Abaddon) aims at no board.
        if (effect instanceof com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect wipe
                && wipe.referencedTarget() != null) {
            return TargetPolarity.NEUTRAL;
        }

        // "Target creature's controller sacrifices it" (Mercy Killing, Sarkhan the Mad, Arcum Dagsson): an edict aimed at
        // the target, which leaves the battlefield. "That player sacrifices one of them" after a choice among the targets
        // (Incriminate, Barrin's Spite, Retribution) is one too.
        if (effect instanceof SacrificeSelfEffect sacrifice
                && (sacrifice.subject().declaresTarget() || sacrifice.subject() instanceof PermanentSubject.ThisWay)
                && sacrifice.sacrificer() instanceof PlayerSubject.ControllerOf) {
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
        // Damage another permanent deals: the bite of two target groups is harmful (Soul's Fire's any target too);
        // a target creature dealing damage to each other creature is harmful when they are its own controller's
        // (Alpha Brawl), else the controller's own creature, beneficial; a target creature dealing damage to each
        // opponent aims at the AI's own creature; a creature damaging itself or a set's damage to a target creature is
        // creature damage, the Aura's creature as it triggered and damage to this creature aim at no permanent.
        if (effect instanceof DealDamageEffect dealt && dealt.dealers() != null) {
            if (dealt.readsTwoTargetGroups()) {
                return TargetPolarity.HARMFUL;
            }
            if (dealt.recipient() == DealDamageEffect.Recipient.EACH_PERMANENT) {
                return dealt.targetSpec().harmful() ? TargetPolarity.HARMFUL : TargetPolarity.BENEFICIAL;
            }
            if (dealt.recipient() == DealDamageEffect.Recipient.PLAYERS
                    && dealt.players() instanceof PlayerSubject.Each) {
                return null;
            }
            if (dealt.recipient() == DealDamageEffect.Recipient.CREATURE) {
                return dealt.subject() instanceof PermanentSubject.Target
                        && !(dealt.dealers() instanceof PermanentSubject.AttachedAtTrigger)
                        ? TargetPolarity.HARMFUL_DAMAGE
                        : null;
            }
        }
        // "Deals N damage to each of up to N targets" damages each target.
        if (effect instanceof DealDamageEffect eachTarget
                && eachTarget.recipient() == DealDamageEffect.Recipient.EACH_TARGET) {
            return TargetPolarity.HARMFUL_DAMAGE;
        }
        if (effect instanceof DamageDealingEffect damage) {
            if (!effect.targetSpec().admits(TargetPredicate.Kind.PERMANENT)) {
                return null;
            }
            return damage.canDamageCreatures() ? TargetPolarity.HARMFUL_DAMAGE : TargetPolarity.HARMFUL;
        }

        // "The next time target creature would deal damage this turn, prevent that damage" aims at an opponent's
        // creature (Awe Strike, Dazzling Reflection); "it deals double that damage instead" at your own.
        if (effect instanceof PreventNextDamageByTargetCreatureEffect nextDamage) {
            return nextDamage.doubling() ? TargetPolarity.BENEFICIAL : TargetPolarity.HARMFUL;
        }

        // Tap/untap family: the scope or subject decides whether a permanent is targeted at all.
        if (effect instanceof TapPermanentsEffect tap) {
            return tap.subject() instanceof PermanentSubject.Target ? TargetPolarity.HARMFUL : null;
        }
        if (effect instanceof SkipNextUntapEffect skip) {
            return skip.subject() instanceof PermanentSubject.Target ? TargetPolarity.HARMFUL : null;
        }
        if (effect instanceof CantAttackThisTurnEffect cantAttack) {
            return cantAttack.subject() instanceof PermanentSubject.Target ? TargetPolarity.HARMFUL : null;
        }
        if (effect instanceof CantBlockThisTurnEffect cantBlock) {
            // A set whose players come from a target shuts down that player's blockers: the target player's (Ember
            // Gale, Concussive Bolt) or the rest of the target creature's controller's (Mark for Death), so it aims at
            // the opponent just like a target creature does.
            return cantBlock.subject() instanceof PermanentSubject.Target
                    || cantBlock.subject() instanceof PermanentSubject.ControlledBy set && namesTargetsPlayer(set.players())
                    ? TargetPolarity.HARMFUL
                    : null;
        }
        if (effect instanceof UntapPermanentsEffect untap) {
            return untap.subject() instanceof PermanentSubject.Target || untap.subject() instanceof PermanentSubject.AllTargets
                    || untap.subject() instanceof PermanentSubject.WithCreaturesSharingAColor sharing && sharing.declaresTarget()
                    ? TargetPolarity.BENEFICIAL
                    : null;
        }
        if (effect instanceof TapOrUntapTargetPermanentEffect) {
            return TargetPolarity.NEUTRAL;
        }
        // A copy made by or of a target ("target creature becomes a copy of another target creature") is as good as
        // bad: it depends on the two creatures, not on whose they are.
        if (effect instanceof com.github.laxika.magicalvibes.model.effect.BecomeCopyOfSubjectEffect copy
                && copy.targetSpec().admits(TargetPredicate.Kind.PERMANENT)) {
            return TargetPolarity.NEUTRAL;
        }

        // Phasing: only the targeted form aims at a permanent the AI chose (Reality Ripple, Vision
        // Charm) — the source and attached forms carry no target and no polarity.
        if (effect instanceof PhaseOutEffect phaseOut) {
            return phaseOut.subject() instanceof PermanentSubject.Target ? TargetPolarity.HARMFUL_REMOVAL : null;
        }
        // Remove from combat: the target form and the triggering form (Melee's delayed ability) kept the name entry's
        // HARMFUL; the source and enchanted-creature forms (formerly their own records) have no polarity.
        if (effect instanceof RemoveTargetFromCombatEffect removal) {
            return removal.subject() instanceof PermanentSubject.Target
                    || removal.subject() instanceof PermanentSubject.Triggering ? TargetPolarity.HARMFUL : null;
        }

        // Redirect-next-damage: the target is either the object the redirected damage lands on
        // (Zhalfirin Crusader — aim at the opponent) or the object being shielded (Martyrdom,
        // Hazduhr the Abbot — aim at the AI's own board).
        if (effect instanceof RedirectNextDamageEffect redirect) {
            return redirect.destinationRole() == RedirectRole.TARGET
                    ? TargetPolarity.HARMFUL_DAMAGE
                    : TargetPolarity.BENEFICIAL;
        }

        // "All damage that would be dealt to you is dealt to [target] instead": a target destination taking only your
        // damage is an attacker (Turn the Tables); one taking your creatures' damage too is your own (Heroic Sacrifice).
        // A targeted damage source (Shimian Night Stalker) keeps the fallback.
        if (effect instanceof RedirectYourDamageToTargetCreatureThisTurnEffect redirect) {
            if (!redirect.destination().declaresTarget()) {
                return null;
            }
            return redirect.redirectsOnlyYourDamage() ? TargetPolarity.HARMFUL_DAMAGE : TargetPolarity.BENEFICIAL;
        }

        // Conjured Currency's source-mode exchange hands the source away but takes the target, so
        // the target should be an opponent's permanent.
        if (effect instanceof ExchangeControlOfTargetPermanentsEffect) {
            return TargetPolarity.HARMFUL;
        }

        // "Each player may bid life for control of target creature" (Illicit Auction): the controller opens the bidding
        // and the AI never outbids, so the target is a creature it would take from an opponent.
        if (effect instanceof BidLifeEffect bid && bid.prize() == BidLifeEffect.Prize.CONTROL_OF_TARGET) {
            return TargetPolarity.HARMFUL;
        }

        // "Target player gains control of up to three target creatures" (Domineering Will) may hand the AI's own
        // creatures to an ally or take an opponent's: no side is implied.
        if (effect instanceof GainControlOfTargetEffect gain && !(gain.gainingPlayer() instanceof PlayerSubject.You)) {
            return TargetPolarity.NEUTRAL;
        }
        if (effect instanceof TargetCreatureMustAttackSourcePermanentNextTurnEffect
                || effect instanceof GainControlOfTargetEffect) {
            return TargetPolarity.HARMFUL;
        }

        // One-shot combat requirements split by who benefits: forcing a creature to attack or block is a
        // punisher aimed at an opponent's creature, while the "must BE blocked" lures help the AI's own.
        if (effect instanceof SetCombatRequirementThisTurnEffect combatRequirement) {
            return switch (combatRequirement.requirement()) {
                // "Target creature attacks target opponent" (Dulcet Sirens, Ravener) is aimed at the AI's own creature.
                case MUST_ATTACK -> combatRequirement.attacked() != null && combatRequirement.attacked().declaresTarget()
                        ? TargetPolarity.BENEFICIAL : TargetPolarity.HARMFUL;
                case MUST_BLOCK, MUST_ATTACK_OR_BLOCK -> TargetPolarity.HARMFUL;
                case MUST_BE_BLOCKED, MUST_BE_BLOCKED_BY_ALL -> TargetPolarity.BENEFICIAL;
            };
        }

        // Counters: -1/-1 hurts, +1/+1 helps, anything else carries no direction. Counters on the source go on no
        // target: a target such an ability declares (a player it counts for, a permanent it reads) has no direction.
        if (effect instanceof PutCountersEffect counter) {
            if (counter.subject() instanceof PermanentSubject.Source) {
                return TargetPolarity.NEUTRAL;
            }
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
        // Removing some counters from a target: stripping -1/-1 counters helps it, any other kind (loyalty, +1/+1,
        // lore, time, defense) is aimed at the opponent's permanent or the opponent; the non-target forms have no
        // polarity.
        if (effect instanceof RemoveUpToCountersFromAllPermanentsEffect removeSome) {
            if (removeSome.targetSpec().equals(com.github.laxika.magicalvibes.model.effect.TargetSpec.NONE)) {
                return null;
            }
            return removedCountersPolarity(removeSome.counterType());
        }
        // The same for "remove N [kind] (or all [kind]) counters from target permanent"; a counter of any kind, whose
        // kind its controller picks as it resolves, has no direction. From this permanent or a named one: no polarity.
        if (effect instanceof RemoveCountersEffect remove) {
            if (!(remove.subject() instanceof PermanentSubject.Target)) {
                return null;
            }
            return remove.counterType() == CounterType.ANY
                    ? TargetPolarity.NEUTRAL : removedCountersPolarity(remove.counterType());
        }

        // A move between two targets or from a set onto a target swings both ways; one to or from this permanent
        // has no polarity.
        if (effect instanceof MoveCounterFromSourceToTargetCreatureEffect move) {
            return move.from() instanceof PermanentSubject.Source || move.to() instanceof PermanentSubject.Source
                    ? null : TargetPolarity.NEUTRAL;
        }

        if (effect instanceof DoubleSelfPowerToughnessEffect doubling) {
            return doubling.subject().declaresTarget() ? TargetPolarity.BENEFICIAL : null;
        }
        // Stripping every counter: from a creature it is a debuff (old target-creature record), from any permanent
        // or a target player it swings both ways; the non-target forms have no polarity.
        if (effect instanceof RemoveAllCountersFromTargetPermanentEffect removeEvery) {
            if (removeEvery.targetSpec().equals(com.github.laxika.magicalvibes.model.effect.TargetSpec.NONE)) {
                return null;
            }
            return removeEvery.targetFilter() != null ? TargetPolarity.HARMFUL : TargetPolarity.NEUTRAL;
        }

        // Targeted P/T boosts are sign-aware: any negative component makes the shape a
        // debuff. Dynamic amounts evaluate in estimation context, matching SpellEvaluator; a
        // "-1/-1 for each ..." amount is a debuff even when it counts nothing yet.
        if (CreatureBoostEffect.targetedBoost(effect) instanceof CreatureBoostEffect boost) {
            AmountContext ctx = AmountContext.forEstimation(aiPlayerId);
            boolean negative = amountEvaluationService.evaluate(gameData, boost.powerBoost(), ctx) < 0
                    || amountEvaluationService.evaluate(gameData, boost.toughnessBoost(), ctx) < 0
                    || negativePerCount(boost.powerBoost()) || negativePerCount(boost.toughnessBoost());
            return negative ? TargetPolarity.HARMFUL : TargetPolarity.BENEFICIAL;
        }
        if (effect instanceof PerpetuallyBoostTargetCreatureEffect boost) {
            return boost.powerBoost() < 0 || boost.toughnessBoost() < 0
                    ? TargetPolarity.HARMFUL : TargetPolarity.BENEFICIAL;
        }

        if (effect instanceof GrantTargetingRestrictionToTargetUntilEndOfTurnEffect grant) {
            return grant.subject().declaresTarget() ? TargetPolarity.BENEFICIAL : null;
        }
        if (effect instanceof RedirectTargetCreatureDamageFromChosenSourceToTargetEffect redirect) {
            return redirect.redirectTo().declaresTarget() ? TargetPolarity.BENEFICIAL : null;
        }
        if (effect instanceof BecomeChosenColorsUntilEndOfTurnEffect becomes) {
            return becomes.subject().declaresTarget() ? TargetPolarity.NEUTRAL : null;
        }
        if (effect instanceof TargetCreatureBecomesSubtypeUntilEndOfTurnEffect becomes) {
            return becomes.subject() instanceof PermanentSubject.Source ? null : TargetPolarity.NEUTRAL;
        }
        if (effect instanceof TransformSelfEffect transform) {
            return transform.subject() instanceof PermanentSubject.Source ? null : TargetPolarity.BENEFICIAL;
        }

        if (effect instanceof RegenerationEffect) {
            return TargetPolarity.BENEFICIAL;
        }
        if (effect instanceof ExploreEffect explore) {
            return explore.targeted() ? TargetPolarity.BENEFICIAL : null;
        }
        if (effect instanceof KeywordGrantingEffect grant) {
            return GrantSubjectShapes.grantsToItsTarget(grant)
                    || GrantSubjectShapes.namesAttachedCreature(grant.subject())
                    ? TargetPolarity.BENEFICIAL
                    : null;
        }
        if (effect instanceof SuspectEffect suspect) {
            return GrantSubjectShapes.namesTarget(suspect.subject())
                    ? TargetPolarity.NEUTRAL : null;
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

        // A base-P/T setter over amounts (Fractalize's X plus 1, Exuberant Wolfbear's power) has no direction of its
        // own: the fallback stays, and the card's other effects decide.
        if (effect instanceof SetBasePowerToughnessToAmountEffect) {
            return TargetPolarity.NEUTRAL;
        }

        return FIXED_BY_CLASS_NAME.get(effect.getClass().getSimpleName());
    }

    /** "-1/-1 for each [counted thing]": a count scaled by a negative factor. */
    private static boolean negativePerCount(DynamicAmount amount) {
        return amount instanceof Scaled scaled && scaled.factor() < 0;
    }

    /**
     * Polarity of a triggered ability granted to the target, read from what the ability does to the creature that
     * has it: a perpetual boost of its sign, or damage to its controller (Traumatic Prank's upkeep ping). An ability
     * whose effects carry no direction (Duelist's Flame's combat-damage card advantage, Town-Razer Tyrant's
     * sacrifice-or-damage) is NEUTRAL, and the card's other effects decide the target.
     */
    /**
     * "Target permanent's controller sacrifices it, then reveals ... until [a card of its type], puts that card onto
     * the battlefield" (Shape Anew, Reweave): an edict that hands its controller another permanent, as good as bad.
     */
    private static boolean tradesTheTargetForAnother(List<? extends CardEffect> effects) {
        return effects.stream().anyMatch(effect -> effect instanceof SacrificeSelfEffect sacrifice
                && sacrifice.subject().declaresTarget() && sacrifice.sacrificer() instanceof PlayerSubject.ControllerOf)
                && effects.stream().anyMatch(TargetPolarityClassifier::replacesTheTargetForItsController);
    }

    /**
     * "Its controller reveals ... until a creature card, puts it onto the battlefield" (Polymorph, Shape Anew), also
     * after "if the player does" (Reweave): as good as bad.
     */
    private static boolean replacesTheTargetForItsController(CardEffect effect) {
        if (effect instanceof ConditionalEffect conditional) {
            return replacesTheTargetForItsController(conditional.wrapped());
        }
        return effect instanceof RevealUntilCardPredicateRestOnBottomRandomEffect reveal
                && (reveal.revealedBy()
                instanceof RevealUntilCardPredicateRestOnBottomRandomEffect.ControllerOfEachExiledThisWay
                || reveal.revealedBy() instanceof RevealUntilCardPredicateRestOnBottomRandomEffect.Players(var players)
                && players instanceof PlayerSubject.ControllerOf);
    }

    private static TargetPolarity classifyGrantedAbility(Ability grantedAbility) {
        if (grantedAbility == null) {
            return TargetPolarity.NEUTRAL;
        }
        TargetPolarity best = null;
        for (CardEffect granted : AbilityEffects.effectsOf(grantedAbility)) {
            best = higherPriority(best, classifyGrantedEffect(granted));
        }
        return best == null ? TargetPolarity.NEUTRAL : best;
    }

    private static TargetPolarity classifyGrantedEffect(CardEffect granted) {
        if (granted instanceof SequenceEffect sequence) {
            TargetPolarity best = null;
            for (CardEffect step : sequence.steps()) {
                best = higherPriority(best, classifyGrantedEffect(step));
            }
            return best;
        }
        if (granted instanceof PerpetuallyBoostCardEffect boost
                && (boost.cards() == null || boost.cards() instanceof CardSubject.SourceObjectCard)
                && boost.powerBoost() instanceof Fixed power && boost.toughnessBoost() instanceof Fixed toughness) {
            return power.value() < 0 || toughness.value() < 0
                    ? TargetPolarity.HARMFUL : TargetPolarity.BENEFICIAL;
        }
        if (granted instanceof DealDamageEffect damage && damage.players() instanceof PlayerSubject.You) {
            return TargetPolarity.HARMFUL;
        }
        return null;
    }

    /** Removing counters of {@code kind} from a target: -1/-1 counters help it, any other kind hurts it. */
    private static TargetPolarity removedCountersPolarity(CounterType kind) {
        return kind == CounterType.MINUS_ONE_MINUS_ONE ? TargetPolarity.BENEFICIAL : TargetPolarity.HARMFUL;
    }

    /**
     * Fixed polarity for shapes whose direction never depends on instance state. Keyed by
     * simple class name to keep the table readable; this trades compile-time rename safety
     * for legibility, and {@code TargetPolarityGuardTest} makes the trade safe — a renamed
     * or newly added shape with no live entry here fails that test loudly.
     */
    private static final Map<String, TargetPolarity> FIXED_BY_CLASS_NAME = Map.ofEntries(
            // The target leaves the battlefield (or the board position it holds).
            entry("DestroyTwoTargetCreaturesIfSameColorsEffect", TargetPolarity.HARMFUL_REMOVAL),
            entry("DisorientingChoiceEffect", TargetPolarity.HARMFUL_REMOVAL),
            entry("PhaseOutTargetCreatureUntilSourceLeavesEffect", TargetPolarity.HARMFUL_REMOVAL),
            entry("DestroyTargetPermanentThenEffect", TargetPolarity.HARMFUL_REMOVAL),
            entry("ExileTargetPermanentAndImprintEffect", TargetPolarity.HARMFUL_REMOVAL),
            entry("ExileTargetPermanentAndTrackWithSourceEffect", TargetPolarity.HARMFUL_REMOVAL),
            entry("ExileTargetPermanentThenEffect", TargetPolarity.HARMFUL_REMOVAL),
            entry("ExileTargetPermanentUntilSourceLeavesEffect", TargetPolarity.HARMFUL_REMOVAL),
            entry("ExileTargetPermanentUntilOpponentBecomesMonarchEffect", TargetPolarity.HARMFUL_REMOVAL),
            entry("ExileTargetCreatureAndCopyEnchantedCreatureEffect", TargetPolarity.HARMFUL_REMOVAL),
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
            entry("ReturnTargetPermanentToHandThenEffect", TargetPolarity.HARMFUL_REMOVAL),
            entry("ShuffleTargetPermanentIntoLibraryEffect", TargetPolarity.HARMFUL_REMOVAL),
            entry("EquipoiseEffect", TargetPolarity.HARMFUL_REMOVAL),
            entry("WintersChillEffect", TargetPolarity.HARMFUL_REMOVAL),

            // The target (or a permanent tied to it) takes damage.
            entry("DealDamageToTargetCreaturesThenCreateTokensEffect", TargetPolarity.HARMFUL_DAMAGE),
            entry("DealDamageToTargetAndTheirCreaturesEffect", TargetPolarity.HARMFUL_DAMAGE),
            entry("DealDividedDamageEffect", TargetPolarity.HARMFUL_DAMAGE),
            entry("FlipUntilLoseOrStopEffect", TargetPolarity.HARMFUL_DAMAGE),
            entry("WhammyBurnEffect", TargetPolarity.HARMFUL_DAMAGE),
            entry("DiscardRandomCardDealDiscardedPowerToTargetPlayerOrPlaneswalkerEffect", TargetPolarity.HARMFUL_DAMAGE),
            // Divine Deflection prevents damage to its controller, but the target is who the
            // prevented damage is then dealt to.
            entry("PreventNextDamageAndRedirectEffect", TargetPolarity.HARMFUL_DAMAGE),
            entry("RedirectNextDamageFromChosenSourceToSelfEffect", TargetPolarity.HARMFUL_DAMAGE),
            entry("RedirectNextDamageFromTargetToAnotherTargetEffect", TargetPolarity.HARMFUL_DAMAGE),
            entry("ChannelHarmEffect", TargetPolarity.HARMFUL_DAMAGE),
            entry("RevealUntilNonlandBottomThenDealManaValueDamageEffect", TargetPolarity.HARMFUL_DAMAGE),

            // Other harm: fights, steals, strips, debuffs, forced blocks.
            entry("DestroyAttachmentsOnTargetCreatureEffect", TargetPolarity.HARMFUL),
            entry("EnchantedCreatureFightsTargetCreatureEffect", TargetPolarity.HARMFUL),
            entry("EnteringCreatureFightsTargetCreatureEffect", TargetPolarity.HARMFUL),
            entry("FightTargetsEffect", TargetPolarity.HARMFUL),
            entry("SourceFightsTargetCreatureEffect", TargetPolarity.HARMFUL),
            // Perpetually granted by Traumatic Prank.
            entry("CantBlockEffect", TargetPolarity.HARMFUL),
            entry("GainControlOfTargetAuraEffect", TargetPolarity.HARMFUL),
            entry("GainControlOfTargetUntilRansomEffect", TargetPolarity.HARMFUL),
            entry("LockTargetPermanentEffect", TargetPolarity.HARMFUL),
            entry("LoseAllCreatureTypesEffect", TargetPolarity.HARMFUL),
            entry("LosesAllAbilitiesEffect", TargetPolarity.HARMFUL),
            entry("BecomeColorlessEffect", TargetPolarity.HARMFUL),
            entry("MarkTargetCreatureExileInsteadOfDieThisTurnEffect", TargetPolarity.HARMFUL),
            entry("MakeTargetAttackingCreatureBlockedEffect", TargetPolarity.HARMFUL),
            entry("MustBlockSourceEffect", TargetPolarity.HARMFUL),
            entry("MustBlockTargetCreatureEffect", TargetPolarity.HARMFUL),
            entry("MustAttackEffect", TargetPolarity.HARMFUL),
            entry("GoadTargetCreatureUntilNextTurnEffect", TargetPolarity.HARMFUL),
            entry("HuntedByTheFamilyEffect", TargetPolarity.HARMFUL),
            entry("PreventTargetCreatureRegenerationThisTurnEffect", TargetPolarity.HARMFUL),
            entry("RemoveKeywordEffect", TargetPolarity.HARMFUL),
            entry("TargetCreaturesDealToughnessDamageToEachOtherEffect", TargetPolarity.HARMFUL_DAMAGE),
            entry("UnattachEquipmentFromTargetPermanentsEffect", TargetPolarity.HARMFUL),
            entry("PutCounterOnEitherTargetPermanentEffect", TargetPolarity.HARMFUL),

            // Arcbond's watched creature is a strategic choice: either player's creature may be
            // the best center for the later symmetric damage event.
            entry("RegisterGlobalTriggeredAbilityAboutTargetUntilEndOfTurnEffect", TargetPolarity.NEUTRAL),
            // The delayed combat-damage reward belongs to the effect's controller, but either
            // player's creature can be the strategically useful watched source.
            entry("RegisterDelayedWatchedCreaturesCombatDamageEffect", TargetPolarity.NEUTRAL),
            // Feint can save either an attacker or its blockers depending on the combat state.
            entry("TapAndPreventCombatDamageByTargetAndBlockersEffect", TargetPolarity.NEUTRAL),
            entry("TurnTargetCreatureFaceDownEffect", TargetPolarity.NEUTRAL),
            entry("TurnSourceFaceUpEffect", TargetPolarity.NEUTRAL),

            // The target's side comes out ahead: pumps, shields, blinks, lure, animation.
            entry("DoubleDamageFromTargetPermanentThisTurnEffect", TargetPolarity.BENEFICIAL),
            entry("AnimatePermanentsEffect", TargetPolarity.BENEFICIAL),
            entry("EarthbendTargetLandEffect", TargetPolarity.BENEFICIAL),
            entry("EarthbendTargetLandThenFightEffect", TargetPolarity.BENEFICIAL),
            entry("AttachTargetEquipmentsToTargetCreatureThenEffect", TargetPolarity.BENEFICIAL),
            entry("AttachTargetToSourcePermanentEffect", TargetPolarity.BENEFICIAL),
            entry("DestroyCreaturesBlockedByTargetWallThenReturnFromGraveyardEffect", TargetPolarity.BENEFICIAL),
            entry("PreventAllDamageToTargetFromColorlessSourcesEffect", TargetPolarity.BENEFICIAL),
            entry("DoubleCountersOnTargetPermanentEffect", TargetPolarity.BENEFICIAL),
            entry("DoublePlusOneCountersOnTargetCreatureEffect", TargetPolarity.BENEFICIAL),
            entry("DoubleTargetCreaturePowerEffect", TargetPolarity.BENEFICIAL),
            entry("DrawDiscardAndConniveEffect", TargetPolarity.BENEFICIAL),
            entry("PhaseOutChosenTargetCreaturesEffect", TargetPolarity.BENEFICIAL),
            entry("FlickerEffect", TargetPolarity.BENEFICIAL),
            entry("ExileTargetPermanentThenDiscardAndReturnToBattlefieldEffect", TargetPolarity.BENEFICIAL),
            entry("GrantFlyingToTargetCreatureOrPlayerEffect", TargetPolarity.BENEFICIAL),
            entry("MakeTargetCreatureCommanderEffect", TargetPolarity.BENEFICIAL),
            // Predator's Rapport: targets a creature you control and only reads its stats.
            entry("GainLifeEqualToTargetCreatureStatEffect", TargetPolarity.BENEFICIAL),
            // Chandra's Ignition: the target is a creature you control and is only the damage
            // source — it takes no damage itself, so the AI should aim at its own board.
            entry("TapTargetThenEffect", TargetPolarity.BENEFICIAL),
            entry("GrantActivatedAbilityEffect", TargetPolarity.BENEFICIAL),
            entry("CanBlockAnyNumberOfCreaturesUntilEndOfTurnEffect", TargetPolarity.BENEFICIAL),
            entry("CantBeBlockedEffect", TargetPolarity.BENEFICIAL),
            entry("GrantAdditionalBlockToTargetUntilEndOfTurnEffect", TargetPolarity.BENEFICIAL),
            entry("GrantChosenKeywordEffect", TargetPolarity.BENEFICIAL),
            entry("GrantEffectToTargetEffect", TargetPolarity.BENEFICIAL),
            entry("GrantProtectionChoiceUntilEndOfTurnEffect", TargetPolarity.BENEFICIAL),
            entry("ProtectionFromColorsEffect", TargetPolarity.BENEFICIAL),
            entry("PerpetuallyGrantTriggeredAbilityToTargetCreatureOrGraveyardCardEffect", TargetPolarity.BENEFICIAL),
            entry("GrantProtectionFromCardTypeUntilEndOfTurnEffect", TargetPolarity.BENEFICIAL),
            entry("GrantProtectionFromOpponentCreaturesUntilEndOfTurnEffect", TargetPolarity.BENEFICIAL),
            entry("GrantProtectionFromOpponentsUntilEndOfTurnEffect", TargetPolarity.BENEFICIAL),
            entry("GuardianAngelPermissionEffect", TargetPolarity.BENEFICIAL),
            entry("MakeCreatureUnblockableEffect", TargetPolarity.BENEFICIAL),
            entry("MakeCreatureBlockableOnlyByFilterThisTurnEffect", TargetPolarity.BENEFICIAL),
            entry("TapCombatOpponentsOfTargetAtEndOfCombatEffect", TargetPolarity.BENEFICIAL),
            entry("PreventDamageEffect", TargetPolarity.BENEFICIAL),
            entry("PreventDamageFromChosenSourceAndRedirectToAnyTargetEffect", TargetPolarity.BENEFICIAL),
            entry("PreventDamageToTargetCreatureFromTargetingSpellOrAbilityEffect", TargetPolarity.BENEFICIAL),
            entry("PreventDamageToTargetFromChosenSourceEffect", TargetPolarity.BENEFICIAL),
            entry("PreventDividedDamageEffect", TargetPolarity.BENEFICIAL),
            entry("PreventNextDamageToTargetAndAddPlusOnePlusOneCountersEffect", TargetPolarity.BENEFICIAL),
            entry("PreventNextDamageToTargetAndAddToughnessCountersEffect", TargetPolarity.BENEFICIAL),
            entry("ReturnTargetCardOnDeathThisTurnEffect", TargetPolarity.BENEFICIAL),

            // Mimeofacture copies a card named after the opposing permanent it targets.
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
            entry("BalduvianWarlordEffect", TargetPolarity.NEUTRAL),
            entry("ReselectAttackingCreatureAttackTargetEffect", TargetPolarity.NEUTRAL),
            entry("TargetLandBecomesBasicLandTypeUntilSourceLeavesEffect", TargetPolarity.NEUTRAL),
            entry("ExchangeTextBoxesEffect", TargetPolarity.NEUTRAL),
            entry("ExchangeControlOfPermanentAndSpellEffect", TargetPolarity.NEUTRAL),
            entry("ShuffleTargetPermanentsThenEachControllerMayCastEffect", TargetPolarity.NEUTRAL),
            entry("MustBlockEachAttackingCreatureThisTurnEffect", TargetPolarity.NEUTRAL),
            entry("ChangeColorTextEffect", TargetPolarity.NEUTRAL),
            entry("ChooseOneForTargetPermanentEffect", TargetPolarity.NEUTRAL),
            entry("CreateTokenCopyAndLinkToSourceEffect", TargetPolarity.NEUTRAL),
            entry("RegisterMysticReflectionEffect", TargetPolarity.NEUTRAL),
            entry("EachControlledPermanentBecomesCopyOfTargetNonAuraPermanentEffect", TargetPolarity.NEUTRAL),
            entry("EachOtherPermanentMatchingPredicateBecomesCopyOfTargetPermanentUntilEndOfTurnEffect", TargetPolarity.NEUTRAL),
            entry("EachControlledLandOfChosenNonbasicTypeBecomesCopyOfTargetCreatureUntilEndOfTurnEffect", TargetPolarity.NEUTRAL),
            entry("GrantBasicLandTypeToTargetEffect", TargetPolarity.NEUTRAL),
            entry("GrantColorEffect", TargetPolarity.NEUTRAL),
            entry("GrantColorUntilEndOfTurnEffect", TargetPolarity.NEUTRAL),
            entry("GrantSubtypeEffect", TargetPolarity.NEUTRAL),
            entry("GrantSubtypeUntilEndOfTurnEffect", TargetPolarity.NEUTRAL),
            entry("GrantSubtypeToTargetCreatureEffect", TargetPolarity.HARMFUL),
            entry("RegisterControlLossUnattachTriggerEffect", TargetPolarity.NEUTRAL),
            entry("SearchLibraryForTargetCreatureNameToBattlefieldEffect", TargetPolarity.NEUTRAL),
            // Reality Scramble: a permanent its caster owns, traded for a random one sharing a card type.
            entry("PutTargetOnBottomThenRevealUntilSharedCardTypeToBattlefieldRestOnBottomRandomEffect",
                    TargetPolarity.NEUTRAL),
            // Fateful Handoff: the caster's own artifact or creature is handed away for cards.
            entry("TargetPlayerGainsControlOfSourceCreatureEffect", TargetPolarity.NEUTRAL),
            // Fallaji Antiquarian, Smog Smasher: a duplicate of any artifact or creature, best aimed at the strongest one.
            entry("ConjureCardsEffect", TargetPolarity.NEUTRAL),
            entry("GrantAllCreatureTypesToOwnCreaturesEffect", TargetPolarity.NEUTRAL),
            entry("SetTargetColorEffect", TargetPolarity.NEUTRAL),
            entry("SetNameEffect", TargetPolarity.NEUTRAL),
            entry("SetTargetPermanentSupertypeEffect", TargetPolarity.NEUTRAL),
            entry("SetCardTypesEffect", TargetPolarity.HARMFUL),
            entry("BecomeColorlessUntilEndOfTurnEffect", TargetPolarity.NEUTRAL),
            entry("SwitchPowerToughnessEffect", TargetPolarity.NEUTRAL)
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

    /** Whether {@code players} is a target player, or the controller of a target, alone or together. */
    private static boolean namesTargetsPlayer(PlayerSubject players) {
        return switch (players) {
            case PlayerSubject.TargetPlayer ignored -> true;
            case PlayerSubject.ControllerOf controllerOf -> controllerOf.permanent() instanceof PermanentSubject.Target;
            case PlayerSubject.AllOf allOf -> allOf.subjects().stream()
                    .anyMatch(TargetPolarityClassifier::namesTargetsPlayer);
            default -> false;
        };
    }
}

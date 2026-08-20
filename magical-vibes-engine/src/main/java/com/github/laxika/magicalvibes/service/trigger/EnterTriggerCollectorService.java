package com.github.laxika.magicalvibes.service.trigger;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.EffectResolution;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.amount.ChosenPermanentPower;
import com.github.laxika.magicalvibes.model.amount.SourcePower;
import com.github.laxika.magicalvibes.model.effect.AttachSourceAuraToEnteringCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.AttachSourceAuraToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.AttachSourceEquipmentToEnteringCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.AttachSourceEquipmentToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.BecomeSaddledUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.BecomeCopyOfEnteringCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.BecomeCopyOfEnteringCreatureUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.BecomeCopyOfTargetCreatureUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.BoostEnteringCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureThenBoostSourceIfDamagedEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardCardThenEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTriggeringCreatureUntilSourceLeavesEffect;
import com.github.laxika.magicalvibes.model.effect.EvolveTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardForTargetPlayerEffect;
import com.github.laxika.magicalvibes.model.effect.ExploreEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEqualToPowerEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEqualToToughnessEffect;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsEffect;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsEqualToEnteringPowerPutOneOnTopRestOnBottomEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnEnteringCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceEqualToEnteringPowerEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnTargetCardsFromGraveyardToHandEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.SoulbondPairWithEnteringEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfThenCreateTokensEqualToEnteringManaValueEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPredicate;
import com.github.laxika.magicalvibes.model.effect.TargetSpec;
import com.github.laxika.magicalvibes.model.amount.TargetPower;
import com.github.laxika.magicalvibes.model.effect.TransformEnteringCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.TransformTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringCardConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.UntapEnteringPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.condition.TargetPermanentMatches;
import com.github.laxika.magicalvibes.model.filter.FilterContext;
import com.github.laxika.magicalvibes.model.filter.PermanentHasCountersPredicate;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.GraveyardTargetingService;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import com.github.laxika.magicalvibes.service.effect.ConditionContext;
import com.github.laxika.magicalvibes.service.effect.ConditionEvaluationService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import com.github.laxika.magicalvibes.model.GameLog;
/**
 * Trigger collectors for enter-the-battlefield events. Mirrors the other {@code *CollectorService}
 * beans: each {@link CollectsTrigger}-annotated method handles one (slot, effect class) pair and the
 * per-slot {@code CardEffect.class} default puts the effect onto the stack. The scan orchestration
 * (which permanents/slots to visit, self-exclusion, conditional gating, Naban doubling, the
 * "skip targeting effects" rule) lives in {@link TriggerCollectionService}.
 */
@Slf4j
@Service
public class EnterTriggerCollectorService {

    private final GameLogService gameLogService;
    private final AmountEvaluationService amountEvaluationService;
    private final GameQueryService gameQueryService;
    private final GraveyardTargetingService graveyardTargetingService;
    private final PredicateEvaluationService predicateEvaluationService;
    private final ConditionEvaluationService conditionEvaluationService;

    public EnterTriggerCollectorService(GameLogService gameLogService,
                                        AmountEvaluationService amountEvaluationService,
                                        GameQueryService gameQueryService,
                                        GraveyardTargetingService graveyardTargetingService,
                                        PredicateEvaluationService predicateEvaluationService,
                                        ConditionEvaluationService conditionEvaluationService) {
        this.gameLogService = gameLogService;
        this.amountEvaluationService = amountEvaluationService;
        this.gameQueryService = gameQueryService;
        this.graveyardTargetingService = graveyardTargetingService;
        this.predicateEvaluationService = predicateEvaluationService;
        this.conditionEvaluationService = conditionEvaluationService;
    }

    // ── Default "put it on the stack" fallbacks (one per registry-backed slot) ─────────

    /**
     * "Whenever a permanent you control enters tapped, untap it" (Amulet of Vigor). The entering
     * permanent is fixed by the event and therefore is not chosen as a target.
     */
    @CollectsTrigger(value = UntapEnteringPermanentEffect.class,
            slot = EffectSlot.ON_ANY_PERMANENT_ENTERS_BATTLEFIELD)
    private boolean handleAnyPermanentEnterUntapEntering(TriggerMatchContext match,
                                                           UntapEnteringPermanentEffect effect,
                                                           TriggerContext ctx) {
        TriggerContext.PermanentEnters pe = (TriggerContext.PermanentEnters) ctx;
        if (!match.controllerId().equals(pe.enteringControllerId())) {
            return false;
        }
        Permanent enteringPermanent = findEnteringPermanent(match.gameData(), pe);
        if (enteringPermanent == null || !enteringPermanent.isTapped()) {
            return false;
        }
        UUID enteringPermanentId = findEnteringPermanentId(match, pe.enteringCard());
        if (enteringPermanentId == null) {
            return true;
        }

        Card sourceCard = match.permanent().getCard();
        for (int i = 0; i < pe.perEffectTriggerCount(); i++) {
            StackEntry entry = new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    sourceCard,
                    match.controllerId(),
                    sourceCard.getName() + "'s ability",
                    new ArrayList<>(List.of(new UntapPermanentsEffect(TapUntapScope.TARGET))),
                    enteringPermanentId,
                    match.permanent().getId());
            entry.setNonTargeting(true);
            entry.setTriggeringPermanentId(enteringPermanentId);
            entry.setTriggeringCardId(pe.enteringCard().getId());
            match.gameData().stack.add(entry);
        }
        gameLogService.append(match.gameData(), GameLog.abilityTriggers(sourceCard));
        log.info("Game {} - {} triggers to untap {} entering tapped",
                match.gameData().id, sourceCard.getName(), pe.enteringCard().getName());
        return true;
    }

    @CollectsTrigger(value = CardEffect.class, slot = EffectSlot.ON_ANY_PERMANENT_ENTERS_BATTLEFIELD)
    private boolean handleAnyPermanentEnterDefault(TriggerMatchContext match, CardEffect effect,
                                                    TriggerContext ctx) {
        return enqueueAnyPermanentEnter(match, effect, (TriggerContext.PermanentEnters) ctx);
    }

    @CollectsTrigger(value = TriggeringCardConditionalEffect.class,
            slot = EffectSlot.ON_ANY_PERMANENT_ENTERS_BATTLEFIELD)
    private boolean handleAnyPermanentEnterCardConditional(TriggerMatchContext match,
                                                             TriggeringCardConditionalEffect conditional,
                                                             TriggerContext ctx) {
        TriggerContext.PermanentEnters pe = (TriggerContext.PermanentEnters) ctx;
        if (!predicateEvaluationService.matchesCardPredicate(pe.enteringCard(), conditional.predicate(), null,
                match.gameData(), match.controllerId())) {
            return false;
        }
        return enqueueAnyPermanentEnter(match, conditional.wrapped(), pe);
    }

    @CollectsTrigger(value = TriggeringPermanentConditionalEffect.class,
            slot = EffectSlot.ON_ANY_PERMANENT_ENTERS_BATTLEFIELD)
    private boolean handleAnyPermanentEnterPermanentConditional(TriggerMatchContext match,
                                                                  TriggeringPermanentConditionalEffect conditional,
                                                                  TriggerContext ctx) {
        TriggerContext.PermanentEnters pe = (TriggerContext.PermanentEnters) ctx;
        if (!conditional.anyController() && !match.controllerId().equals(pe.enteringControllerId())) return false;
        Permanent enteringPermanent = findEnteringPermanent(match.gameData(), pe);
        if (enteringPermanent == null) return false;

        FilterContext filterContext = FilterContext.of(match.gameData())
                .withSourceCardId(match.permanent().getCard().getId())
                .withSourceControllerId(match.controllerId())
                .withSourcePermanentSnapshot(match.permanent());
        if (!predicateEvaluationService.matchesPermanentPredicate(enteringPermanent, conditional.predicate(),
                filterContext)) {
            return false;
        }
        return enqueueAnyPermanentEnter(match, conditional.wrapped(), pe);
    }

    private boolean enqueueAnyPermanentEnter(TriggerMatchContext match, CardEffect effect,
                                             TriggerContext.PermanentEnters pe) {
        if (effect instanceof ConditionalEffect conditional
                && conditional.interveningIf()
                && !conditionEvaluationService.isMet(match.gameData(), conditional.condition(),
                ConditionContext.forPermanent(match.permanent(), match.controllerId()))) {
            return false;
        }

        if (effect.targetSpec().admits(TargetPredicate.Kind.PERMANENT)) {
            UUID enteringPermanentId = findEnteringPermanentId(match, pe.enteringCard());
            if (enteringPermanentId == null) {
                return true;
            }
            for (int i = 0; i < pe.perEffectTriggerCount(); i++) {
                match.gameData().queueInteraction(new PermanentChoiceContext.EntersTriggerTarget(
                        match.permanent().getCard(), pe.enteringControllerId(),
                        new ArrayList<>(List.of(effect)), match.permanent().getId(),
                        enteringPermanentId, enteringPermanentId));
            }
            gameLogService.append(match.gameData(), GameLog.abilityTriggers(match.permanent().getCard()));
            log.info("Game {} - {} any-permanent-enters trigger awaiting target selection",
                    match.gameData().id, match.permanent().getCard().getName());
            return true;
        }
        StackEntry entry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                match.permanent().getCard(),
                match.controllerId(),
                match.permanent().getCard().getName() + "'s ability",
                new ArrayList<>(List.of(effect)),
                pe.enteringControllerId(),
                match.permanent().getId());
        entry.setNonTargeting(true);
        entry.setTriggeringPermanentId(pe.mayPayTargetCardId());
        entry.setTriggeringCardId(pe.enteringCard().getId());
        match.gameData().stack.add(entry);
        gameLogService.append(match.gameData(), GameLog.abilityTriggers(match.permanent().getCard()));
        log.info("Game {} - {} any-permanent-enters trigger queued", match.gameData().id,
                match.permanent().getCard().getName());
        return true;
    }

    private Permanent findEnteringPermanent(com.github.laxika.magicalvibes.model.GameData gameData,
                                            TriggerContext.PermanentEnters pe) {
        if (pe.mayPayTargetCardId() == null) return null;
        List<Permanent> battlefield = gameData.playerBattlefields.get(pe.enteringControllerId());
        if (battlefield == null) return null;
        return battlefield.stream()
                .filter(permanent -> pe.mayPayTargetCardId().equals(permanent.getId()))
                .findFirst()
                .orElse(null);
    }

    @CollectsTriggers({
            @CollectsTrigger(value = CardEffect.class, slot = EffectSlot.ON_ALLY_CREATURE_ENTERS_BATTLEFIELD),
            @CollectsTrigger(value = CardEffect.class, slot = EffectSlot.ON_SELF_OR_ALLY_CREATURE_ENTERS_BATTLEFIELD),
            @CollectsTrigger(value = CardEffect.class, slot = EffectSlot.ON_OPPONENT_CREATURE_ENTERS_BATTLEFIELD),
            @CollectsTrigger(value = CardEffect.class, slot = EffectSlot.ON_OPPONENT_LAND_ENTERS_BATTLEFIELD),
            @CollectsTrigger(value = CardEffect.class, slot = EffectSlot.ON_ALLY_ARTIFACT_ENTERS_BATTLEFIELD),
            @CollectsTrigger(value = CardEffect.class, slot = EffectSlot.ON_ALLY_NONTOKEN_ARTIFACT_ENTERS_BATTLEFIELD),
            @CollectsTrigger(value = CardEffect.class, slot = EffectSlot.ON_ALLY_NONTOKEN_CREATURE_ENTERS_BATTLEFIELD),
    })
    private boolean handleEnterDefault(TriggerMatchContext match, CardEffect effect, TriggerContext ctx) {
        TriggerContext.PermanentEnters pe = (TriggerContext.PermanentEnters) ctx;
        // A targeting effect can't be pushed straight onto the stack with a pre-set target — queue a
        // pending choice so the controller picks one as the ability goes on the stack (CR 603.3d).
        // Permanent-targeting effects (e.g. Reaper King's "destroy target permanent") always route
        // through the choice; player-targeting ones only when the scan left the player unset — the
        // opponent-enters scans bake in the entering permanent's controller, while the ally scans
        // leave it null (Sage's Row Denizen's "target player mills two cards").
        boolean needsTargetChoice = effect.targetSpec().admits(TargetPredicate.Kind.PERMANENT)
                || (effect.targetSpec().admits(TargetPredicate.Kind.PLAYER) && pe.defaultTargetPlayerId() == null);
        if (needsTargetChoice) {
            Card sourceCard = match.permanent().getCard();
            // The entering permanent rides along for effects phrased around "that creature"
            // (Gruul Ragebeast's fight); target-only effects such as Reaper King's ignore it.
            for (int i = 0; i < pe.perEffectTriggerCount(); i++) {
                match.gameData().queueInteraction(new PermanentChoiceContext.EntersTriggerTarget(
                        sourceCard, match.controllerId(), new ArrayList<>(List.of(effect)), match.permanent().getId(),
                        findEnteringPermanentId(match, pe.enteringCard())));
            }
            logTriggered(match);
            return true;
        }
        enqueue(match, effect, pe.defaultTargetPlayerId(), pe.perEffectTriggerCount(),
                findEnteringPermanentId(match, pe.enteringCard()));
        logTriggered(match);
        return true;
    }

    @CollectsTriggers({
            @CollectsTrigger(value = ReturnTargetCardsFromGraveyardToHandEffect.class,
                    slot = EffectSlot.ON_ALLY_CREATURE_ENTERS_BATTLEFIELD),
            @CollectsTrigger(value = ReturnTargetCardsFromGraveyardToHandEffect.class,
                    slot = EffectSlot.ON_SELF_OR_ALLY_CREATURE_ENTERS_BATTLEFIELD)
    })
    private boolean handleCreatureEnterReturnToHand(TriggerMatchContext match,
                                                     ReturnTargetCardsFromGraveyardToHandEffect effect,
                                                     TriggerContext ctx) {
        TriggerContext.PermanentEnters pe = (TriggerContext.PermanentEnters) ctx;
        Card sourceCard = match.permanent().getCard();
        for (int i = 0; i < pe.perEffectTriggerCount(); i++) {
            graveyardTargetingService.handleReturnToHandETBTargeting(
                    match.gameData(), match.controllerId(), sourceCard, List.of(effect), effect);
        }
        logTriggered(match);
        return true;
    }

    @CollectsTriggers({
            @CollectsTrigger(value = BecomeSaddledUntilEndOfTurnEffect.class,
                    slot = EffectSlot.ON_ALLY_CREATURE_ENTERS_BATTLEFIELD),
            @CollectsTrigger(value = BecomeSaddledUntilEndOfTurnEffect.class,
                    slot = EffectSlot.ON_ALLY_ARTIFACT_ENTERS_BATTLEFIELD)
    })
    private boolean handleEnteringPermanentSelfEffect(TriggerMatchContext match, CardEffect effect,
                                                       TriggerContext ctx) {
        TriggerContext.PermanentEnters pe = (TriggerContext.PermanentEnters) ctx;
        UUID enteringPermanentId = findEnteringPermanentId(match, pe.enteringCard());
        if (enteringPermanentId == null) {
            return true;
        }

        for (int i = 0; i < pe.perEffectTriggerCount(); i++) {
            StackEntry entry = new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    match.permanent().getCard(),
                    match.controllerId(),
                    match.permanent().getCard().getName() + "'s ability",
                    new ArrayList<>(List.of(effect)),
                    null,
                    enteringPermanentId);
            entry.setTriggeringCardId(pe.enteringCard().getId());
            entry.setTriggeringPermanentId(enteringPermanentId);
            entry.setNonTargeting(true);
            match.gameData().enqueueTrigger(entry);
        }
        logTriggered(match);
        return true;
    }

    @CollectsTrigger(value = CardEffect.class, slot = EffectSlot.ON_ALLY_TOKEN_ENTERS_BATTLEFIELD)
    private boolean handleTokenEnterDefault(TriggerMatchContext match, CardEffect effect, TriggerContext ctx) {
        TriggerContext.TokensEnter tokensEnter = (TriggerContext.TokensEnter) ctx;
        Card sourceCard = match.permanent().getCard();
        StackEntry entry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                sourceCard,
                match.controllerId(),
                sourceCard.getName() + "'s ability",
                new ArrayList<>(List.of(effect)),
                null,
                match.permanent().getId());
        entry.setEventValue(tokensEnter.count());
        entry.setNonTargeting(true);
        for (int i = 0; i < tokensEnter.perEffectTriggerCount(); i++) {
            match.gameData().enqueueTrigger(new StackEntry(entry));
        }
        logTriggered(match);
        return true;
    }

    /**
     * The "any other creature enters" default queues the trigger directly when it needs no target
     * and routes targeted effects through the normal enter-trigger target choice.
     */
    @CollectsTrigger(value = CardEffect.class, slot = EffectSlot.ON_ANY_OTHER_CREATURE_ENTERS_BATTLEFIELD)
    private boolean handleAnyCreatureEnterDefault(TriggerMatchContext match, CardEffect effect, TriggerContext ctx) {
        TriggerContext.PermanentEnters pe = (TriggerContext.PermanentEnters) ctx;
        boolean needsTargetChoice = isTargeting(effect);
        if (needsTargetChoice) {
            Card sourceCard = match.permanent().getCard();
            for (int i = 0; i < pe.perEffectTriggerCount(); i++) {
                match.gameData().queueInteraction(new PermanentChoiceContext.EntersTriggerTarget(
                        sourceCard, match.controllerId(), new ArrayList<>(List.of(effect)), match.permanent().getId(),
                        findEnteringPermanentId(match, pe.enteringCard())));
            }
            logTriggered(match);
            return true;
        }
        enqueue(match, effect, pe.defaultTargetPlayerId(), pe.perEffectTriggerCount());
        logTriggered(match);
        return true;
    }

    @CollectsTrigger(value = CardEffect.class, slot = EffectSlot.ON_CREATURE_ENTERS_FROM_GRAVEYARD)
    private boolean handleCreatureEnterFromGraveyardDefault(TriggerMatchContext match,
                                                             CardEffect effect, TriggerContext ctx) {
        TriggerContext.PermanentEnters pe = (TriggerContext.PermanentEnters) ctx;
        UUID enteringPermanentId = findEnteringPermanentId(match, pe.enteringCard());
        if (enteringPermanentId == null) {
            return true;
        }
        Card sourceCard = match.permanent().getCard();
        for (int i = 0; i < pe.perEffectTriggerCount(); i++) {
            match.gameData().queueInteraction(new PermanentChoiceContext.EnteringPermanentAnyTargetTrigger(
                    sourceCard, match.controllerId(), new ArrayList<>(List.of(effect)), enteringPermanentId));
        }
        logTriggered(match);
        return true;
    }

    @CollectsTrigger(value = SacrificeSelfThenCreateTokensEqualToEnteringManaValueEffect.class,
            slot = EffectSlot.ON_ANY_OTHER_CREATURE_ENTERS_BATTLEFIELD)
    private boolean handleAnyCreatureSacrificeSelfCreateTokens(
            TriggerMatchContext match,
            SacrificeSelfThenCreateTokensEqualToEnteringManaValueEffect effect,
            TriggerContext ctx) {
        TriggerContext.PermanentEnters pe = (TriggerContext.PermanentEnters) ctx;
        Card sourceCard = match.permanent().getCard();
        for (int i = 0; i < pe.perEffectTriggerCount(); i++) {
            StackEntry entry = new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    sourceCard,
                    match.controllerId(),
                    sourceCard.getName() + "'s ability",
                    new ArrayList<>(List.of(effect)),
                    null,
                    match.permanent().getId());
            entry.setEventValue(pe.enteringCard().getManaValue());
            entry.setNonTargeting(true);
            match.gameData().stack.add(entry);
        }
        logTriggered(match);
        log.info("Game {} - {} triggers for {} entering (sacrifice and create tokens)",
                match.gameData().id, sourceCard.getName(), pe.enteringCard().getName());
        return true;
    }

    // ── "May" wrappers (queued as a may-ability, not unwrapped onto the stack) ──────────

    @CollectsTriggers({
            @CollectsTrigger(value = MayEffect.class, slot = EffectSlot.ON_SELF_OR_ALLY_CREATURE_ENTERS_BATTLEFIELD),
            @CollectsTrigger(value = MayEffect.class, slot = EffectSlot.ON_ALLY_CREATURE_ENTERS_BATTLEFIELD),
            @CollectsTrigger(value = MayEffect.class, slot = EffectSlot.ON_OPPONENT_CREATURE_ENTERS_BATTLEFIELD),
            @CollectsTrigger(value = MayEffect.class, slot = EffectSlot.ON_OPPONENT_LAND_ENTERS_BATTLEFIELD),
            @CollectsTrigger(value = MayEffect.class, slot = EffectSlot.ON_ALLY_ARTIFACT_ENTERS_BATTLEFIELD),
            @CollectsTrigger(value = MayEffect.class, slot = EffectSlot.ON_ALLY_NONTOKEN_CREATURE_ENTERS_BATTLEFIELD),
    })
    private boolean handleEnterMay(TriggerMatchContext match, MayEffect may, TriggerContext ctx) {
        TriggerContext.PermanentEnters pe = (TriggerContext.PermanentEnters) ctx;
        Card sourceCard = match.permanent().getCard();
        boolean gainLifeEqualToEnteringPower = may.wrapped() instanceof GainLifeEqualToPowerEffect;
        // "You may gain life equal to that creature's toughness" (e.g. Orchard Warden): read the
        // entering creature's toughness now, since the wrapped effect loses that context once queued.
        if (may.wrapped() instanceof GainLifeEqualToToughnessEffect) {
            may = new MayEffect(new GainLifeEffect(pe.enteringCard().getToughness()), may.prompt());
        }
        if (gainLifeEqualToEnteringPower) {
            may = new MayEffect(new GainLifeEffect(new TargetPower()), may.prompt());
        }
        // Always bind the source permanent so a "may put a counter on this creature" wrapper
        // (e.g. Godtracker of Jund) resolves against the source; ally scans leave the target
        // player unset (null), which is harmless for player-directed wrapped effects.
        boolean needsTargetChoice = may.targetSpec().admits(TargetPredicate.Kind.PERMANENT)
                || (may.targetSpec().admits(TargetPredicate.Kind.PLAYER)
                && pe.defaultTargetPlayerId() == null);
        if (needsTargetChoice) {
            for (int i = 0; i < pe.perEffectTriggerCount(); i++) {
                match.gameData().queueInteraction(new PermanentChoiceContext.EntersTriggerTarget(
                        sourceCard, match.controllerId(), new ArrayList<>(List.of(may)), match.permanent().getId(),
                        findEnteringPermanentId(match, pe.enteringCard())));
            }
            logTriggered(match);
            return true;
        }
        boolean usesEnteringTarget = may.wrapped() instanceof DiscardCardThenEffect discard
                && discard.useEntryTarget();
        UUID enteringPermanentId = (gainLifeEqualToEnteringPower || usesEnteringTarget)
                ? findEnteringPermanentId(match, pe.enteringCard()) : null;
        UUID mayTargetId = gainLifeEqualToEnteringPower || usesEnteringTarget
                ? enteringPermanentId : pe.defaultTargetPlayerId();
        for (int i = 0; i < pe.perEffectTriggerCount(); i++) {
            match.gameData().queueMayAbility(sourceCard, match.controllerId(), may,
                    mayTargetId,
                    match.permanent().getId());
        }
        logTriggered(match);
        log.info("Game {} - {} triggers for {} entering (may effect)",
                match.gameData().id, sourceCard.getName(), pe.enteringCard().getName());
        return true;
    }

    @CollectsTrigger(value = MayEffect.class,
            slot = EffectSlot.ON_ANY_OTHER_CREATURE_ENTERS_BATTLEFIELD)
    private boolean handleAnyCreatureEnterMay(TriggerMatchContext match, MayEffect may, TriggerContext ctx) {
        TriggerContext.PermanentEnters pe = (TriggerContext.PermanentEnters) ctx;
        UUID enteringPermanentId = findEnteringPermanentId(match, pe.enteringCard());
        Permanent enteringPermanent = enteringPermanentId == null
                ? null : gameQueryService.findPermanentById(match.gameData(), enteringPermanentId);
        if (enteringPermanent == null) {
            return true;
        }

        Card sourceCard = match.permanent().getCard();
        for (int i = 0; i < pe.perEffectTriggerCount(); i++) {
            match.gameData().queueMayAbilityForPlayer(
                    sourceCard,
                    match.controllerId(),
                    may,
                    null,
                    enteringPermanentId,
                    pe.enteringControllerId(),
                    new Permanent(enteringPermanent)
            );
        }
        logTriggered(match);
        log.info("Game {} - {} triggers for {} entering (may effect for entering controller)",
                match.gameData().id, sourceCard.getName(), pe.enteringCard().getName());
        return true;
    }

    @CollectsTriggers({
            @CollectsTrigger(value = MayPayManaEffect.class, slot = EffectSlot.ON_ALLY_CREATURE_ENTERS_BATTLEFIELD),
            @CollectsTrigger(value = MayPayManaEffect.class, slot = EffectSlot.ON_ALLY_ARTIFACT_ENTERS_BATTLEFIELD),
            @CollectsTrigger(value = MayPayManaEffect.class, slot = EffectSlot.ON_ALLY_NONTOKEN_ARTIFACT_ENTERS_BATTLEFIELD),
            @CollectsTrigger(value = MayPayManaEffect.class, slot = EffectSlot.ON_ALLY_NONTOKEN_CREATURE_ENTERS_BATTLEFIELD),
    })
    private boolean handleEnterMayPay(TriggerMatchContext match, MayPayManaEffect mayPay, TriggerContext ctx) {
        TriggerContext.PermanentEnters pe = (TriggerContext.PermanentEnters) ctx;
        Card sourceCard = match.permanent().getCard();
        if (mayPay.sourceIsTriggeringPermanent()) {
            UUID enteringPermanentId = findEnteringPermanentId(match, pe.enteringCard());
            if (enteringPermanentId == null) {
                return true;
            }
            if (mayPay.targetSpec() != TargetSpec.NONE) {
                match.gameData().queueInteraction(new PermanentChoiceContext.EntersTriggerTarget(
                        sourceCard, match.controllerId(), new ArrayList<>(List.of(mayPay)),
                        enteringPermanentId, enteringPermanentId));
            } else {
                match.gameData().queueMayAbility(sourceCard, match.controllerId(), mayPay,
                        pe.mayPayTargetCardId(), enteringPermanentId);
            }
            logTriggered(match);
            log.info("Game {} - {} triggers for {} entering (may pay mana)",
                    match.gameData().id, sourceCard.getName(), pe.enteringCard().getName());
            return true;
        }
        UUID targetCardId = mayPay.targetSpec().admits(TargetPredicate.Kind.PERMANENT)
                ? pe.mayPayTargetCardId()
                : null;
        for (int i = 0; i < pe.perEffectTriggerCount(); i++) {
            match.gameData().queueMayAbility(sourceCard, match.controllerId(), mayPay, targetCardId);
        }
        logTriggered(match);
        log.info("Game {} - {} triggers for {} entering (may pay mana)",
                match.gameData().id, sourceCard.getName(), pe.enteringCard().getName());
        return true;
    }

    // ── Value-materialising effects ─────────────────────────────────────────────────────

    @CollectsTrigger(value = GainLifeEffect.class, slot = EffectSlot.ON_ANY_OTHER_CREATURE_ENTERS_BATTLEFIELD)
    private boolean handleAnyCreatureGainLife(TriggerMatchContext match, GainLifeEffect gainLife, TriggerContext ctx) {
        int amount = amountEvaluationService.evaluate(match.gameData(), gainLife.amount(),
                new AmountContext(match.controllerId(), match.permanent(), null, 0, 0));
        return enqueueGainLife(match, ctx, amount);
    }

    @CollectsTrigger(value = DrawCardForTargetPlayerEffect.class,
            slot = EffectSlot.ON_ANY_OTHER_CREATURE_ENTERS_BATTLEFIELD)
    private boolean handleAnyCreatureDrawEnteringController(TriggerMatchContext match,
            DrawCardForTargetPlayerEffect effect, TriggerContext ctx) {
        TriggerContext.PermanentEnters pe = (TriggerContext.PermanentEnters) ctx;
        enqueue(match, effect, pe.enteringControllerId(), pe.perEffectTriggerCount());
        logTriggered(match);
        return true;
    }

    @CollectsTrigger(value = GainLifeEqualToToughnessEffect.class, slot = EffectSlot.ON_ALLY_CREATURE_ENTERS_BATTLEFIELD)
    private boolean handleAllyCreatureGainLifeEqualToToughness(TriggerMatchContext match,
            GainLifeEqualToToughnessEffect effect, TriggerContext ctx) {
        TriggerContext.PermanentEnters pe = (TriggerContext.PermanentEnters) ctx;
        return enqueueGainLife(match, ctx, pe.enteringCard().getToughness());
    }

    private boolean enqueueGainLife(TriggerMatchContext match, TriggerContext ctx, int amount) {
        TriggerContext.PermanentEnters pe = (TriggerContext.PermanentEnters) ctx;
        var gameData = match.gameData();
        Card sourceCard = match.permanent().getCard();
        String cardName = sourceCard.getName();
        enqueue(match, new GainLifeEffect(amount), pe.defaultTargetPlayerId(), pe.perEffectTriggerCount());
        String controllerName = gameData.playerIdToName.get(match.controllerId());
        gameLogService.append(gameData, GameLog.cardThen(sourceCard,
                " triggers — " + controllerName + " will gain " + amount + " life."));
        log.info("Game {} - {} triggers for {} entering (gain {} life)",
                gameData.id, cardName, pe.enteringCard().getName(), amount);
        return true;
    }

    @CollectsTrigger(value = ExploreEffect.class,
            slot = EffectSlot.ON_ALLY_CREATURE_ENTERS_BATTLEFIELD)
    private boolean handleAllyCreatureExplore(TriggerMatchContext match,
                                               ExploreEffect effect, TriggerContext ctx) {
        TriggerContext.PermanentEnters pe = (TriggerContext.PermanentEnters) ctx;
        UUID enteringPermanentId = findEnteringPermanentId(match, pe.enteringCard());
        if (enteringPermanentId == null) {
            return true;
        }

        Card sourceCard = match.permanent().getCard();
        for (int i = 0; i < pe.perEffectTriggerCount(); i++) {
            StackEntry entry = new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    sourceCard,
                    match.controllerId(),
                    sourceCard.getName() + "'s ability",
                    new ArrayList<>(List.of(effect)),
                    null,
                    enteringPermanentId);
            entry.setNonTargeting(true);
            match.gameData().stack.add(entry);
        }
        logTriggered(match);
        return true;
    }

    @CollectsTriggers({
            @CollectsTrigger(value = DealDamageToPlayersEffect.class, slot = EffectSlot.ON_ANY_OTHER_CREATURE_ENTERS_BATTLEFIELD),
            @CollectsTrigger(value = DealDamageToPlayersEffect.class, slot = EffectSlot.ON_OPPONENT_LAND_ENTERS_BATTLEFIELD),
    })
    private boolean handleDealDamageToEnteringController(TriggerMatchContext match,
            DealDamageToPlayersEffect damageEffect, TriggerContext ctx) {
        TriggerContext.PermanentEnters pe = (TriggerContext.PermanentEnters) ctx;
        var gameData = match.gameData();
        Card sourceCard = match.permanent().getCard();
        String cardName = sourceCard.getName();
        UUID targetPlayerId = pe.enteringControllerId();
        enqueue(match, new DealDamageToPlayersEffect(damageEffect.amount(), DamageRecipient.TARGET_PLAYER), targetPlayerId,
                pe.perEffectTriggerCount());
        String targetName = gameData.playerIdToName.get(targetPlayerId);
        gameLogService.append(gameData, GameLog.cardThen(sourceCard,
                " triggers — deals " + damageEffect.amount() + " damage to " + targetName + "."));
        log.info("Game {} - {} triggers for {} entering (deal {} damage to controller)",
                gameData.id, cardName, pe.enteringCard().getName(), damageEffect.amount());
        return true;
    }

    /**
     * "Whenever a creature enters, its controller sacrifices a [permanent] of their choice" (Tainted
     * Aether). The authored effect already carries {@code SacrificeRecipient.TARGET_PLAYER}; here we
     * queue it with the entering creature's controller as the sacrificing player, mirroring the
     * damage-to-entering-controller handler above.
     */
    @CollectsTrigger(value = SacrificePermanentsEffect.class,
            slot = EffectSlot.ON_ANY_OTHER_CREATURE_ENTERS_BATTLEFIELD)
    private boolean handleAnyCreatureSacrifice(TriggerMatchContext match,
            SacrificePermanentsEffect effect, TriggerContext ctx) {
        TriggerContext.PermanentEnters pe = (TriggerContext.PermanentEnters) ctx;
        enqueue(match, effect, pe.enteringControllerId(), pe.perEffectTriggerCount());
        logTriggered(match);
        log.info("Game {} - {} triggers for {} entering (controller sacrifices)",
                match.gameData().id, match.permanent().getCard().getName(), pe.enteringCard().getName());
        return true;
    }

    /**
     * "Whenever a creature enters, this enchantment deals N damage to it" (Aether Flash). The entering
     * creature is the (non-chosen) recipient, so we resolve its permanent id now and queue a normal
     * {@link DealDamageToTargetCreatureEffect} with that id baked in as the target — the source being
     * this permanent, so prevention/protection/damage-triggers all key off it.
     */
    @CollectsTrigger(value = DealDamageToTargetCreatureEffect.class,
            slot = EffectSlot.ON_ANY_OTHER_CREATURE_ENTERS_BATTLEFIELD)
    private boolean handleAnyCreatureDealDamageToEntering(TriggerMatchContext match,
            DealDamageToTargetCreatureEffect effect, TriggerContext ctx) {
        TriggerContext.PermanentEnters pe = (TriggerContext.PermanentEnters) ctx;
        UUID enteringPermanentId = findEnteringPermanentId(match, pe.enteringCard());
        if (enteringPermanentId == null) {
            // The creature already left the battlefield; nothing to damage.
            return true;
        }
        Card sourceCard = match.permanent().getCard();
        for (int i = 0; i < pe.perEffectTriggerCount(); i++) {
            match.gameData().stack.add(new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    sourceCard,
                    match.controllerId(),
                    sourceCard.getName() + "'s ability",
                    new ArrayList<>(List.of(effect)),
                    enteringPermanentId,
                    match.permanent().getId()));
        }
        logTriggered(match);
        log.info("Game {} - {} triggers for {} entering (deal damage to entering creature)",
                match.gameData().id, sourceCard.getName(), pe.enteringCard().getName());
        return true;
    }

    /**
     * Queues Marauding Raptor-style damage to the creature that caused the ally-creature trigger.
     * The entering creature is fixed by the event, so no target choice is presented.
     */
    @CollectsTrigger(value = DealDamageToTargetCreatureThenBoostSourceIfDamagedEffect.class,
            slot = EffectSlot.ON_ALLY_CREATURE_ENTERS_BATTLEFIELD)
    private boolean handleAllyCreatureDealDamageThenBoost(TriggerMatchContext match,
            DealDamageToTargetCreatureThenBoostSourceIfDamagedEffect effect, TriggerContext ctx) {
        TriggerContext.PermanentEnters pe = (TriggerContext.PermanentEnters) ctx;
        UUID enteringPermanentId = findEnteringPermanentId(match, pe.enteringCard());
        if (enteringPermanentId == null) {
            return true;
        }
        Card sourceCard = match.permanent().getCard();
        for (int i = 0; i < pe.perEffectTriggerCount(); i++) {
            StackEntry entry = new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    sourceCard,
                    match.controllerId(),
                    sourceCard.getName() + "'s ability",
                    new ArrayList<>(List.of(effect)),
                    enteringPermanentId,
                    match.permanent().getId());
            entry.setNonTargeting(true);
            match.gameData().stack.add(entry);
        }
        logTriggered(match);
        log.info("Game {} - {} triggers for {} entering (deal damage and boost source)",
                match.gameData().id, sourceCard.getName(), pe.enteringCard().getName());
        return true;
    }

    /**
     * "Whenever a creature you control enters, it deals damage equal to its power to any target"
     * (Warstorm Surge). The entering creature — not this permanent — is the damage source (CR 608.2h),
     * so the pending any-target choice carries the entering permanent's id as {@code sourcePermanentId};
     * that is what {@code SourcePower} and the damage pipeline read at resolution. The permanent-targeting
     * default would instead bind this enchantment and offer no player targets.
     */
    @CollectsTrigger(value = DealDamageToAnyTargetEffect.class,
            slot = EffectSlot.ON_ALLY_CREATURE_ENTERS_BATTLEFIELD)
    private boolean handleAllyCreatureEntersDealsPowerDamage(TriggerMatchContext match,
            DealDamageToAnyTargetEffect effect, TriggerContext ctx) {
        TriggerContext.PermanentEnters pe = (TriggerContext.PermanentEnters) ctx;
        UUID enteringPermanentId = findEnteringPermanentId(match, pe.enteringCard());
        if (enteringPermanentId == null) {
            // The creature already left the battlefield; it has no power to deal damage with.
            return true;
        }
        Card sourceCard = match.permanent().getCard();
        for (int i = 0; i < pe.perEffectTriggerCount(); i++) {
            PermanentChoiceContext.EnteringPermanentAnyTargetTrigger context = effect.damage() instanceof ChosenPermanentPower
                    ? new PermanentChoiceContext.EnteringPermanentAnyTargetTrigger(
                    sourceCard, match.controllerId(), new ArrayList<>(List.of(effect)),
                    match.permanent().getId(), enteringPermanentId,
                    gameQueryService.getEffectivePower(match.gameData(),
                            gameQueryService.findPermanentById(match.gameData(), enteringPermanentId)))
                    : new PermanentChoiceContext.EnteringPermanentAnyTargetTrigger(
                    sourceCard, match.controllerId(), new ArrayList<>(List.of(effect)), enteringPermanentId);
            match.gameData().queueInteraction(context);
        }
        logTriggered(match);
        log.info("Game {} - {} triggers for {} entering (entering creature deals its power to any target)",
                match.gameData().id, sourceCard.getName(), pe.enteringCard().getName());
        return true;
    }

    /**
     * "Whenever a creature you control enters, it deals damage equal to its power to each opponent."
     * The entering permanent is the damage source, while the permanent carrying the trigger remains
     * the source of the triggered ability itself.
     */
    @CollectsTrigger(value = DealDamageToPlayersEffect.class,
            slot = EffectSlot.ON_ALLY_CREATURE_ENTERS_BATTLEFIELD)
    private boolean handleAllyCreatureDealsPowerDamageToEachOpponent(TriggerMatchContext match,
            DealDamageToPlayersEffect effect, TriggerContext ctx) {
        TriggerContext.PermanentEnters pe = (TriggerContext.PermanentEnters) ctx;
        if (effect.recipient() != DamageRecipient.EACH_OPPONENT
                || !(effect.amount() instanceof SourcePower)) {
            return handleEnterDefault(match, effect, ctx);
        }

        Permanent enteringPermanent = findEnteringPermanent(match.gameData(), pe);
        if (enteringPermanent == null) {
            return true;
        }

        Card sourceCard = match.permanent().getCard();
        for (int i = 0; i < pe.perEffectTriggerCount(); i++) {
            StackEntry entry = new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    sourceCard,
                    match.controllerId(),
                    sourceCard.getName() + "'s ability",
                    new ArrayList<>(List.of(effect)),
                    null,
                    enteringPermanent.getId());
            entry.setSourcePermanentSnapshot(enteringPermanent);
            entry.setDamageSourceCard(enteringPermanent.getCard());
            match.gameData().stack.add(entry);
        }
        logTriggered(match);
        log.info("Game {} - {} triggers for {} entering (entering creature deals power damage to each opponent)",
                match.gameData().id, sourceCard.getName(), pe.enteringCard().getName());
        return true;
    }

    /**
     * "Whenever a nontoken [creature] you control enters, create a token that's a copy of that
     * creature" (Necroduality). The entering creature is fixed at trigger time — bake its id as
     * {@code targetId} rather than routing through the EntersTriggerTarget choice pipeline that
     * the permanent-targeting default would use. Subtype gating (e.g. Zombie) is applied upstream
     * by {@code TriggeringCardConditionalEffect}; tokens are already excluded by the slot.
     */
    @CollectsTriggers({
            @CollectsTrigger(value = CreateTokenCopyOfTargetPermanentEffect.class,
                    slot = EffectSlot.ON_ALLY_CREATURE_ENTERS_BATTLEFIELD),
            @CollectsTrigger(value = CreateTokenCopyOfTargetPermanentEffect.class,
                    slot = EffectSlot.ON_ALLY_NONTOKEN_CREATURE_ENTERS_BATTLEFIELD),
            @CollectsTrigger(value = CreateTokenCopyOfTargetPermanentEffect.class,
                    slot = EffectSlot.ON_ANY_OTHER_CREATURE_ENTERS_BATTLEFIELD)
    })
    private boolean handleAllyNontokenCreatureCreateTokenCopy(TriggerMatchContext match,
            CreateTokenCopyOfTargetPermanentEffect effect, TriggerContext ctx) {
        TriggerContext.PermanentEnters pe = (TriggerContext.PermanentEnters) ctx;
        UUID enteringPermanentId = pe.mayPayTargetCardId();
        if (enteringPermanentId == null) {
            enteringPermanentId = findEnteringPermanentId(match, pe.enteringCard());
        }
        if (enteringPermanentId == null) {
            // The creature already left the battlefield; nothing to copy.
            return true;
        }
        Card sourceCard = match.permanent().getCard();
        for (int i = 0; i < pe.perEffectTriggerCount(); i++) {
            match.gameData().stack.add(new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    sourceCard,
                    match.controllerId(),
                    sourceCard.getName() + "'s ability",
                    new ArrayList<>(List.of(effect)),
                    enteringPermanentId,
                    match.permanent().getId()));
        }
        logTriggered(match);
        log.info("Game {} - {} triggers for {} entering (create token copy of entering creature)",
                match.gameData().id, sourceCard.getName(), pe.enteringCard().getName());
        return true;
    }

    /**
     * Unstable Shapeshifter: "Whenever another creature enters, this creature becomes a copy of that
     * creature, except it has this ability." The entering permanent's id is baked in at trigger time
     * and the source permanent rides along as the stack entry's self-target.
     */
    @CollectsTrigger(value = BecomeCopyOfEnteringCreatureEffect.class,
            slot = EffectSlot.ON_ANY_OTHER_CREATURE_ENTERS_BATTLEFIELD)
    private boolean handleAnyCreatureBecomeCopyOfEntering(TriggerMatchContext match,
            BecomeCopyOfEnteringCreatureEffect effect, TriggerContext ctx) {
        TriggerContext.PermanentEnters pe = (TriggerContext.PermanentEnters) ctx;
        UUID enteringPermanentId = pe.mayPayTargetCardId();
        if (enteringPermanentId == null) {
            enteringPermanentId = findEnteringPermanentId(match, pe.enteringCard());
        }
        if (enteringPermanentId == null) {
            // The creature already left the battlefield; nothing to copy.
            return true;
        }
        enqueue(match, new BecomeCopyOfEnteringCreatureEffect(enteringPermanentId),
                match.permanent().getId(), pe.perEffectTriggerCount());
        logTriggered(match);
        log.info("Game {} - {} triggers for {} entering (become a copy of it)",
                match.gameData().id, match.permanent().getCard().getName(), pe.enteringCard().getName());
        return true;
    }

    /**
     * Renegade Doppelganger: "Whenever another creature you control enters, you may have this
     * creature become a copy of that creature until end of turn." The copy effect is already
     * temporary and does not retain the source's ability, so accepting the may choice also models
     * the parenthetical loss of the ability for the rest of the turn.
     */
    @CollectsTrigger(value = BecomeCopyOfEnteringCreatureUntilEndOfTurnEffect.class,
            slot = EffectSlot.ON_ALLY_CREATURE_ENTERS_BATTLEFIELD)
    private boolean handleAllyBecomeCopyOfEnteringUntilEndOfTurn(TriggerMatchContext match,
            BecomeCopyOfEnteringCreatureUntilEndOfTurnEffect effect, TriggerContext ctx) {
        TriggerContext.PermanentEnters pe = (TriggerContext.PermanentEnters) ctx;
        UUID enteringPermanentId = findEnteringPermanentId(match, pe.enteringCard());
        if (enteringPermanentId == null) {
            return true;
        }

        Card sourceCard = match.permanent().getCard();
        MayEffect may = new MayEffect(
                new BecomeCopyOfTargetCreatureUntilEndOfTurnEffect(),
                "Have " + sourceCard.getName() + " become a copy of "
                        + pe.enteringCard().getName() + " until end of turn?");
        for (int i = 0; i < pe.perEffectTriggerCount(); i++) {
            match.gameData().queueMayAbility(sourceCard, match.controllerId(), may,
                    enteringPermanentId, match.permanent().getId());
        }
        logTriggered(match);
        log.info("Game {} - {} triggers for {} entering (may become a copy until end of turn)",
                match.gameData().id, sourceCard.getName(), pe.enteringCard().getName());
        return true;
    }

    @CollectsTrigger(value = ExileTriggeringCreatureUntilSourceLeavesEffect.class,
            slot = EffectSlot.ON_ANY_OTHER_CREATURE_ENTERS_BATTLEFIELD)
    private boolean handleAnyCreatureExileUntilSourceLeaves(TriggerMatchContext match,
            ExileTriggeringCreatureUntilSourceLeavesEffect effect, TriggerContext ctx) {
        TriggerContext.PermanentEnters pe = (TriggerContext.PermanentEnters) ctx;
        UUID enteringPermanentId = findEnteringPermanentId(match, pe.enteringCard());
        if (enteringPermanentId == null
                || countOtherCreatures(match.gameData(), enteringPermanentId) < effect.minimumOtherCreatures()) {
            return false;
        }

        Card sourceCard = match.permanent().getCard();
        for (int i = 0; i < pe.perEffectTriggerCount(); i++) {
            StackEntry entry = new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    sourceCard,
                    match.controllerId(),
                    sourceCard.getName() + "'s ability",
                    new ArrayList<>(List.of(effect)),
                    null,
                    match.permanent().getId());
            entry.setTriggeringPermanentId(enteringPermanentId);
            entry.setNonTargeting(true);
            match.gameData().enqueueTrigger(entry);
        }
        logTriggered(match);
        log.info("Game {} - {} triggers for {} entering (exile until source leaves)",
                match.gameData().id, sourceCard.getName(), pe.enteringCard().getName());
        return true;
    }

    @CollectsTrigger(value = PutCountersOnSourceEqualToEnteringPowerEffect.class,
            slot = EffectSlot.ON_ANY_OTHER_CREATURE_ENTERS_BATTLEFIELD)
    private boolean handleAnyCreaturePutCountersEqualToPower(TriggerMatchContext match,
            PutCountersOnSourceEqualToEnteringPowerEffect effect, TriggerContext ctx) {
        TriggerContext.PermanentEnters pe = (TriggerContext.PermanentEnters) ctx;
        int power = Math.max(0, pe.enteringCard().getPower());
        Card sourceCard = match.permanent().getCard();
        var counters = new PutCountersOnSourceEffect(effect.powerModifier(), effect.toughnessModifier(), power);
        if (effect.optional()) {
            var may = new MayEffect(counters, "Put " + power + " counter(s) on " + sourceCard.getName() + "?");
            for (int i = 0; i < pe.perEffectTriggerCount(); i++) {
                match.gameData().queueMayAbility(sourceCard, match.controllerId(), may, null, match.permanent().getId());
            }
        } else {
            enqueue(match, counters, pe.defaultTargetPlayerId(), pe.perEffectTriggerCount());
        }
        logTriggered(match);
        log.info("Game {} - {} triggers for {} entering (put {} +1/+1 counter(s))",
                match.gameData().id, sourceCard.getName(), pe.enteringCard().getName(), power);
        return true;
    }

    /**
     * "Whenever a [subtype] you control enters, [you may] transform it" (Vildin-Pack Alpha). The
     * subtype gate is applied upstream by {@code TriggeringCardConditionalEffect}; here we resolve
     * the entering permanent and either queue a "you may" ({@code optional}) or bake a mandatory
     * transform onto the stack — both whose {@code targetId} is that creature.
     */
    @CollectsTrigger(value = TransformEnteringCreatureEffect.class,
            slot = EffectSlot.ON_ALLY_CREATURE_ENTERS_BATTLEFIELD)
    private boolean handleAllyTransformEntering(TriggerMatchContext match,
            TransformEnteringCreatureEffect effect, TriggerContext ctx) {
        TriggerContext.PermanentEnters pe = (TriggerContext.PermanentEnters) ctx;
        Card sourceCard = match.permanent().getCard();
        UUID enteringPermanentId = findEnteringPermanentId(match, pe.enteringCard());
        if (enteringPermanentId == null) {
            return true;
        }
        var transform = new TransformTargetPermanentEffect();
        if (effect.optional()) {
            var may = new MayEffect(transform, "Transform " + pe.enteringCard().getName() + "?");
            for (int i = 0; i < pe.perEffectTriggerCount(); i++) {
                match.gameData().queueMayAbility(sourceCard, match.controllerId(), may,
                        enteringPermanentId, match.permanent().getId());
            }
        } else {
            for (int i = 0; i < pe.perEffectTriggerCount(); i++) {
                match.gameData().stack.add(new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY,
                        sourceCard,
                        match.controllerId(),
                        sourceCard.getName() + "'s ability",
                        new ArrayList<>(List.of(transform)),
                        enteringPermanentId,
                        match.permanent().getId()));
            }
        }
        logTriggered(match);
        log.info("Game {} - {} triggers for {} entering ({} transform it)",
                match.gameData().id, sourceCard.getName(), pe.enteringCard().getName(),
                effect.optional() ? "may" : "mandatory");
        return true;
    }

    @CollectsTrigger(value = EvolveTriggerEffect.class,
            slot = EffectSlot.ON_ALLY_CREATURE_ENTERS_BATTLEFIELD)
    private boolean handleAllyEvolve(TriggerMatchContext match,
            EvolveTriggerEffect effect, TriggerContext ctx) {
        TriggerContext.PermanentEnters pe = (TriggerContext.PermanentEnters) ctx;
        UUID enteringPermanentId = pe.mayPayTargetCardId();
        if (enteringPermanentId == null) {
            enteringPermanentId = findEnteringPermanentId(match, pe.enteringCard());
        }
        if (enteringPermanentId == null) {
            return true;
        }

        Permanent entering = gameQueryService.findPermanentById(match.gameData(), enteringPermanentId);
        if (entering == null
                || (gameQueryService.getEffectivePower(match.gameData(), entering)
                <= gameQueryService.getEffectivePower(match.gameData(), match.permanent())
                && gameQueryService.getEffectiveToughness(match.gameData(), entering)
                <= gameQueryService.getEffectiveToughness(match.gameData(), match.permanent()))) {
            return false;
        }

        int enteringPower = gameQueryService.getEffectivePower(match.gameData(), entering);
        int enteringToughness = gameQueryService.getEffectiveToughness(match.gameData(), entering);
        Card sourceCard = match.permanent().getCard();
        for (int i = 0; i < pe.perEffectTriggerCount(); i++) {
            StackEntry entry = new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    sourceCard,
                    match.controllerId(),
                    sourceCard.getName() + "'s ability",
                    new ArrayList<>(List.of(effect)),
                    null,
                    match.permanent().getId());
            entry.setTriggeringPermanentId(enteringPermanentId);
            entry.setTriggeringPermanentPowerAtTrigger(enteringPower);
            entry.setTriggeringPermanentToughnessAtTrigger(enteringToughness);
            entry.setNonTargeting(true);
            match.gameData().enqueueTrigger(entry);
        }
        logTriggered(match);
        log.info("Game {} - {} triggers for {} entering (counter-based evolve)",
                match.gameData().id, sourceCard.getName(), pe.enteringCard().getName());
        return true;
    }

    /**
     * "Whenever a creature you control [gated] enters, [you may] put M counters on it"
     * (Mighty Emergence "you may", Sigil Captain mandatory). The gate is applied upstream by an
     * {@code EnterCreatureConditionalEffect}; here we resolve the entering permanent and either queue a
     * "you may" ({@code optional}) or bake a mandatory counter placement onto the stack — both whose
     * {@code targetId} is that creature, resolved via {@link PutCounterOnTargetPermanentEffect}.
     */
    @CollectsTrigger(value = PutCountersOnEnteringCreatureEffect.class,
            slot = EffectSlot.ON_ALLY_CREATURE_ENTERS_BATTLEFIELD)
    private boolean handleAllyPutCountersOnEntering(TriggerMatchContext match,
            PutCountersOnEnteringCreatureEffect effect, TriggerContext ctx) {
        TriggerContext.PermanentEnters pe = (TriggerContext.PermanentEnters) ctx;
        Card sourceCard = match.permanent().getCard();
        UUID enteringPermanentId = findEnteringPermanentId(match, pe.enteringCard());
        if (enteringPermanentId == null) {
            // The creature already left the battlefield; nothing to add counters to.
            return true;
        }
        CardEffect counters = new PutCounterOnTargetPermanentEffect(effect.counterType(), effect.count());
        if (effect.requiredCounterType() != null) {
            counters = new ConditionalEffect(
                    new TargetPermanentMatches(new PermanentHasCountersPredicate(effect.requiredCounterType())),
                    counters);
        }
        String counterDescription = effect.counterType().name().toLowerCase(Locale.ROOT).replace('_', ' ');
        if (effect.optional()) {
            var may = new MayEffect(counters,
                    "Put " + effect.count() + " " + counterDescription + " counter(s) on "
                            + pe.enteringCard().getName() + "?");
            for (int i = 0; i < pe.perEffectTriggerCount(); i++) {
                match.gameData().queueMayAbility(sourceCard, match.controllerId(), may,
                        enteringPermanentId, match.permanent().getId());
            }
        } else {
            for (int i = 0; i < pe.perEffectTriggerCount(); i++) {
                match.gameData().stack.add(new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY,
                        sourceCard,
                        match.controllerId(),
                        sourceCard.getName() + "'s ability",
                        new ArrayList<>(List.of(counters)),
                        enteringPermanentId,
                        match.permanent().getId()));
            }
        }
        logTriggered(match);
        log.info("Game {} - {} triggers for {} entering ({} put {} {} counter(s) on it)",
                match.gameData().id, sourceCard.getName(), pe.enteringCard().getName(),
                effect.optional() ? "may" : "mandatory", effect.count(), counterDescription);
        return true;
    }

    /**
     * "Whenever another creature you control enters, that creature gets +X/+Y and gains [keywords]
     * until end of turn" (Ogre Battledriver). Resolves the entering permanent and bakes a mandatory
     * boost (plus keyword grant) onto the stack with that creature as {@code targetId}.
     */
    @CollectsTrigger(value = BoostEnteringCreatureEffect.class,
            slot = EffectSlot.ON_ALLY_CREATURE_ENTERS_BATTLEFIELD)
    private boolean handleAllyBoostEntering(TriggerMatchContext match,
            BoostEnteringCreatureEffect effect, TriggerContext ctx) {
        TriggerContext.PermanentEnters pe = (TriggerContext.PermanentEnters) ctx;
        Card sourceCard = match.permanent().getCard();
        UUID enteringPermanentId = findEnteringPermanentId(match, pe.enteringCard());
        if (enteringPermanentId == null) {
            // The creature already left the battlefield; nothing to boost.
            return true;
        }
        List<CardEffect> effects = new ArrayList<>();
        effects.add(new BoostTargetCreatureEffect(effect.powerBoost(), effect.toughnessBoost()));
        if (!effect.keywords().isEmpty()) {
            effects.add(new GrantKeywordEffect(effect.keywords(), GrantScope.TARGET));
        }
        for (int i = 0; i < pe.perEffectTriggerCount(); i++) {
            match.gameData().stack.add(new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    sourceCard,
                    match.controllerId(),
                    sourceCard.getName() + "'s ability",
                    effects,
                    enteringPermanentId,
                    match.permanent().getId()));
        }
        logTriggered(match);
        log.info("Game {} - {} triggers for {} entering (+{}/+{}, gains {})",
                match.gameData().id, sourceCard.getName(), pe.enteringCard().getName(),
                effect.powerBoost(), effect.toughnessBoost(), effect.keywords());
        return true;
    }

    /**
     * Soulbond "whenever another unpaired creature enters" (CR 702.94a). Intervening-if: both this
     * permanent and the entering creature are unpaired creatures under the controller. Queues a may
     * with the entering permanent baked as {@code targetId}.
     */
    @CollectsTrigger(value = SoulbondPairWithEnteringEffect.class,
            slot = EffectSlot.ON_ALLY_CREATURE_ENTERS_BATTLEFIELD)
    private boolean handleAllySoulbondPairWithEntering(TriggerMatchContext match,
            SoulbondPairWithEnteringEffect effect, TriggerContext ctx) {
        TriggerContext.PermanentEnters pe = (TriggerContext.PermanentEnters) ctx;
        Card sourceCard = match.permanent().getCard();
        if (match.permanent().getPairedWithId() != null) {
            return true;
        }
        UUID enteringPermanentId = findEnteringPermanentId(match, pe.enteringCard());
        if (enteringPermanentId == null) {
            return true;
        }
        var entering = new Permanent[1];
        match.gameData().forEachPermanent((playerId, perm) -> {
            if (entering[0] == null && perm.getId().equals(enteringPermanentId)) {
                entering[0] = perm;
            }
        });
        if (entering[0] == null || entering[0].getPairedWithId() != null) {
            return true;
        }
        var may = new MayEffect(effect, "Pair " + sourceCard.getName() + " with " + pe.enteringCard().getName() + "?");
        for (int i = 0; i < pe.perEffectTriggerCount(); i++) {
            match.gameData().queueMayAbility(sourceCard, match.controllerId(), may,
                    enteringPermanentId, match.permanent().getId());
        }
        logTriggered(match);
        log.info("Game {} - {} soulbond may-pair with entering {}",
                match.gameData().id, sourceCard.getName(), pe.enteringCard().getName());
        return true;
    }

    @CollectsTrigger(value = LookAtTopCardsEqualToEnteringPowerPutOneOnTopRestOnBottomEffect.class,
            slot = EffectSlot.ON_ALLY_CREATURE_ENTERS_BATTLEFIELD)
    private boolean handleAllyLookAtTopEqualToEnteringPower(TriggerMatchContext match,
            LookAtTopCardsEqualToEnteringPowerPutOneOnTopRestOnBottomEffect effect, TriggerContext ctx) {
        TriggerContext.PermanentEnters pe = (TriggerContext.PermanentEnters) ctx;
        int power = Math.max(0, pe.enteringCard().getPower());
        Card sourceCard = match.permanent().getCard();
        // X = 0: looking at zero cards accomplishes nothing, so skip the "you may" prompt entirely.
        if (power <= 0) {
            logTriggered(match);
            return true;
        }
        var look = LookAtTopCardsEffect.putOneOnTopRestOnBottom(power);
        var may = new MayEffect(look, "Look at the top " + power + " card(s) of your library?");
        for (int i = 0; i < pe.perEffectTriggerCount(); i++) {
            match.gameData().queueMayAbility(sourceCard, match.controllerId(), may, null, match.permanent().getId());
        }
        logTriggered(match);
        log.info("Game {} - {} triggers for {} entering (look at top {})",
                match.gameData().id, sourceCard.getName(), pe.enteringCard().getName(), power);
        return true;
    }

    /**
     * Resolves an Equipment attachment to the creature that caused the enter trigger. Optional
     * markers queue the existing may-attach flow; mandatory markers queue a non-targeting stack
     * entry with the entering permanent already identified.
     */
    @CollectsTriggers({
            @CollectsTrigger(value = AttachSourceEquipmentToEnteringCreatureEffect.class,
                    slot = EffectSlot.ON_ANY_OTHER_CREATURE_ENTERS_BATTLEFIELD),
            @CollectsTrigger(value = AttachSourceEquipmentToEnteringCreatureEffect.class,
                    slot = EffectSlot.ON_ALLY_CREATURE_ENTERS_BATTLEFIELD),
            @CollectsTrigger(value = AttachSourceEquipmentToEnteringCreatureEffect.class,
                    slot = EffectSlot.ON_CREATURE_ENTERS_FROM_GRAVEYARD)
    })
    private boolean handleCreatureAttachEquipment(TriggerMatchContext match,
            AttachSourceEquipmentToEnteringCreatureEffect effect, TriggerContext ctx) {
        TriggerContext.PermanentEnters pe = (TriggerContext.PermanentEnters) ctx;
        Card sourceCard = match.permanent().getCard();
        UUID enteringPermanentId = findEnteringPermanentId(match, pe.enteringCard());
        if (enteringPermanentId == null) {
            // The creature already left the battlefield; nothing to attach to.
            return true;
        }
        if (effect.optional()) {
            var may = new MayEffect(new AttachSourceEquipmentToTargetCreatureEffect(),
                    "Attach " + sourceCard.getName() + " to " + pe.enteringCard().getName() + "?");
            for (int i = 0; i < pe.perEffectTriggerCount(); i++) {
                match.gameData().queueMayAbility(sourceCard, match.controllerId(), may,
                        enteringPermanentId, match.permanent().getId());
            }
        } else {
            for (int i = 0; i < pe.perEffectTriggerCount(); i++) {
                StackEntry entry = new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY,
                        sourceCard,
                        match.controllerId(),
                        sourceCard.getName() + "'s ability",
                        new ArrayList<>(List.of(new AttachSourceEquipmentToTargetCreatureEffect())),
                        enteringPermanentId,
                        match.permanent().getId());
                entry.setNonTargeting(true);
                match.gameData().stack.add(entry);
            }
        }
        logTriggered(match);
        log.info("Game {} - {} triggers for {} entering ({} attach equipment)",
                match.gameData().id, sourceCard.getName(), pe.enteringCard().getName(),
                effect.optional() ? "may" : "mandatory");
        return true;
    }

    /**
     * "Whenever a creature an opponent controls enters, you may attach this Aura to that creature"
     * (Prison Term). Resolves the entering permanent (under an opponent's control) and queues a
     * "you may attach" whose {@code targetId} is that creature and {@code sourcePermanentId} is this
     * Aura, so the Aura's controller chooses whether to move it.
     */
    @CollectsTrigger(value = AttachSourceAuraToEnteringCreatureEffect.class,
            slot = EffectSlot.ON_OPPONENT_CREATURE_ENTERS_BATTLEFIELD)
    private boolean handleOpponentCreatureAttachAura(TriggerMatchContext match,
            AttachSourceAuraToEnteringCreatureEffect effect, TriggerContext ctx) {
        TriggerContext.PermanentEnters pe = (TriggerContext.PermanentEnters) ctx;
        Card sourceCard = match.permanent().getCard();
        UUID enteringPermanentId = findEnteringPermanentId(match, pe.enteringCard());
        if (enteringPermanentId == null) {
            // The creature already left the battlefield; nothing to attach to.
            return true;
        }
        var may = new MayEffect(new AttachSourceAuraToTargetCreatureEffect(),
                "Attach " + sourceCard.getName() + " to " + pe.enteringCard().getName() + "?");
        for (int i = 0; i < pe.perEffectTriggerCount(); i++) {
            match.gameData().queueMayAbility(sourceCard, match.controllerId(), may,
                    enteringPermanentId, match.permanent().getId());
        }
        logTriggered(match);
        log.info("Game {} - {} triggers for {} entering (may attach aura)",
                match.gameData().id, sourceCard.getName(), pe.enteringCard().getName());
        return true;
    }

    private UUID findEnteringPermanentId(TriggerMatchContext match, Card enteringCard) {
        UUID[] found = new UUID[1];
        match.gameData().forEachPermanent((playerId, perm) -> {
            if (found[0] == null && perm.getCard() == enteringCard) {
                found[0] = perm.getId();
            }
        });
        return found[0];
    }

    private int countOtherCreatures(GameData gameData, UUID enteringPermanentId) {
        int[] count = {0};
        gameData.forEachPermanent((playerId, permanent) -> {
            if (!permanent.getId().equals(enteringPermanentId)
                    && gameQueryService.isCreature(gameData, permanent)) {
                count[0]++;
            }
        });
        return count[0];
    }

    // ── Helpers ─────────────────────────────────────────────────────────────────────────

    private void enqueue(TriggerMatchContext match, CardEffect effect, UUID targetPlayerId, int count) {
        enqueue(match, effect, targetPlayerId, count, null);
    }

    private void enqueue(TriggerMatchContext match, CardEffect effect, UUID targetPlayerId, int count,
                         UUID enteringPermanentId) {
        Card sourceCard = match.permanent().getCard();
        Permanent enteringPermanent = enteringPermanentId == null
                ? null : gameQueryService.findPermanentById(match.gameData(), enteringPermanentId);
        for (int i = 0; i < count; i++) {
            StackEntry entry = new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    sourceCard,
                    match.controllerId(),
                    sourceCard.getName() + "'s ability",
                    new ArrayList<>(List.of(effect)),
                    targetPlayerId,
                    match.permanent().getId()
            );
            entry.setSourcePermanentSnapshot(new Permanent(match.permanent()));
            if (enteringPermanent != null) {
                entry.setTriggeringPermanentId(enteringPermanentId);
                entry.setTriggeringPermanentPowerAtTrigger(
                        gameQueryService.getEffectivePower(match.gameData(), enteringPermanent));
            }
            match.gameData().stack.add(entry);
        }
    }

    private void logTriggered(TriggerMatchContext match) {
        gameLogService.append(match.gameData(), GameLog.abilityTriggers(match.permanent().getCard()));
    }

    private static boolean isTargeting(CardEffect effect) {
        TargetSpec spec = effect.targetSpec();
        return spec.admits(TargetPredicate.Kind.PLAYER)
                || spec.admits(TargetPredicate.Kind.PERMANENT)
                || EffectResolution.targetsSpellOnStack(effect)
                || spec.admits(TargetPredicate.Kind.GRAVEYARD_CARD);
    }

}

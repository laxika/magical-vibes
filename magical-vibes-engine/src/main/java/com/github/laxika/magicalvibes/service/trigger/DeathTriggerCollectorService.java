package com.github.laxika.magicalvibes.service.trigger;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EmblemArtifactGraveyardReturnTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfDyingCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenWithTotalDyingCreaturesPowerEffect;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToEnchantedPermanentControllerEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardForTargetPlayerEffect;
import com.github.laxika.magicalvibes.model.effect.EmblemCreatureDeathTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTriggeringCreatureAndReturnSourceToHandEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayLifeEffect;
import com.github.laxika.magicalvibes.model.effect.PerpetuallyGrantTriggeredAbilityToSourceCardEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceCardEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterDelayedReturnDyingCreatureUnderControlEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnSourceCardFromGraveyardToOwnerHandEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnSourceTransformedFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnTriggeringPermanentToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnDyingCreatureToOwnerHandUnlessTargetPaysLifeEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnTriggeringArtifactToOwnerHandUnlessTargetTakesDamageEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnEnchantedCreatureToOwnerHandOnDeathEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnSourceTransformedFromGraveyardAttachedToTargetPlayerEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnTriggeringLandFromGraveyardToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.StealDyingOpponentPermanentUnlessPaysLifeEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilter;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.GraveyardTargetingService;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import com.github.laxika.magicalvibes.service.effect.ConditionEvaluationService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Trigger collectors for death and leaves-battlefield events.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DeathTriggerCollectorService {

    private final GameQueryService gameQueryService;
    private final PredicateEvaluationService predicateEvaluationService;
    private final GameLogService gameLogService;
    private final AmountEvaluationService amountEvaluationService;
    private final ConditionEvaluationService conditionEvaluationService;
    private final com.github.laxika.magicalvibes.service.effect.GraveyardTargetingSupport graveyardTargetingSupport;
    @Autowired @Lazy private GraveyardTargetingService graveyardTargetingService;

    /**
     * The handlers of the "whenever [an object] dies" abilities the trigger pipeline matches, by the old slot
     * collector that binds them ({@link CreatureDeathWatch}) and effect class. They left {@code @CollectsTrigger}
     * when their slots left {@code EffectSlot} (effect-slot-refactor.md step 11); step 15 replaces them.
     */
    private final Map<CreatureDeathWatch, Map<Class<? extends CardEffect>, TriggerCollectorHandler>>
            creatureDeathHandlers = new EnumMap<>(CreatureDeathWatch.class);

    {
        registerCreatureDeathHandlers();
    }

    /**
     * Dispatches {@code effect}, an effect of a "whenever [an object] dies" ability that {@code watch}'s collector
     * binds, to its handler, in the order the trigger registry picks one: the effect's class, then the watch's
     * default. A "you may" is the effect itself, never unwrapped (CR 603.5). Returns whether it triggered.
     */
    public boolean dispatchCreatureDeath(CreatureDeathWatch watch, TriggerMatchContext match, CardEffect effect,
                                         TriggerContext context) {
        Map<Class<? extends CardEffect>, TriggerCollectorHandler> handlers =
                creatureDeathHandlers.getOrDefault(watch, Map.of());
        TriggerCollectorHandler handler = handlers.get(effect.getClass());
        if (handler != null) {
            return handler.handle(match, effect, context);
        }
        handler = handlers.get(CardEffect.class);
        return handler != null && handler.handle(match, effect, context);
    }

    private void registerCreatureDeath(CreatureDeathWatch watch, Class<? extends CardEffect> effectClass,
                                       TriggerCollectorHandler handler) {
        creatureDeathHandlers.computeIfAbsent(watch, ignored -> new HashMap<>()).put(effectClass, handler);
    }

    private void registerCreatureDeathHandlers() {
        registerCreatureDeath(CreatureDeathWatch.ALLY, MayPayManaEffect.class,
                (match, effect, context) -> handleAllyCreatureMayPay(match, (MayPayManaEffect) effect, context));
        registerCreatureDeath(CreatureDeathWatch.ALLY, MayPayLifeEffect.class,
                (match, effect, context) -> handleAllyCreatureMayPayLife(match, (MayPayLifeEffect) effect, context));
        registerCreatureDeath(CreatureDeathWatch.ALLY, MayEffect.class,
                (match, effect, context) -> handleAllyCreatureMay(match, (MayEffect) effect, context));
        registerCreatureDeath(CreatureDeathWatch.ALLY_CREATURE_OR_PLANESWALKER, MayEffect.class,
                (match, effect, context) -> handleAllyCreatureOrPlaneswalkerMay(match, (MayEffect) effect, context));
        registerCreatureDeath(CreatureDeathWatch.EQUIPPED, RegisterDelayedReturnDyingCreatureUnderControlEffect.class,
                (match, effect, context) -> handleEquippedCreatureDelayedReturn(match,
                        (RegisterDelayedReturnDyingCreatureUnderControlEffect) effect, context));
        registerCreatureDeath(CreatureDeathWatch.EQUIPPED, PerpetuallyGrantTriggeredAbilityToSourceCardEffect.class,
                (match, effect, context) -> handleEquippedCreatureDeathPerpetualGrant(match,
                        (PerpetuallyGrantTriggeredAbilityToSourceCardEffect) effect, context));
        registerCreatureDeath(CreatureDeathWatch.EQUIPPED, CardEffect.class,
                (match, effect, context) -> handleEquippedCreatureDeathDefault(match, (CardEffect) effect, context));
        registerCreatureDeath(CreatureDeathWatch.ALLY_ARTIFACT_OR_CREATURE, CardEffect.class,
                this::handleAllyArtifactOrCreatureDeath);
        registerCreatureDeath(CreatureDeathWatch.ANY, ReturnTriggeringPermanentToBattlefieldEffect.class,
                (match, effect, context) -> handleAnyCreatureDeathReturnTriggeringPermanent(match,
                        (ReturnTriggeringPermanentToBattlefieldEffect) effect, context));
        registerCreatureDeath(CreatureDeathWatch.ANY, BoostSelfEffect.class,
                (match, effect, context) -> handleAnyCreatureDeathBoostSelf(match, (BoostSelfEffect) effect, context));
        registerCreatureDeath(CreatureDeathWatch.ANY, PutCountersOnSourceEffect.class,
                (match, effect, context) -> handleAnyCreatureDeathPutCounters(match,
                        (PutCountersOnSourceEffect) effect, context));
        registerCreatureDeath(CreatureDeathWatch.ANY, PutCountersOnSelfEffect.class,
                (match, effect, context) -> handleAnyCreatureDeathPutCountersOnSelf(match,
                        (PutCountersOnSelfEffect) effect, context));
        registerCreatureDeath(CreatureDeathWatch.ANY, UntapPermanentsEffect.class,
                (match, effect, context) -> handleAnyCreatureDeathUntap(match,
                        (UntapPermanentsEffect) effect, context));
        registerCreatureDeath(CreatureDeathWatch.ANY, TapPermanentsEffect.class,
                (match, effect, context) -> handleAnyCreatureDeathTap(match, (TapPermanentsEffect) effect, context));
        registerCreatureDeath(CreatureDeathWatch.ANY, MayEffect.class,
                (match, effect, context) -> handleAnyCreatureDeathMay(match, (MayEffect) effect, context));
        registerCreatureDeath(CreatureDeathWatch.ANY, CreateTokenCopyOfDyingCreatureEffect.class,
                (match, effect, context) -> handleAnyCreatureDeathCreateTokenCopy(match,
                        (CreateTokenCopyOfDyingCreatureEffect) effect, context));
        registerCreatureDeath(CreatureDeathWatch.ANY, CardEffect.class,
                (match, effect, context) -> handleAnyCreatureDeathDefault(match, (CardEffect) effect, context));
        registerCreatureDeath(CreatureDeathWatch.ANY, RegisterDelayedReturnDyingCreatureUnderControlEffect.class,
                (match, effect, context) -> handleAnyCreatureDeathDelayedReturnUnderOwnersControl(match,
                        (RegisterDelayedReturnDyingCreatureUnderControlEffect) effect, context));
        registerCreatureDeath(CreatureDeathWatch.ANY, ExileTriggeringCreatureAndReturnSourceToHandEffect.class,
                (match, effect, context) -> handleAnyCreatureDeathExileLinkedCreature(match,
                        (ExileTriggeringCreatureAndReturnSourceToHandEffect) effect, context));
        registerCreatureDeath(CreatureDeathWatch.ALLY_NONTOKEN, CreateTokenWithTotalDyingCreaturesPowerEffect.class,
                (match, effect, context) -> handleAllyNontokenCreateTokenWithTotalDyingCreaturesPower(match,
                        (CreateTokenWithTotalDyingCreaturesPowerEffect) effect, context));
        registerCreatureDeath(CreatureDeathWatch.ALLY_NONTOKEN, MayEffect.class,
                (match, effect, context) -> handleAllyNontokenMay(match, (MayEffect) effect, context));
        registerCreatureDeath(CreatureDeathWatch.ALLY_NONTOKEN, MayPayManaEffect.class,
                (match, effect, context) -> handleAllyNontokenMayPay(match, (MayPayManaEffect) effect, context));
        registerCreatureDeath(CreatureDeathWatch.ALLY_NONTOKEN, CardEffect.class,
                (match, effect, context) -> handleAllyNontokenDefault(match, (CardEffect) effect, context));
        registerCreatureDeath(CreatureDeathWatch.ALLY_NONTOKEN, ConditionalEffect.class,
                (match, effect, context) -> handleAllyNontokenInterveningIfConditional(match,
                        (ConditionalEffect) effect, context));
        registerCreatureDeath(CreatureDeathWatch.ANY_NONTOKEN, CardEffect.class,
                (match, effect, context) -> handleAnyNontokenCreatureDeathDefault(match, (CardEffect) effect, context));
        registerCreatureDeath(CreatureDeathWatch.ENCHANTED_PLAYER_NONTOKEN, CardEffect.class,
                (match, effect, context) -> handleEnchantedPlayerNontokenCreatureDeathDefault(match,
                        (CardEffect) effect, context));
        registerCreatureDeath(CreatureDeathWatch.OPPONENT, MayEffect.class,
                (match, effect, context) -> handleOpponentCreatureDeathMay(match, (MayEffect) effect, context));
        registerCreatureDeath(CreatureDeathWatch.OPPONENT, RegisterDelayedReturnDyingCreatureUnderControlEffect.class,
                (match, effect, context) -> handleOpponentCreatureDelayedReturnUnderControl(match,
                        (RegisterDelayedReturnDyingCreatureUnderControlEffect) effect, context));
        registerCreatureDeath(CreatureDeathWatch.OPPONENT, CardEffect.class,
                (match, effect, context) -> handleOpponentCreatureDeathDefault(match, (CardEffect) effect, context));
        registerCreatureDeath(CreatureDeathWatch.ANY, PutCountersOnSourceCardEffect.class,
                (match, effect, context) -> handleAnyCreatureDeathPutCountersOnSourceCard(match,
                        (PutCountersOnSourceCardEffect) effect, context));
    }

    /**
     * The mana value limit of the graveyard target of {@code effect} ({@code ReturnCardFromGraveyardEffect
     * .dynamicMaxManaValue()}), evaluated as the ability triggers with {@code lastKnown}, the object that died as it
     * last existed, as the amount's source, so it looks back at the others that died with it (CR 603.10a): Soulshift X
     * counts the Kodama, "with lesser mana value" is the dying object's. The choice carries it to the stack entry.
     * {@code null} when the effect has no such limit.
     */
    private Integer graveyardTargetMaxManaValueAtDeath(GameData gameData, UUID controllerId, CardEffect effect,
                                                       Permanent lastKnown) {
        var target = graveyardTargetingSupport.findTarget(List.of(effect));
        if (target == null || target.maximumManaValue() == null || lastKnown == null) {
            return null;
        }
        return amountEvaluationService.evaluateAtDeath(gameData, target.maximumManaValue(), controllerId, lastKnown);
    }

    boolean handleAllyCreatureMayPay(TriggerMatchContext match,
            MayPayManaEffect mayPay, TriggerContext ctx) {
        TriggerContext.CreatureDeath cd = (TriggerContext.CreatureDeath) ctx;
        match.gameData().queueMayAbility(match.permanent().getCard(), cd.dyingCreatureControllerId(), mayPay,
                null, match.permanent().getId());
        return true;
    }

    boolean handleAllyCreatureMayPayLife(TriggerMatchContext match,
            MayPayLifeEffect mayPay, TriggerContext ctx) {
        TriggerContext.CreatureDeath cd = (TriggerContext.CreatureDeath) ctx;
        StackEntry entry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                match.permanent().getCard(),
                cd.dyingCreatureControllerId(),
                match.permanent().getCard().getName() + "'s ability",
                new ArrayList<>(List.of(mayPay)));
        markDyingCard(match.gameData(), entry, cd.dyingCard());
        match.gameData().stack.add(entry);
        return true;
    }

    boolean handleAllyCreatureMay(TriggerMatchContext match,
            MayEffect may, TriggerContext ctx) {
        TriggerContext.CreatureDeath cd = (TriggerContext.CreatureDeath) ctx;
        match.gameData().queueMayAbility(match.permanent().getCard(), cd.dyingCreatureControllerId(), may, null,
                match.permanent().getId());
        // Snapshot the source so a transform instruction can tell whether it has already transformed
        // since this trigger was put on the stack (CR 701.27f, Thraben Sentry).
        StackEntry entry = match.gameData().stack.getLast();
        entry.setSourcePermanentSnapshot(new Permanent(match.permanent()));
        // "That card" (Colfenor's Urn, Angelic Renewal, Shirei, Shizo's Caretaker): the creature that died.
        markDyingCard(match.gameData(), entry, cd.dyingCard());
        entry.setTriggeringPermanent(cd.dyingPermanent());
        return true;
    }

    @CollectsWatchTrigger(value = RegisterDelayedReturnDyingCreatureUnderControlEffect.class,
            watch = ZoneChangeWatch.ON_CREATURE_PUT_INTO_CONTROLLER_GRAVEYARD_FROM_BATTLEFIELD)
    boolean handleOwnedCreatureGraveyardDelayedReturn(TriggerMatchContext match,
            RegisterDelayedReturnDyingCreatureUnderControlEffect effect, TriggerContext ctx) {
        TriggerContext.AnyPermanentGraveyard death = (TriggerContext.AnyPermanentGraveyard) ctx;
        if (death.dyingCard() == null) {
            return false;
        }
        StackEntry entry = new StackEntry(StackEntryType.TRIGGERED_ABILITY,
                match.permanent().getCard(), match.controllerId(),
                match.permanent().getCard().getName() + "'s ability",
                new ArrayList<>(List.of(effect)),
                null, match.permanent().getId());
        markDyingCard(match.gameData(), entry, death.dyingCard());
        match.gameData().stack.add(entry);
        return true;
    }

    @CollectsWatchTrigger(value = MayEffect.class,
            watch = ZoneChangeWatch.ON_ANY_PERMANENT_PUT_INTO_GRAVEYARD_FROM_BATTLEFIELD)
    boolean handleAnyPermanentGraveyardMay(TriggerMatchContext match,
            MayEffect may, TriggerContext ctx) {
        TriggerContext.AnyPermanentGraveyard death = (TriggerContext.AnyPermanentGraveyard) ctx;
        if (!match.controllerId().equals(death.dyingControllerId())) {
            return false;
        }
        Permanent dyingPermanent = death.dyingPermanent() != null
                ? death.dyingPermanent() : new Permanent(death.dyingCard());

        if (may.targetSpec().admits(TargetPredicate.Kind.PERMANENT)
                || may.targetSpec().admits(TargetPredicate.Kind.PLAYER)
                || graveyardTargetingSupport.findTarget(List.of(may)) != null) {
            match.gameData().queueInteraction(new PermanentChoiceContext.DeathTriggerTarget(
                    match.permanent().getCard(), match.controllerId(), new ArrayList<>(List.of(may)))
                    .withTriggeringPermanent(dyingPermanent));
        } else {
            match.gameData().queueMayAbility(match.permanent().getCard(), match.controllerId(), may,
                    null, match.permanent().getId());
            match.gameData().stack.getLast().setTriggeringPermanent(dyingPermanent);
        }
        return true;
    }

    boolean handleAllyCreatureOrPlaneswalkerMay(TriggerMatchContext match,
            MayEffect may, TriggerContext ctx) {
        TriggerContext.CreatureDeath cd = (TriggerContext.CreatureDeath) ctx;
        match.gameData().queueMayAbility(match.permanent().getCard(), cd.dyingCreatureControllerId(), may, null,
                match.permanent().getId());
        return true;
    }

    boolean handleEquippedCreatureDelayedReturn(TriggerMatchContext match,
            RegisterDelayedReturnDyingCreatureUnderControlEffect effect, TriggerContext ctx) {
        TriggerContext.EquippedCreatureDeath death = (TriggerContext.EquippedCreatureDeath) ctx;
        if (death.dyingCard() == null) {
            return false;
        }

        StackEntry entry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                match.permanent().getCard(),
                match.controllerId(),
                match.permanent().getCard().getName() + "'s ability",
                new ArrayList<>(List.of(effect)),
                null,
                match.permanent().getId()
        );
        markDyingCard(match.gameData(), entry, death.dyingCard());
        match.gameData().stack.add(entry);
        gameLogService.append(match.gameData(), GameLog.cardThen(match.permanent().getCard(),
                "'s ability triggers (equipped creature died)."));
        return true;
    }

    boolean handleEquippedCreatureDeathPerpetualGrant(TriggerMatchContext match,
            PerpetuallyGrantTriggeredAbilityToSourceCardEffect effect, TriggerContext ctx) {
        TriggerContext.EquippedCreatureDeath death = (TriggerContext.EquippedCreatureDeath) ctx;
        if (death.dyingCard() == null) {
            return false;
        }
        StackEntry entry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                match.permanent().getCard(),
                match.controllerId(),
                match.permanent().getCard().getName() + "'s ability",
                new ArrayList<>(List.of(effect)),
                null,
                match.permanent().getId());
        markDyingCard(match.gameData(), entry, death.dyingCard());
        match.gameData().stack.add(entry);
        gameLogService.append(match.gameData(), GameLog.cardThen(match.permanent().getCard(),
                "'s ability triggers (equipped creature died)."));
        log.info("Game {} - {} perpetual-grant trigger fires (equipped creature died)",
                match.gameData().id, match.permanent().getCard().getName());
        return true;
    }

    boolean handleEquippedCreatureDeathDefault(TriggerMatchContext match,
            CardEffect effect, TriggerContext ctx) {
        GameData gameData = match.gameData();
        TriggerContext.EquippedCreatureDeath death = (TriggerContext.EquippedCreatureDeath) ctx;
        CardEffect resolvedEffect = effect;
        if (resolvedEffect.targetSpec().admits(TargetPredicate.Kind.PERMANENT)
                || resolvedEffect.targetSpec().admits(TargetPredicate.Kind.PLAYER)) {
            PermanentChoiceContext.DeathTriggerTarget target = new PermanentChoiceContext.DeathTriggerTarget(
                    match.permanent().getCard(), match.controllerId(), new ArrayList<>(List.of(resolvedEffect)),
                    null, new Permanent(match.permanent()), match.permanent().getCard().getTargetFilter())
                    .withTriggeringPermanent(death.dyingPermanent());
            gameData.queueInteraction(death.dyingCard() == null ? target : target.withTriggeringCard(
                    death.dyingCard().getId(), gameData.graveyardEntryVersion(death.dyingCard().getId())));
        } else {
            StackEntry entry = new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    match.permanent().getCard(),
                    match.controllerId(),
                    match.permanent().getCard().getName() + "'s ability",
                    new ArrayList<>(List.of(resolvedEffect)),
                    null,
                    match.permanent().getId()
            );
            // The dying card's graveyard object is fixed at the death (CR 400.7): a later re-entry
            // before this trigger resolves must not satisfy a delayed return meant for the old object.
            markDyingCard(gameData, entry, death.dyingCard());
            entry.setTriggeringPermanent(death.dyingPermanent());
            // "X is its power" (Mask of Griselbrand): its last-known power, read as the ability resolves.
            entry.setTriggeringPermanentPowerAtTrigger(death.dyingCreaturePower());
            gameData.stack.add(entry);
        }
        gameLogService.append(gameData, GameLog.cardThen(match.permanent().getCard(),
                "'s ability triggers (equipped creature died)."));
        log.info("Game {} - {} triggers (equipped creature died)", gameData.id, match.permanent().getCard().getName());
        return true;
    }

    // ── ON_ENCHANTED_PERMANENT_PUT_INTO_GRAVEYARD ──────────────────────

    @CollectsWatchTrigger(value = ReturnSourceTransformedFromGraveyardAttachedToTargetPlayerEffect.class,
            watch = ZoneChangeWatch.ON_ENCHANTED_PERMANENT_PUT_INTO_GRAVEYARD)
    boolean handleReturnSourceTransformedAttachedToTargetPlayer(TriggerMatchContext match,
            ReturnSourceTransformedFromGraveyardAttachedToTargetPlayerEffect effect, TriggerContext ctx) {
        addEnchantedPermanentDeathEntry(match, ctx, effect);
        return true;
    }

    @CollectsWatchTrigger(value = DrawCardForTargetPlayerEffect.class,
            watch = ZoneChangeWatch.ON_ENCHANTED_PERMANENT_PUT_INTO_GRAVEYARD)
    boolean handleEnchantedCreatureControllerDrawsCard(TriggerMatchContext match,
            DrawCardForTargetPlayerEffect effect, TriggerContext ctx) {
        // Fate Foretold: the dying creature's controller draws, not the Aura's controller.
        TriggerContext.EnchantedPermanentDeath epd = (TriggerContext.EnchantedPermanentDeath) ctx;
        match.gameData().stack.add(new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                match.permanent().getCard(),
                match.controllerId(),
                match.permanent().getCard().getName() + "'s ability",
                new ArrayList<>(List.of(effect)),
                epd.dyingPermanentControllerId(),
                match.permanent().getId()
        ));
        gameLogService.append(match.gameData(), GameLog.cardThen(match.permanent().getCard(),
                "'s ability triggers (enchanted permanent put into graveyard)."));
        log.info("Game {} - {} triggers (enchanted permanent put into graveyard)",
                match.gameData().id, match.permanent().getCard().getName());
        return true;
    }

    @CollectsWatchTrigger(value = DealDamageToEnchantedPermanentControllerEffect.class,
            watch = ZoneChangeWatch.ON_ENCHANTED_PERMANENT_PUT_INTO_GRAVEYARD)
    boolean handleEnchantedCreatureDeathDamageEnchantedController(TriggerMatchContext match,
            DealDamageToEnchantedPermanentControllerEffect effect, TriggerContext ctx) {
        TriggerContext.EnchantedPermanentDeath epd = (TriggerContext.EnchantedPermanentDeath) ctx;
        StackEntry entry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                match.permanent().getCard(),
                match.controllerId(),
                match.permanent().getCard().getName() + "'s ability",
                new ArrayList<>(List.of(effect)),
                epd.dyingPermanentControllerId(),
                match.permanent().getId()
        );
        entry.setSourcePermanentSnapshot(new Permanent(match.permanent()));
        // When the dying creature deals the damage (last-known information), its lifelink and its
        // controller apply to the damage rather than the Aura's.
        if (effect.enchantedPermanentIsSource()) {
            if (epd.dyingCard() != null) {
                entry.setDamageSourceCard(epd.dyingCard());
            }
            entry.setDamageSourceControllerId(epd.dyingPermanentControllerId());
        }
        match.gameData().stack.add(entry);
        gameLogService.append(match.gameData(), GameLog.cardThen(match.permanent().getCard(),
                "'s ability triggers (enchanted permanent put into graveyard)."));
        log.info("Game {} - {} triggers (enchanted permanent put into graveyard)", match.gameData().id,
                match.permanent().getCard().getName());
        return true;
    }

    @CollectsWatchTrigger(value = CardEffect.class, watch = ZoneChangeWatch.ON_ENCHANTED_PERMANENT_PUT_INTO_GRAVEYARD)
    boolean handleEnchantedPermanentDeathDefault(TriggerMatchContext match,
            CardEffect effect, TriggerContext ctx) {
        TriggerContext.EnchantedPermanentDeath epd = (TriggerContext.EnchantedPermanentDeath) ctx;
        StackEntry entry = addEnchantedPermanentDeathEntryReturning(match, ctx, effect, null,
                epd.dyingPermanentControllerId());
        if (entry != null && enchantedPermanentDealsDamage(effect)) {
            // The dying creature deals the damage (last-known information): its lifelink and its
            // controller apply to that damage rather than the Aura's.
            if (epd.dyingCard() != null) {
                entry.setDamageSourceCard(epd.dyingCard());
            }
            entry.setDamageSourceControllerId(epd.dyingPermanentControllerId());
        }
        return true;
    }

    private static boolean enchantedPermanentDealsDamage(CardEffect effect) {
        if (effect instanceof SequenceEffect sequence) {
            return sequence.steps().stream().anyMatch(DeathTriggerCollectorService::enchantedPermanentDealsDamage);
        }
        return effect instanceof DealDamageToEnchantedPermanentControllerEffect damage
                && damage.enchantedPermanentIsSource();
    }

    private void addEnchantedPermanentDeathEntry(TriggerMatchContext match, TriggerContext ctx, CardEffect effect) {
        addEnchantedPermanentDeathEntryReturning(match, ctx, effect, null, null);
    }

    /**
     * Puts an "enchanted permanent is put into a graveyard" ability on the stack; returns the stack entry, or
     * {@code null} if the trigger was queued for a target choice. The entry (or the choice) records the permanent
     * that was put into a graveyard as its triggering permanent, as it last existed on the battlefield (CR 603.10a):
     * "its controller", "its power", "its toughness" and "its owner" are read from it as the ability resolves, never
     * bound into the effect. The entry's triggering cards are the cards that permanent put into the graveyard, each
     * with its graveyard incarnation (CR 400.7), which "return that card" reads. Its event value is that creature's
     * last-known power ("X is its power") unless {@code eventValue} says otherwise.
     */
    private StackEntry addEnchantedPermanentDeathEntryReturning(TriggerMatchContext match, TriggerContext ctx,
            CardEffect effect, Integer eventValue, UUID enchantedControllerId) {
        TriggerContext.EnchantedPermanentDeath epd = ctx instanceof TriggerContext.EnchantedPermanentDeath death
                ? death : null;
        if (eventValue == null && epd != null) {
            eventValue = Math.max(0, epd.dyingCreaturePower());
        }
        if (effect.targetSpec().admits(TargetPredicate.Kind.PERMANENT) || effect.targetSpec().admits(TargetPredicate.Kind.PLAYER)
                || effect.targetSpec().admits(TargetPredicate.Kind.GRAVEYARD_CARD)) {
            PermanentChoiceContext.DeathTriggerTarget target = new PermanentChoiceContext.DeathTriggerTarget(
                    match.permanent().getCard(), match.controllerId(), new ArrayList<>(List.of(effect)), eventValue,
                    new Permanent(match.permanent()), false);
            match.gameData().queueInteraction(epd == null || epd.dyingPermanent() == null ? target
                    : target.withTriggeringPermanent(epd.dyingPermanent())
                            .withTriggeringPermanentController(epd.dyingPermanentControllerId()));
            return null;
        }

        StackEntry entry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                match.permanent().getCard(),
                match.controllerId(),
                match.permanent().getCard().getName() + "'s ability",
                new ArrayList<>(List.of(effect))
        );
        entry.setSourcePermanentSnapshot(new Permanent(match.permanent()));
        if (returnsSourceCardFromGraveyard(effect)) {
            // The aura is still on the battlefield while this is queued and enters the graveyard in the
            // same state-based pass; GameData.markGraveyardEntry binds that stay's identity to this entry.
            entry.setTriggeringCardId(match.permanent().getCard().getId());
        }
        if (enchantedControllerId != null) {
            entry.setTargetId(enchantedControllerId);
            entry.setNonTargeting(true);
        }
        // The field is a primitive, so a null would unbox and throw.
        if (eventValue != null) {
            entry.setEventValue(eventValue);
        }
        if (epd != null) {
            recordEnchantedPermanent(match.gameData(), entry, epd);
        }
        match.gameData().stack.add(entry);
        gameLogService.append(match.gameData(), GameLog.cardThen(match.permanent().getCard(),
                "'s ability triggers (enchanted permanent put into graveyard)."));
        log.info("Game {} - {} triggers (enchanted permanent put into graveyard)", match.gameData().id, match.permanent().getCard().getName());
        return entry;
    }

    /**
     * Records the enchanted permanent that was put into a graveyard as {@code entry}'s triggering permanent: its
     * last-known snapshot, controller, owner, power and toughness (CR 603.10a). The effects read them as the ability
     * resolves ({@code TRIGGERING_PERMANENT_CONTROLLER} recipients, {@code TriggeringPermanentPower} /
     * {@code TriggeringPermanentToughness} amounts). The cards it put into the graveyard (every card of a merged
     * permanent) are the entry's triggering cards, each with the graveyard incarnation it has now (CR 400.7).
     */
    private static void recordEnchantedPermanent(GameData gameData, StackEntry entry,
                                                 TriggerContext.EnchantedPermanentDeath epd) {
        entry.setTriggeringPermanentId(epd.dyingPermanentId());
        if (epd.dyingPermanent() != null) {
            entry.setTriggeringPermanent(epd.dyingPermanent());
        }
        entry.setTriggeringPermanentControllerId(epd.dyingPermanentControllerId());
        entry.setTriggeringPermanentOwnerId(epd.dyingPermanentOwnerId());
        entry.setTriggeringPermanentPowerAtTrigger(epd.dyingCreaturePower());
        entry.setTriggeringPermanentToughnessAtTrigger(epd.dyingCreatureToughness());
        List<Card> dyingCards = epd.dyingPermanent() != null ? epd.dyingPermanent().cardsLeavingBattlefield()
                : epd.dyingCard() != null ? List.of(epd.dyingCard()) : List.of();
        entry.setTriggeringCards(dyingCards);
        for (Card card : dyingCards) {
            entry.getEventCardGraveyardEntryVersions().put(card.getId(), gameData.graveyardEntryVersion(card.getId()));
        }
    }

    /**
     * Whether {@code effect} returns the source Aura from its graveyard ("return this card to its owner's hand",
     * "return it transformed", Puppet Master's paid follow-up), so the entry's triggering card is the Aura, whose
     * graveyard stay the return checks.
     */
    private static boolean returnsSourceCardFromGraveyard(CardEffect effect) {
        if (effect instanceof SequenceEffect sequence) {
            return sequence.steps().stream().anyMatch(DeathTriggerCollectorService::returnsSourceCardFromGraveyard);
        }
        return effect instanceof ReturnSourceCardFromGraveyardToOwnerHandEffect
                || effect instanceof ReturnSourceTransformedFromGraveyardEffect
                || effect instanceof ReturnEnchantedCreatureToOwnerHandOnDeathEffect returnToHand
                && returnToHand.followUpManaCost() != null;
    }

    // ── ON_ENCHANTED_PERMANENT_LEAVES_BATTLEFIELD ──────────────────────

    /**
     * "When enchanted creature leaves the battlefield": the entry records the permanent that left as its triggering
     * permanent, as it last existed on the battlefield, and its controller then (CR 603.10a), which "its controller"
     * reads as the ability resolves (Funeral March's sacrifice, Traveling Plague's return). When it was put into a
     * graveyard or exiled its card is the entry's triggering card, with its incarnation in that zone (CR 400.7), which
     * "return it" reads (Kaya's Ghostform).
     */
    @CollectsWatchTrigger(value = CardEffect.class, watch = ZoneChangeWatch.ON_ENCHANTED_PERMANENT_LEAVES_BATTLEFIELD)
    boolean handleEnchantedPermanentLeavesDefault(TriggerMatchContext match,
            CardEffect effect, TriggerContext ctx) {
        StackEntry entry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                match.permanent().getCard(),
                match.controllerId(),
                match.permanent().getCard().getName() + "'s ability",
                new ArrayList<>(List.of(effect))
        );
        if (ctx instanceof TriggerContext.EnchantedPermanentLeaves leaves && leaves.leavingPermanent() != null) {
            entry.setTriggeringPermanentId(leaves.leavingPermanent().getId());
            entry.setTriggeringPermanent(leaves.leavingPermanent());
            entry.setTriggeringPermanentControllerId(leaves.leavingControllerId());
            UUID cardId = leaves.leavingPermanent().getCard().getId();
            if (leaves.destination() == com.github.laxika.magicalvibes.model.Zone.GRAVEYARD) {
                entry.setTriggeringCard(cardId, match.gameData().graveyardEntryVersion(cardId));
            } else if (leaves.destination() == com.github.laxika.magicalvibes.model.Zone.EXILE) {
                entry.setTriggeringCardId(cardId);
                entry.setTriggeringCardExileEntryVersion(
                        match.gameData().exileEntryVersions.getOrDefault(cardId, 0L));
            }
        }
        match.gameData().stack.add(entry);
        logEnchantedPermanentLTB(match);
        return true;
    }

    private void logEnchantedPermanentLTB(TriggerMatchContext match) {
        gameLogService.append(match.gameData(), GameLog.cardThen(match.permanent().getCard(),
                "'s ability triggers (enchanted permanent left the battlefield)."));
        log.info("Game {} - {} triggers (enchanted permanent left the battlefield)", match.gameData().id, match.permanent().getCard().getName());
    }

    // ── ON_ANY_ARTIFACT_PUT_INTO_GRAVEYARD_FROM_BATTLEFIELD ────────────

    @CollectsWatchTrigger(value = DealDamageToPlayersEffect.class,
            watch = ZoneChangeWatch.ON_ANY_NONCREATURE_ARTIFACT_SACRIFICED_OR_DESTROYED)
    boolean handleNoncreatureArtifactSacrificedOrDestroyedDamage(TriggerMatchContext match,
            DealDamageToPlayersEffect effect, TriggerContext ctx) {
        Card sourceCard = match.permanent().getCard();
        match.gameData().enqueueTrigger(new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                sourceCard,
                match.controllerId(),
                sourceCard.getName() + "'s ability",
                new ArrayList<>(List.of(effect)),
                null,
                match.permanent().getId()));
        gameLogService.append(match.gameData(), GameLog.abilityTriggers(sourceCard));
        log.info("Game {} - {} triggers on a noncreature artifact being sacrificed or destroyed",
                match.gameData().id, sourceCard.getName());
        return true;
    }

    @CollectsWatchTrigger(value = ReturnTriggeringArtifactToOwnerHandUnlessTargetTakesDamageEffect.class,
            watch = ZoneChangeWatch.ON_ANY_ARTIFACT_PUT_INTO_GRAVEYARD_FROM_BATTLEFIELD)
    boolean handleArtifactGraveyardReturnUnlessDamage(TriggerMatchContext match,
            ReturnTriggeringArtifactToOwnerHandUnlessTargetTakesDamageEffect effect, TriggerContext ctx) {
        TriggerContext.ArtifactGraveyard artifactGraveyard = (TriggerContext.ArtifactGraveyard) ctx;
        Card artifactCard = artifactGraveyard.artifactCard();
        if (artifactCard == null || artifactCard.isToken()
                || !match.controllerId().equals(artifactGraveyard.graveyardOwnerId())) {
            return false;
        }

        // "That card" is the entry's triggering card.
        match.gameData().queueInteraction(new PermanentChoiceContext.SpellTargetTriggerAnyTarget(
                match.permanent().getCard(), match.controllerId(), new ArrayList<>(List.of(effect)),
                true, match.permanent().getCard().getTargetFilter(), 0, match.permanent().getId(),
                new Permanent(match.permanent()), false, null, null, match.controllerId(), null, null, false,
                artifactCard.getId()));
        logArtifactGraveyard(match);
        return true;
    }

    @CollectsWatchTrigger(value = MayEffect.class, watch = ZoneChangeWatch.ON_ANY_ARTIFACT_PUT_INTO_GRAVEYARD_FROM_BATTLEFIELD)
    boolean handleArtifactGraveyardMay(TriggerMatchContext match,
            MayEffect may, TriggerContext ctx) {
        // A "you may" wrapping a targeted effect (Disciple of the Vault) chooses its target as the
        // trigger goes on the stack (CR 603.3d), before the may decision is asked on resolution.
        boolean targeted = may.targetSpec().admits(TargetPredicate.Kind.PERMANENT)
                || may.targetSpec().admits(TargetPredicate.Kind.PLAYER)
                || graveyardTargetingSupport.findTarget(List.of(may)) != null;
        if (targeted) {
            match.gameData().queueInteraction(new PermanentChoiceContext.DeathTriggerTarget(
                    match.permanent().getCard(), match.controllerId(), new ArrayList<>(List.of(may)),
                    null, new Permanent(match.permanent()), match.permanent().getCard().getTargetFilter()));
            logArtifactGraveyard(match);
            return true;
        }
        match.gameData().queueMayAbility(match.permanent().getCard(), match.controllerId(), may,
                null, match.permanent().getId());
        logArtifactGraveyard(match);
        return true;
    }

    @CollectsWatchTrigger(value = DealDamageToPlayersEffect.class, watch = ZoneChangeWatch.ON_ANY_ARTIFACT_PUT_INTO_GRAVEYARD_FROM_BATTLEFIELD)
    boolean handleArtifactGraveyardDamageController(TriggerMatchContext match,
            DealDamageToPlayersEffect effect, TriggerContext ctx) {
        TriggerContext.ArtifactGraveyard ag = (TriggerContext.ArtifactGraveyard) ctx;
        // "Whenever another artifact is put into a graveyard from the battlefield" (Magnetic Mine).
        if (ag.artifactCard() != null && (ag.artifactCard().getId().equals(match.permanent().getCard().getId())
                || ag.artifactCard().getId().equals(match.permanent().getOriginalCard().getId()))) {
            return false;
        }
        match.gameData().stack.add(new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                match.permanent().getCard(),
                match.controllerId(),
                match.permanent().getCard().getName() + "'s ability",
                new ArrayList<>(List.of(effect)),
                ag.artifactControllerId(),
                match.permanent().getId()
        ));
        logArtifactGraveyard(match);
        return true;
    }

    /**
     * "Whenever an artifact [you control] is put into a graveyard from the battlefield": the artifact, as it last
     * existed on the battlefield (CR 603.10a), is the stack entry's triggering permanent and the source of a graveyard
     * target's mana value limit, and its target is chosen as it goes on the stack (CR 603.3d).
     */
    @CollectsWatchTrigger(value = CardEffect.class, watch = ZoneChangeWatch.ON_ANY_ARTIFACT_PUT_INTO_GRAVEYARD_FROM_BATTLEFIELD)
    boolean handleArtifactGraveyardDefault(TriggerMatchContext match,
            CardEffect effect, TriggerContext ctx) {
        TriggerContext.ArtifactGraveyard ag = (TriggerContext.ArtifactGraveyard) ctx;
        CardEffect triggerEffect = effect;
        if (triggerEffect.targetSpec().admits(TargetPredicate.Kind.GRAVEYARD_CARD)
                || graveyardTargetingSupport.findTarget(List.of(triggerEffect)) != null) {
            match.gameData().queueInteraction(new PermanentChoiceContext.DeathTriggerTarget(
                    match.permanent().getCard(), match.controllerId(), new ArrayList<>(List.of(triggerEffect)))
                    .withTriggeringPermanent(ag.artifactPermanent())
                    .withGraveyardTargetMaxManaValue(graveyardTargetMaxManaValueAtDeath(
                            match.gameData(), match.controllerId(), triggerEffect, ag.artifactPermanent())));
            logArtifactGraveyard(match);
            return true;
        }

        if (triggerEffect.targetSpec().declaredTarget() == null) {
            StackEntry entry = new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    match.permanent().getCard(),
                    match.controllerId(),
                    match.permanent().getCard().getName() + "'s ability",
                    new ArrayList<>(List.of(triggerEffect)),
                    null,
                    match.permanent().getId()
            );
            entry.setTriggeringPermanent(ag.artifactPermanent());
            match.gameData().stack.add(entry);
            logArtifactGraveyard(match);
            return true;
        }

        boolean playerTargetOnly = !triggerEffect.targetSpec().admits(TargetPredicate.Kind.PERMANENT);
        // "up to one target" (minimum 0): the controller may decline by choosing themselves.
        int targetIndex = match.permanent().getCard().getEffectTargetIndex(match.rawEffect());
        boolean optionalTarget = targetIndex >= 0
                && targetIndex < match.permanent().getCard().getTargetGroups().size()
                && match.permanent().getCard().getTargetGroups().get(targetIndex).getMinTargets() == 0;
        match.gameData().queueInteraction(new PermanentChoiceContext.SpellTargetTriggerAnyTarget(
                match.permanent().getCard(), match.controllerId(),
                new ArrayList<>(List.of(triggerEffect)), playerTargetOnly,
                match.permanent().getCard().getTargetFilter(), 0, match.permanent().getId(),
                new Permanent(match.permanent()), optionalTarget, null, null, match.controllerId(),
                null, null).withDepartedPermanent(ag.artifactPermanent()));
        logArtifactGraveyard(match);
        return true;
    }

    @CollectsWatchTrigger(value = CardEffect.class, watch = ZoneChangeWatch.ON_ANY_ARTIFACT_EXILED_FROM_BATTLEFIELD)
    boolean handleArtifactExiledDefault(TriggerMatchContext match,
                                        CardEffect effect, TriggerContext ctx) {
        match.gameData().stack.add(new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                match.permanent().getCard(),
                match.controllerId(),
                match.permanent().getCard().getName() + "'s ability",
                new ArrayList<>(List.of(effect)),
                null,
                match.permanent().getId()
        ));
        gameLogService.append(match.gameData(), GameLog.abilityTriggers(match.permanent().getCard()));
        log.info("Game {} - {} triggers (artifact exiled from battlefield)",
                match.gameData().id, match.permanent().getCard().getName());
        return true;
    }

    private void logArtifactGraveyard(TriggerMatchContext match) {
        gameLogService.append(match.gameData(), GameLog.abilityTriggers(match.permanent().getCard()));
        log.info("Game {} - {} triggers (artifact put into graveyard from battlefield)", match.gameData().id, match.permanent().getCard().getName());
    }

    // ── ON_ANY_LAND_PUT_INTO_GRAVEYARD_FROM_BATTLEFIELD ────────────────

    @CollectsWatchTrigger(value = DealDamageToPlayersEffect.class, watch = ZoneChangeWatch.ON_ANY_LAND_PUT_INTO_GRAVEYARD_FROM_BATTLEFIELD)
    boolean handleLandGraveyardDamageController(TriggerMatchContext match,
            DealDamageToPlayersEffect effect, TriggerContext ctx) {
        TriggerContext.AnyLandGraveyard lg = (TriggerContext.AnyLandGraveyard) ctx;
        match.gameData().stack.add(new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                match.permanent().getCard(),
                match.controllerId(),
                match.permanent().getCard().getName() + "'s ability",
                new ArrayList<>(List.of(effect)),
                lg.landControllerId(),
                match.permanent().getId()
        ));
        logLandGraveyard(match);
        return true;
    }

    /** The land's controller rides along for "that land's controller" (Thendar, the Overminer). */
    @CollectsWatchTrigger(value = CardEffect.class, watch = ZoneChangeWatch.ON_ANY_LAND_PUT_INTO_GRAVEYARD_FROM_BATTLEFIELD)
    boolean handleLandGraveyardDefault(TriggerMatchContext match,
            CardEffect effect, TriggerContext ctx) {
        TriggerContext.AnyLandGraveyard land = (TriggerContext.AnyLandGraveyard) ctx;
        StackEntry entry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                match.permanent().getCard(),
                match.controllerId(),
                match.permanent().getCard().getName() + "'s ability",
                new ArrayList<>(List.of(effect)),
                null,
                match.permanent().getId()
        );
        entry.setTriggeringPermanentControllerId(land.landControllerId());
        match.gameData().stack.add(entry);
        logLandGraveyard(match);
        return true;
    }

    private void logLandGraveyard(TriggerMatchContext match) {
        gameLogService.append(match.gameData(), GameLog.abilityTriggers(match.permanent().getCard()));
        log.info("Game {} - {} triggers (land put into graveyard from battlefield)", match.gameData().id, match.permanent().getCard().getName());
    }

    // ── ON_ANY_ENCHANTMENT_PUT_INTO_GRAVEYARD_FROM_BATTLEFIELD ─────────

    @CollectsWatchTrigger(value = CardEffect.class, watch = ZoneChangeWatch.ON_ANY_ENCHANTMENT_PUT_INTO_GRAVEYARD_FROM_BATTLEFIELD)
    boolean handleEnchantmentGraveyardDefault(TriggerMatchContext match,
            CardEffect effect, TriggerContext ctx) {
        match.gameData().stack.add(new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                match.permanent().getCard(),
                match.controllerId(),
                match.permanent().getCard().getName() + "'s ability",
                new ArrayList<>(List.of(effect)),
                null,
                match.permanent().getId()
        ));
        logEnchantmentGraveyard(match);
        return true;
    }

    @CollectsWatchTrigger(value = CardEffect.class, watch = ZoneChangeWatch.ON_ALLY_ENCHANTMENT_PUT_INTO_GRAVEYARD_FROM_BATTLEFIELD)
    boolean handleAllyEnchantmentGraveyardDefault(TriggerMatchContext match,
            CardEffect effect, TriggerContext ctx) {
        match.gameData().stack.add(new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                match.permanent().getCard(),
                match.controllerId(),
                match.permanent().getCard().getName() + "'s ability",
                new ArrayList<>(List.of(effect)),
                null,
                match.permanent().getId()
        ));
        logEnchantmentGraveyard(match);
        return true;
    }

    private void logEnchantmentGraveyard(TriggerMatchContext match) {
        gameLogService.append(match.gameData(), GameLog.abilityTriggers(match.permanent().getCard()));
        log.info("Game {} - {} triggers (enchantment put into graveyard from battlefield)", match.gameData().id, match.permanent().getCard().getName());
    }

    // ── ON_ARTIFACT_PUT_INTO_OPPONENT_GRAVEYARD_FROM_BATTLEFIELD ───────

    @CollectsWatchTrigger(value = MayEffect.class, watch = ZoneChangeWatch.ON_ARTIFACT_PUT_INTO_OPPONENT_GRAVEYARD_FROM_BATTLEFIELD)
    @CollectsWatchTrigger(value = MayEffect.class, watch = ZoneChangeWatch.ON_ARTIFACT_PUT_INTO_OPPONENT_OWNER_GRAVEYARD_FROM_BATTLEFIELD)
    boolean handleOpponentArtifactGraveyardMay(TriggerMatchContext match,
            MayEffect may, TriggerContext ctx) {
        match.gameData().queueMayAbility(match.permanent().getCard(), match.controllerId(), may);
        logOpponentArtifactGraveyard(match);
        return true;
    }

    @CollectsWatchTrigger(value = CardEffect.class, watch = ZoneChangeWatch.ON_ARTIFACT_PUT_INTO_OPPONENT_GRAVEYARD_FROM_BATTLEFIELD)
    @CollectsWatchTrigger(value = CardEffect.class, watch = ZoneChangeWatch.ON_ARTIFACT_PUT_INTO_OPPONENT_OWNER_GRAVEYARD_FROM_BATTLEFIELD)
    boolean handleOpponentArtifactGraveyardDefault(TriggerMatchContext match,
            CardEffect effect, TriggerContext ctx) {
        match.gameData().stack.add(new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                match.permanent().getCard(),
                match.controllerId(),
                match.permanent().getCard().getName() + "'s ability",
                new ArrayList<>(List.of(effect)),
                null,
                match.permanent().getId()
        ));
        logOpponentArtifactGraveyard(match);
        return true;
    }

    @CollectsWatchTrigger(value = DealDamageToPlayersEffect.class,
            watch = ZoneChangeWatch.ON_ARTIFACT_PUT_INTO_OPPONENT_GRAVEYARD_FROM_BATTLEFIELD)
    boolean handleOpponentArtifactGraveyardDamage(TriggerMatchContext match,
            DealDamageToPlayersEffect effect, TriggerContext ctx) {
        if (effect.recipient() != DamageRecipient.TRIGGERING_PERMANENT_CONTROLLER) {
            return handleOpponentArtifactGraveyardDefault(match, effect, ctx);
        }

        TriggerContext.ArtifactGraveyard artifactGraveyard = (TriggerContext.ArtifactGraveyard) ctx;
        StackEntry entry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                match.permanent().getCard(),
                match.controllerId(),
                match.permanent().getCard().getName() + "'s ability",
                new ArrayList<>(List.of(effect)),
                artifactGraveyard.artifactControllerId(),
                match.permanent().getId()
        );
        match.gameData().stack.add(entry);
        logOpponentArtifactGraveyard(match);
        return true;
    }

    private void logOpponentArtifactGraveyard(TriggerMatchContext match) {
        gameLogService.append(match.gameData(), GameLog.abilityTriggers(match.permanent().getCard()));
        log.info("Game {} - {} triggers (opponent artifact put into graveyard from battlefield)", match.gameData().id, match.permanent().getCard().getName());
    }

    // ── ON_BLACK_CARD_PUT_INTO_OPPONENT_GRAVEYARD_FROM_ANYWHERE ────────

    @CollectsWatchTrigger(value = MayEffect.class, watch = ZoneChangeWatch.ON_BLACK_CARD_PUT_INTO_OPPONENT_GRAVEYARD_FROM_ANYWHERE)
    boolean handleBlackCardOpponentGraveyardMay(TriggerMatchContext match,
            MayEffect may, TriggerContext ctx) {
        match.gameData().queueMayAbility(match.permanent().getCard(), match.controllerId(), may);
        logBlackCardOpponentGraveyard(match);
        return true;
    }

    @CollectsWatchTrigger(value = CardEffect.class, watch = ZoneChangeWatch.ON_BLACK_CARD_PUT_INTO_OPPONENT_GRAVEYARD_FROM_ANYWHERE)
    boolean handleBlackCardOpponentGraveyardDefault(TriggerMatchContext match,
            CardEffect effect, TriggerContext ctx) {
        match.gameData().stack.add(new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                match.permanent().getCard(),
                match.controllerId(),
                match.permanent().getCard().getName() + "'s ability",
                new ArrayList<>(List.of(effect)),
                null,
                match.permanent().getId()
        ));
        logBlackCardOpponentGraveyard(match);
        return true;
    }

    private void logBlackCardOpponentGraveyard(TriggerMatchContext match) {
        gameLogService.append(match.gameData(), GameLog.abilityTriggers(match.permanent().getCard()));
        log.info("Game {} - {} triggers (black card put into opponent's graveyard from anywhere)", match.gameData().id, match.permanent().getCard().getName());
    }

    // ── ON_ALLY_LAND_PUT_INTO_GRAVEYARD_BY_OPPONENT ────────────────────

    @CollectsWatchTrigger(value = ReturnTriggeringLandFromGraveyardToBattlefieldEffect.class,
            watch = ZoneChangeWatch.ON_ALLY_LAND_PUT_INTO_GRAVEYARD_BY_OPPONENT)
    boolean handleLandGraveyardReturn(TriggerMatchContext match,
            ReturnTriggeringLandFromGraveyardToBattlefieldEffect effect, TriggerContext ctx) {
        TriggerContext.LandPutIntoGraveyard lpg = (TriggerContext.LandPutIntoGraveyard) ctx;
        Card landCard = lpg.landCard();
        StackEntry entry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                match.permanent().getCard(),
                match.controllerId(),
                match.permanent().getCard().getName() + "'s ability",
                new ArrayList<>(List.of(effect)),
                null,
                match.permanent().getId()
        );
        // "That card" is the entry's triggering card, as the graveyard object the event put there (CR 400.7).
        entry.setTriggeringCard(landCard.getId(), match.gameData().graveyardEntryVersion(landCard.getId()));
        match.gameData().stack.add(entry);

        gameLogService.append(match.gameData(), GameLog.cardTextCard(match.permanent().getCard(),
                "'s ability triggers (", landCard, " was put into a graveyard from the battlefield by an opponent)."));
        log.info("Game {} - {} triggers (land {} put into graveyard by opponent)",
                match.gameData().id, match.permanent().getCard().getName(), landCard.getName());
        return true;
    }

    // ── ON_OPPONENT_PERMANENT_PUT_INTO_GRAVEYARD_FROM_BATTLEFIELD ──────

    @CollectsWatchTrigger(value = PutCountersOnSelfEffect.class,
            watch = ZoneChangeWatch.ON_OPPONENT_PERMANENT_PUT_INTO_GRAVEYARD_FROM_BATTLEFIELD)
    private boolean handleOpponentPermanentGraveyardPutCountersOnSelf(TriggerMatchContext match,
            PutCountersOnSelfEffect effect, TriggerContext ctx) {
        match.gameData().stack.add(new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                match.permanent().getCard(),
                match.controllerId(),
                match.permanent().getCard().getName() + "'s ability",
                new ArrayList<>(List.of(effect)),
                null,
                match.permanent().getId()));
        gameLogService.append(match.gameData(), GameLog.abilityTriggers(match.permanent().getCard()));
        return true;
    }

    @CollectsWatchTrigger(value = CardEffect.class,
            watch = ZoneChangeWatch.ON_OPPONENT_PERMANENT_PUT_INTO_GRAVEYARD_FROM_BATTLEFIELD)
    boolean handleOpponentPermanentGraveyardDefault(TriggerMatchContext match,
            CardEffect effect, TriggerContext ctx) {
        match.gameData().stack.add(new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                match.permanent().getCard(),
                match.controllerId(),
                match.permanent().getCard().getName() + "'s ability",
                new ArrayList<>(List.of(effect)),
                null,
                match.permanent().getId()));
        gameLogService.append(match.gameData(), GameLog.abilityTriggers(match.permanent().getCard()));
        return true;
    }

    @CollectsWatchTrigger(value = MayEffect.class,
            watch = ZoneChangeWatch.ON_ALLY_NONCREATURE_PERMANENT_DESTROYED_BY_OPPONENT)
    private boolean handleNoncreaturePermanentDestroyedByOpponent(TriggerMatchContext match,
            MayEffect may, TriggerContext ctx) {
        GameData gameData = match.gameData();
        gameData.queueInteraction(new PermanentChoiceContext.SpellTargetTriggerAnyTarget(
                match.permanent().getCard(), match.controllerId(), new ArrayList<>(List.of(may)), false,
                match.permanent().getCard().getTargetFilter(), 0, match.permanent().getId()));
        gameLogService.append(gameData, GameLog.abilityTriggers(match.permanent().getCard()));
        log.info("Game {} - {} triggers after an opponent destroys a noncreature permanent",
                gameData.id, match.permanent().getCard().getName());
        return true;
    }

    @CollectsWatchTrigger(value = StealDyingOpponentPermanentUnlessPaysLifeEffect.class,
            watch = ZoneChangeWatch.ON_OPPONENT_PERMANENT_PUT_INTO_GRAVEYARD_FROM_BATTLEFIELD)
    boolean handleOpponentPermanentGraveyardSteal(TriggerMatchContext match,
            StealDyingOpponentPermanentUnlessPaysLifeEffect effect, TriggerContext ctx) {
        TriggerContext.OpponentPermanentGraveyard opg = (TriggerContext.OpponentPermanentGraveyard) ctx;
        // "That card" is the entry's triggering card and "that opponent" the controller of its triggering permanent.
        // The "unless pays" branch is a resolution-time choice made by that opponent (CR 603.5), so this stacks as a
        // plain triggered ability.
        StackEntry entry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                match.permanent().getCard(),
                match.controllerId(),
                match.permanent().getCard().getName() + "'s ability",
                new ArrayList<>(List.of(effect))
        );
        markDyingCard(match.gameData(), entry, opg.dyingCard());
        entry.setTriggeringPermanent(opg.dyingPermanent());
        entry.setTriggeringPermanentControllerId(opg.dyingControllerId());
        match.gameData().stack.add(entry);
        gameLogService.append(match.gameData(), GameLog.abilityTriggers(match.permanent().getCard()));
        log.info("Game {} - {} triggers (opponent permanent put into graveyard)",
                match.gameData().id, match.permanent().getCard().getName());
        return true;
    }

    /** "Whenever a creature is put into your graveyard from the battlefield": "that card" is the entry's triggering card. */
    @CollectsWatchTrigger(value = CardEffect.class,
            watch = ZoneChangeWatch.ON_CREATURE_PUT_INTO_CONTROLLER_GRAVEYARD_FROM_BATTLEFIELD)
    boolean handleCreaturePutIntoOwnerGraveyardDefault(TriggerMatchContext match, CardEffect effect,
            TriggerContext ctx) {
        var death = (TriggerContext.AnyPermanentGraveyard) ctx;
        StackEntry entry = new StackEntry(StackEntryType.TRIGGERED_ABILITY,
                match.permanent().getCard(), match.controllerId(), match.permanent().getCard().getName() + "'s ability",
                new ArrayList<>(List.of(effect)),
                null, match.permanent().getId());
        markDyingCard(match.gameData(), entry, death.dyingCard());
        match.gameData().enqueueTrigger(entry);
        gameLogService.append(match.gameData(), GameLog.abilityTriggers(match.permanent().getCard()));
        return true;
    }

    @CollectsWatchTrigger(value = MayPayManaEffect.class,
            watch = ZoneChangeWatch.ON_CREATURE_PUT_INTO_CONTROLLER_GRAVEYARD_FROM_BATTLEFIELD)
    boolean handleCreaturePutIntoOwnerGraveyardMayPay(TriggerMatchContext match,
            MayPayManaEffect mayPay, TriggerContext ctx) {
        TriggerContext.AnyPermanentGraveyard apg = (TriggerContext.AnyPermanentGraveyard) ctx;
        match.gameData().queueMayAbility(match.permanent().getCard(), apg.graveyardOwnerId(), mayPay, null);
        // The payment may be answered after the card has left and re-entered a graveyard; the
        // trigger only refers to the graveyard stay it was created for (CR 400.7).
        markDyingCard(match.gameData(), match.gameData().stack.getLast(), apg.dyingCard());
        return true;
    }

    @CollectsWatchTrigger(value = CreateTokenEffect.class,
            watch = ZoneChangeWatch.ON_CREATURE_PUT_INTO_CONTROLLER_GRAVEYARD_FROM_BATTLEFIELD)
    boolean handleCreaturePutIntoOwnerGraveyardCreateToken(TriggerMatchContext match,
            CreateTokenEffect effect, TriggerContext ctx) {
        StackEntry entry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                match.permanent().getCard(),
                match.controllerId(),
                match.permanent().getCard().getName() + "'s ability",
                new ArrayList<>(List.of(effect)),
                null,
                match.permanent().getId()
        );
        match.gameData().stack.add(entry);
        return true;
    }

    @CollectsWatchTrigger(value = ReturnDyingCreatureToOwnerHandUnlessTargetPaysLifeEffect.class,
            watch = ZoneChangeWatch.ON_CREATURE_PUT_INTO_CONTROLLER_GRAVEYARD_FROM_BATTLEFIELD)
    boolean handleDyingCreatureReturnUnlessTargetPaysLife(TriggerMatchContext match,
            ReturnDyingCreatureToOwnerHandUnlessTargetPaysLifeEffect effect, TriggerContext ctx) {
        TriggerContext.AnyPermanentGraveyard apg = (TriggerContext.AnyPermanentGraveyard) ctx;
        // "It" is the entry's triggering card; the target is chosen as the ability goes on the stack (CR 603.3d).
        match.gameData().queueInteraction(new PermanentChoiceContext.DeathTriggerTarget(
                match.permanent().getCard(), match.controllerId(), new ArrayList<>(List.of(effect)),
                null, new Permanent(match.permanent()), match.permanent().getCard().getTargetFilter())
                .withTriggeringCard(apg.dyingCard().getId(),
                        match.gameData().graveyardEntryVersion(apg.dyingCard().getId())));
        gameLogService.append(match.gameData(), GameLog.abilityTriggers(match.permanent().getCard()));
        log.info("Game {} - {} triggers (owned creature put into graveyard)",
                match.gameData().id, match.permanent().getCard().getName());
        return true;
    }

    @CollectsWatchTrigger(value = CardEffect.class,
            watch = ZoneChangeWatch.ON_ANY_PERMANENT_PUT_INTO_GRAVEYARD_FROM_BATTLEFIELD)
    boolean handleAnyPermanentGraveyardDefault(TriggerMatchContext match,
            CardEffect effect, TriggerContext ctx) {
        TriggerContext.AnyPermanentGraveyard apg = (TriggerContext.AnyPermanentGraveyard) ctx;
        StackEntry entry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                match.permanent().getCard(),
                match.controllerId(),
                match.permanent().getCard().getName() + "'s ability",
                new ArrayList<>(List.of(effect)),
                apg.graveyardOwnerId(),
                match.permanent().getId()
        );
        markDyingCard(match.gameData(), entry, apg.dyingCard());
        match.gameData().stack.add(entry);
        gameLogService.append(match.gameData(), GameLog.abilityTriggers(match.permanent().getCard()));
        log.info("Game {} - {} triggers (permanent put into a graveyard from the battlefield)",
                match.gameData().id, match.permanent().getCard().getName());
        return true;
    }

    boolean handleAllyArtifactOrCreatureDeath(TriggerMatchContext match, CardEffect effect, TriggerContext ctx) {
        match.gameData().enqueueTrigger(new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                match.permanent().getCard(),
                match.controllerId(),
                match.permanent().getCard().getName() + "'s ability",
                new ArrayList<>(List.of(effect)),
                null,
                match.permanent().getId()
        ));
        gameLogService.append(match.gameData(), GameLog.abilityTriggers(match.permanent().getCard()));
        log.info("Game {} - {} triggers (controlled artifact or creature put into a graveyard from the battlefield)",
                match.gameData().id, match.permanent().getCard().getName());
        return true;
    }

    @CollectsWatchTrigger(value = CardEffect.class,
            watch = ZoneChangeWatch.ON_ANY_NONTOKEN_PERMANENT_PUT_INTO_GRAVEYARD_FROM_BATTLEFIELD)
    boolean handleAnyNontokenPermanentGraveyardDefault(TriggerMatchContext match,
            CardEffect effect, TriggerContext ctx) {
        TriggerContext.AnyPermanentGraveyard apg = (TriggerContext.AnyPermanentGraveyard) ctx;
        StackEntry entry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                match.permanent().getCard(),
                match.controllerId(),
                match.permanent().getCard().getName() + "'s ability",
                new ArrayList<>(List.of(effect)),
                apg.graveyardOwnerId(),
                match.permanent().getId()
        );
        // The graveyard's owner is "that permanent's owner", not a target of the ability.
        entry.setNonTargeting(true);
        markDyingCard(match.gameData(), entry, apg.dyingCard());
        match.gameData().stack.add(entry);
        gameLogService.append(match.gameData(), GameLog.abilityTriggers(match.permanent().getCard()));
        log.info("Game {} - {} triggers (nontoken permanent put into a graveyard from the battlefield)",
                match.gameData().id, match.permanent().getCard().getName());
        return true;
    }

    @CollectsWatchTrigger(value = CardEffect.class,
            watch = ZoneChangeWatch.ON_ALLY_NONTOKEN_PERMANENT_PUT_INTO_GRAVEYARD_FROM_BATTLEFIELD)
    boolean handleAllyNontokenPermanentGraveyardDefault(TriggerMatchContext match,
            CardEffect effect, TriggerContext ctx) {
        StackEntry entry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                match.permanent().getCard(),
                match.controllerId(),
                match.permanent().getCard().getName() + "'s ability",
                new ArrayList<>(List.of(effect)),
                null,
                match.permanent().getId()
        );
        markDyingCard(match.gameData(), entry, ((TriggerContext.AnyPermanentGraveyard) ctx).dyingCard());
        match.gameData().stack.add(entry);
        gameLogService.append(match.gameData(), GameLog.abilityTriggers(match.permanent().getCard()));
        log.info("Game {} - {} triggers (nontoken permanent put into controller's graveyard)",
                match.gameData().id, match.permanent().getCard().getName());
        return true;
    }

    @CollectsWatchTrigger(value = CardEffect.class,
            watch = ZoneChangeWatch.ON_ALLY_PERMANENT_PUT_INTO_GRAVEYARD_FROM_BATTLEFIELD)
    @CollectsWatchTrigger(value = CardEffect.class,
            watch = ZoneChangeWatch.ON_PERMANENT_PUT_INTO_YOUR_GRAVEYARD_FROM_BATTLEFIELD)
    boolean handleAllyPermanentGraveyardDefault(TriggerMatchContext match,
            CardEffect effect, TriggerContext ctx) {
        if (effect.targetSpec().declaredTarget() != null) {
            match.gameData().queueInteraction(new PermanentChoiceContext.DeathTriggerTarget(
                    match.permanent().getCard(), match.controllerId(), new ArrayList<>(List.of(effect)),
                    null, new Permanent(match.permanent()), false));
            gameLogService.append(match.gameData(), GameLog.abilityTriggers(match.permanent().getCard()));
            return true;
        }
        StackEntry entry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                match.permanent().getCard(),
                match.controllerId(),
                match.permanent().getCard().getName() + "'s ability",
                new ArrayList<>(List.of(effect)),
                null,
                match.permanent().getId()
        );
        if (ctx instanceof TriggerContext.AnyPermanentGraveyard death && death.dyingPermanent() != null) {
            // "It": the permanent put into the graveyard, as it last existed (Martyr's Bond).
            entry.setTriggeringPermanent(death.dyingPermanent());
        }
        match.gameData().stack.add(entry);
        gameLogService.append(match.gameData(), GameLog.abilityTriggers(match.permanent().getCard()));
        log.info("Game {} - {} triggers (permanent put into controller's graveyard)",
                match.gameData().id, match.permanent().getCard().getName());
        return true;
    }

    /**
     * Patron of the Nezumi. "That player" is the owner of the graveyard the permanent was put into,
     * so the entry's target is baked to the graveyard owner rather than the dying permanent's
     * controller — a permanent an opponent owns but you control still hits its owner.
     */
    @CollectsWatchTrigger(value = CardEffect.class,
            watch = ZoneChangeWatch.ON_PERMANENT_PUT_INTO_OPPONENT_GRAVEYARD_FROM_BATTLEFIELD)
    boolean handlePermanentIntoOpponentGraveyardDefault(TriggerMatchContext match,
            CardEffect effect, TriggerContext ctx) {
        TriggerContext.AnyPermanentGraveyard apg = (TriggerContext.AnyPermanentGraveyard) ctx;
        StackEntry entry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                match.permanent().getCard(),
                match.controllerId(),
                match.permanent().getCard().getName() + "'s ability",
                new ArrayList<>(List.of(effect)),
                apg.graveyardOwnerId(),
                match.permanent().getId()
        );
        // "Its toughness" (Grim Feast): the permanent as it last existed on the battlefield.
        markDyingCard(match.gameData(), entry, apg.dyingCard());
        entry.setTriggeringPermanent(apg.dyingPermanent());
        entry.setTriggeringPermanentToughnessAtTrigger(apg.dyingToughness());
        match.gameData().stack.add(entry);
        gameLogService.append(match.gameData(), GameLog.abilityTriggers(match.permanent().getCard()));
        log.info("Game {} - {} triggers (permanent {} put into an opponent's graveyard)",
                match.gameData().id, match.permanent().getCard().getName(), apg.dyingCard().getName());
        return true;
    }

    boolean handleAnyCreatureDeathReturnTriggeringPermanent(TriggerMatchContext match,
            ReturnTriggeringPermanentToBattlefieldEffect effect, TriggerContext ctx) {
        if (!(ctx instanceof TriggerContext.CreatureDeath death)
                || effect.fromZone() != Zone.GRAVEYARD
                || death.dyingCard() == null) {
            return false;
        }

        StackEntry entry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                match.permanent().getCard(),
                match.controllerId(),
                match.permanent().getCard().getName() + "'s ability",
                new ArrayList<>(List.of(effect)),
                null,
                match.permanent().getId()
        );
        markDyingCard(match.gameData(), entry, death.dyingCard());
        match.gameData().stack.add(entry);
        logAnyCreatureDeath(match);
        return true;
    }

    boolean handleAnyCreatureDeathBoostSelf(TriggerMatchContext match,
            BoostSelfEffect effect, TriggerContext ctx) {
        match.gameData().stack.add(new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                match.permanent().getCard(),
                match.controllerId(),
                match.permanent().getCard().getName() + "'s ability",
                new ArrayList<>(List.of(effect)),
                null,
                match.permanent().getId()
        ));
        logAnyCreatureDeath(match);
        return true;
    }

    boolean handleAnyCreatureDeathPutCounters(TriggerMatchContext match,
            PutCountersOnSourceEffect effect, TriggerContext ctx) {
        match.gameData().stack.add(new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                match.permanent().getCard(),
                match.controllerId(),
                match.permanent().getCard().getName() + "'s ability",
                new ArrayList<>(List.of(effect)),
                null,
                match.permanent().getId()
        ));
        logAnyCreatureDeath(match);
        return true;
    }

    boolean handleAnyCreatureDeathPutCountersOnSelf(TriggerMatchContext match,
            PutCountersOnSelfEffect effect, TriggerContext ctx) {
        TriggerContext.CreatureDeath death = (TriggerContext.CreatureDeath) ctx;
        StackEntry entry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                match.permanent().getCard(),
                match.controllerId(),
                match.permanent().getCard().getName() + "'s ability",
                new ArrayList<>(List.of(effect)),
                null,
                match.permanent().getId()
        );
        if (amountEvaluationService.referencesEventValue(effect.amount())) {
            entry.setEventValue(death.dyingCreaturePower());
        }
        match.gameData().stack.add(entry);
        logAnyCreatureDeath(match);
        return true;
    }

    boolean handleAnyCreatureDeathUntap(TriggerMatchContext match,
            UntapPermanentsEffect effect, TriggerContext ctx) {
        // "Whenever another creature dies, untap this creature." SELF-scope untap needs its source
        // permanent id to locate this creature at resolution (Galvanic Juggernaut).
        match.gameData().stack.add(new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                match.permanent().getCard(),
                match.controllerId(),
                match.permanent().getCard().getName() + "'s ability",
                new ArrayList<>(List.of(effect)),
                null,
                match.permanent().getId()
        ));
        logAnyCreatureDeath(match);
        return true;
    }

    boolean handleAnyCreatureDeathTap(TriggerMatchContext match,
            TapPermanentsEffect effect, TriggerContext ctx) {
        match.gameData().stack.add(new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                match.permanent().getCard(),
                match.controllerId(),
                match.permanent().getCard().getName() + "'s ability",
                new ArrayList<>(List.of(effect)),
                null,
                match.permanent().getId()
        ));
        logAnyCreatureDeath(match);
        return true;
    }

    boolean handleAnyCreatureDeathMay(TriggerMatchContext match,
            MayEffect may, TriggerContext ctx) {
        // CR 603.3d: a targeted "may" ability (e.g. Vicious Shadows' "you may have this deal damage
        // to target player") needs its target chosen when the trigger is put on the stack.
        Card dyingCard = ctx instanceof TriggerContext.CreatureDeath death ? death.dyingCard() : null;
        if (may.targetSpec().admits(TargetPredicate.Kind.PERMANENT) || may.targetSpec().admits(TargetPredicate.Kind.PLAYER)) {
            PermanentChoiceContext.DeathTriggerTarget target = new PermanentChoiceContext.DeathTriggerTarget(
                    match.permanent().getCard(), match.controllerId(), new ArrayList<>(List.of(may)),
                    null, match.permanent()
            );
            match.gameData().queueInteraction(dyingCard == null ? target : target.withTriggeringCard(
                    dyingCard.getId(), match.gameData().graveyardEntryVersion(dyingCard.getId())));
        } else {
            // Pass the source permanent id so wrapped source-referencing effects (e.g.
            // PutCountersOnSourceEffect for Scavenger Drake's optional +1/+1 counter) can resolve.
            int stackSizeBefore = match.gameData().stack.size();
            match.gameData().queueMayAbility(match.permanent().getCard(), match.controllerId(), may,
                    null, match.permanent().getId());
            // Every copy an effect that makes the ability trigger additional times added records the creature that
            // died, not just the last one (Drivnod, Carnage Dominus with Fecundity).
            for (int i = stackSizeBefore; i < match.gameData().stack.size(); i++) {
                StackEntry entry = match.gameData().stack.get(i);
                markDyingCard(match.gameData(), entry, dyingCard);
                if (ctx instanceof TriggerContext.CreatureDeath death) {
                    recordDyingCreature(entry, death);
                }
            }
        }
        logAnyCreatureDeath(match);
        return true;
    }

    @CollectsEmblemTrigger(EmblemCreatureDeathTriggerEffect.class)
    boolean handleEmblemCreatureDeath(EmblemTriggerMatchContext match,
            EmblemCreatureDeathTriggerEffect effect, TriggerContext ctx) {
        Card source = match.emblem().sourceCard();
        if (source == null) {
            return false;
        }

        GameData gameData = match.gameData();
        List<CardEffect> effects = new ArrayList<>(effect.effects());
        if (effects.stream().anyMatch(cardEffect ->
                cardEffect.targetSpec().admits(TargetPredicate.Kind.PERMANENT)
                        || cardEffect.targetSpec().admits(TargetPredicate.Kind.PLAYER))) {
            gameData.queueInteraction(new PermanentChoiceContext.DeathTriggerTarget(
                    source, match.controllerId(), effects, null, null, effect.targetFilter()));
        } else {
            gameData.stack.add(new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    source,
                    match.controllerId(),
                    source.getName() + "'s emblem ability",
                    effects));
        }
        return true;
    }

    @CollectsEmblemTrigger(EmblemArtifactGraveyardReturnTriggerEffect.class)
    boolean handleEmblemArtifactGraveyardReturn(EmblemTriggerMatchContext match,
            EmblemArtifactGraveyardReturnTriggerEffect effect, TriggerContext ctx) {
        TriggerContext.ArtifactGraveyard artifactGraveyard = (TriggerContext.ArtifactGraveyard) ctx;
        Card artifactCard = artifactGraveyard.artifactCard();
        if (artifactCard == null || artifactCard.isToken()) {
            return false;
        }

        Card source = match.emblem().sourceCard();
        String description = (source != null ? source.getName() : "Emblem") + "'s emblem";
        RegisterDelayedReturnDyingCreatureUnderControlEffect delayedReturn =
                new RegisterDelayedReturnDyingCreatureUnderControlEffect(
                        false, null, 0, null, null, false, true, false, false, false);
        StackEntry entry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                source != null ? source : artifactCard,
                match.controllerId(),
                description,
                new ArrayList<>(List.of(delayedReturn)));
        markDyingCard(match.gameData(), entry, artifactCard);
        match.gameData().stack.add(entry);
        gameLogService.append(match.gameData(), GameLog.text(description + " triggers (an artifact was put into a graveyard)."));
        log.info("Game {} - {} schedules a delayed return of {}", match.gameData().id, description, artifactCard.getName());
        return true;
    }

    boolean handleAnyCreatureDeathCreateTokenCopy(TriggerMatchContext match,
            CreateTokenCopyOfDyingCreatureEffect effect, TriggerContext ctx) {
        TriggerContext.CreatureDeath death = (TriggerContext.CreatureDeath) ctx;
        if (death.dyingCard() == null) {
            return false;
        }

        StackEntry entry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                match.permanent().getCard(),
                match.controllerId(),
                match.permanent().getCard().getName() + "'s ability",
                new ArrayList<>(List.of(effect)),
                null,
                match.permanent().getId()
        );
        entry.setSourcePermanentSnapshot(new Permanent(match.permanent()));
        markDyingCard(match.gameData(), entry, death.dyingCard());
        entry.setTriggeringPermanentId(death.dyingPermanentId());
        entry.rememberLastKnownPermanentCard(death.dyingPermanentId(), death.dyingCard());
        entry.setTriggeringPermanentPowerAtTrigger(death.dyingCreaturePower());
        entry.setEventValue(death.dyingCreaturePower());
        match.gameData().stack.add(entry);
        logAnyCreatureDeath(match);
        return true;
    }

    boolean handleAnyCreatureDeathDefault(TriggerMatchContext match,
            CardEffect effect, TriggerContext ctx) {
        GameData gameData = match.gameData();
        if (effect.targetSpec().admits(TargetPredicate.Kind.PERMANENT)
                || effect.targetSpec().admits(TargetPredicate.Kind.PLAYER)
                || effect.targetSpec().admits(TargetPredicate.Kind.GRAVEYARD_CARD)) {
            PermanentChoiceContext.DeathTriggerTarget target = new PermanentChoiceContext.DeathTriggerTarget(
                    match.permanent().getCard(), match.controllerId(), new ArrayList<>(List.of(effect)),
                    null, new Permanent(match.permanent())
            );
            if (ctx instanceof TriggerContext.CreatureDeath death && death.dyingCard() != null) {
                target = target.withTriggeringCard(death.dyingCard().getId(),
                        gameData.graveyardEntryVersion(death.dyingCard().getId()))
                        .withTriggeringPermanent(death.dyingPermanent(), death.attachments());
            }
            gameData.queueInteraction(target);
        } else {
            StackEntry entry = new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    match.permanent().getCard(),
                    match.controllerId(),
                    match.permanent().getCard().getName() + "'s ability",
                    new ArrayList<>(List.of(effect)),
                    null,
                    match.permanent().getId()
            );
            // The source as it triggered, whatever the effects are: "enchanted creature" of an Aura that died with
            // the creature (Yoke of the Damned, Sadistic Glee) is read from it once the Aura is gone (CR 608.2h).
            entry.setSourcePermanentSnapshot(new Permanent(match.permanent()));
            if (ctx instanceof TriggerContext.CreatureDeath death) {
                if (death.dyingCard() != null) {
                    markDyingCard(gameData, entry, death.dyingCard());
                    if (death.dyingPermanentId() != null) {
                        entry.rememberLastKnownPermanentCard(death.dyingPermanentId(), death.dyingCard());
                    }
                }
                entry.setTriggeringPermanentId(death.dyingPermanentId());
                // "Each Aura you controlled that was attached to it" (Hateful Eidolon): its attachments as it died.
                entry.setTriggeringPermanent(death.dyingPermanent(), death.attachments());
                entry.setTriggeringPermanentControllerId(death.dyingCreatureControllerId());
                entry.setTriggeringPermanentPowerAtTrigger(death.dyingCreaturePower());
                entry.setEventValue(death.dyingCreaturePower());
            }
            gameData.stack.add(entry);
        }
        logAnyCreatureDeath(match);
        return true;
    }

    boolean handleAnyCreatureDeathDelayedReturnUnderOwnersControl(TriggerMatchContext match,
            RegisterDelayedReturnDyingCreatureUnderControlEffect effect, TriggerContext ctx) {
        TriggerContext.CreatureDeath death = (TriggerContext.CreatureDeath) ctx;
        if (death.dyingCard() == null
                || (effect.requireAnotherCreature() && !hasAnotherCreatureAtDeath(match, death))) {
            return false;
        }

        StackEntry entry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                match.permanent().getCard(),
                match.controllerId(),
                match.permanent().getCard().getName() + "'s ability",
                new ArrayList<>(List.of(effect)),
                null,
                match.permanent().getId()
        );
        markDyingCard(match.gameData(), entry, death.dyingCard());
        match.gameData().stack.add(entry);
        logAnyCreatureDeath(match);
        return true;
    }

    private boolean hasAnotherCreatureAtDeath(TriggerMatchContext match, TriggerContext.CreatureDeath death) {
        GameData gameData = match.gameData();
        UUID sourcePermanentId = match.permanent().getId();
        UUID dyingPermanentId = death.dyingPermanentId();
        if (gameData.anyPermanentMatches(permanent ->
                !permanent.getId().equals(dyingPermanentId)
                        && gameQueryService.isCreature(gameData, permanent))) {
            return true;
        }
        return gameData.simultaneousDyingCreatures.entrySet().stream()
                .filter(entry -> !entry.getKey().equals(sourcePermanentId)
                        && !entry.getKey().equals(dyingPermanentId))
                .anyMatch(entry -> gameQueryService.isCreature(gameData, entry.getValue()));
    }

    boolean handleAnyCreatureDeathExileLinkedCreature(TriggerMatchContext match,
            ExileTriggeringCreatureAndReturnSourceToHandEffect effect, TriggerContext ctx) {
        TriggerContext.CreatureDeath death = (TriggerContext.CreatureDeath) ctx;
        UUID linkedPermanentId = match.permanent().getChosenPermanentId();
        if (linkedPermanentId == null || !linkedPermanentId.equals(death.dyingPermanentId())
                || death.dyingCard() == null) {
            return false;
        }

        // "That creature" is the entry's triggering card.
        StackEntry entry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                match.permanent().getCard(),
                match.controllerId(),
                match.permanent().getCard().getName() + "'s ability",
                new ArrayList<>(List.of(effect)),
                null,
                match.permanent().getId()
        );
        markDyingCard(match.gameData(), entry, death.dyingCard());
        match.gameData().stack.add(entry);
        logAnyCreatureDeath(match);
        return true;
    }

    private void logAnyCreatureDeath(TriggerMatchContext match) {
        gameLogService.append(match.gameData(), GameLog.abilityTriggers(match.permanent().getCard()));
        log.info("Game {} - {} triggers (any creature died)", match.gameData().id, match.permanent().getCard().getName());
    }

    boolean handleAllyNontokenCreateTokenWithTotalDyingCreaturesPower(
            TriggerMatchContext match, CreateTokenWithTotalDyingCreaturesPowerEffect effect,
            TriggerContext ctx) {
        TriggerContext.CreatureDeath death = (TriggerContext.CreatureDeath) ctx;
        if (!isFirstNontokenDeathInSimultaneousEvent(match.gameData(), death)) {
            return false;
        }

        int totalPower = totalPowerOfSimultaneousNontokenDeaths(match.gameData(), death);
        StackEntry entry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                match.permanent().getCard(),
                death.dyingCreatureControllerId(),
                match.permanent().getCard().getName() + "'s ability",
                new ArrayList<>(List.of(effect)),
                null,
                match.permanent().getId()
        );
        entry.setEventValue(totalPower);
        match.gameData().stack.add(entry);
        logAllyNontokenCreatureDeath(match);
        return true;
    }

    private boolean isFirstNontokenDeathInSimultaneousEvent(GameData gameData,
                                                              TriggerContext.CreatureDeath death) {
        if (gameData.simultaneousDyingCreatures.isEmpty()) {
            return true;
        }

        UUID firstPermanentId = null;
        for (Map.Entry<UUID, Permanent> entry : gameData.simultaneousDyingCreatures.entrySet()) {
            if (!death.dyingCreatureControllerId().equals(
                    gameData.simultaneousDyingControllers.get(entry.getKey()))
                    || entry.getValue().getCard().isToken()) {
                continue;
            }
            if (firstPermanentId == null || entry.getKey().compareTo(firstPermanentId) < 0) {
                firstPermanentId = entry.getKey();
            }
        }
        return death.dyingPermanentId() != null && death.dyingPermanentId().equals(firstPermanentId);
    }

    private int totalPowerOfSimultaneousNontokenDeaths(GameData gameData,
                                                        TriggerContext.CreatureDeath death) {
        if (gameData.simultaneousDyingCreatures.isEmpty()) {
            return death.dyingCreaturePower();
        }

        int totalPower = 0;
        for (Map.Entry<UUID, Permanent> entry : gameData.simultaneousDyingCreatures.entrySet()) {
            if (!death.dyingCreatureControllerId().equals(
                    gameData.simultaneousDyingControllers.get(entry.getKey()))
                    || entry.getValue().getCard().isToken()) {
                continue;
            }
            Integer power = gameData.simultaneousDyingPowers.get(entry.getKey());
            if (power == null) {
                power = entry.getKey().equals(death.dyingPermanentId())
                        ? death.dyingCreaturePower() : entry.getValue().getEffectivePower();
            }
            totalPower += power;
        }
        return totalPower;
    }

    boolean handleAllyNontokenMay(TriggerMatchContext match,
            MayEffect may, TriggerContext ctx) {
        TriggerContext.CreatureDeath cd = (TriggerContext.CreatureDeath) ctx;
        // "That card": the card that went to the graveyard, for a copy the physical card.
        UUID dyingCardId = cd.dyingCard() == null ? null : cd.dyingPermanent() == null ? cd.dyingCard().getId()
                : cd.dyingPermanent().getOriginalCard().getId();
        match.gameData().queueMayAbility(match.permanent().getCard(), cd.dyingCreatureControllerId(), may,
                dyingCardId);
        // "That creature" (its name, its counters) as it last existed on the battlefield (CR 603.10a).
        match.gameData().stack.getLast().setTriggeringPermanent(cd.dyingPermanent() != null ? cd.dyingPermanent()
                : cd.dyingCard() == null ? null : new Permanent(cd.dyingCard()));
        logAllyNontokenCreatureDeath(match);
        return true;
    }

    boolean handleAllyNontokenMayPay(TriggerMatchContext match,
            MayPayManaEffect mayPay, TriggerContext ctx) {
        TriggerContext.CreatureDeath cd = (TriggerContext.CreatureDeath) ctx;
        match.gameData().queueMayAbility(match.permanent().getCard(), cd.dyingCreatureControllerId(), mayPay,
                null, match.permanent().getId());
        markDyingCard(match.gameData(), match.gameData().stack.getLast(), cd.dyingCard());
        return true;
    }

    boolean handleAllyNontokenDefault(TriggerMatchContext match,
            CardEffect effect, TriggerContext ctx) {
        TriggerContext.CreatureDeath cd = (TriggerContext.CreatureDeath) ctx;
        if (effect.targetSpec().declaredTarget() != null) {
            PermanentChoiceContext.DeathTriggerTarget target = new PermanentChoiceContext.DeathTriggerTarget(
                    match.permanent().getCard(), match.controllerId(), new ArrayList<>(List.of(effect)));
            match.gameData().queueInteraction((cd.dyingCard() == null ? target : target.withTriggeringCard(
                    cd.dyingCard().getId(), match.gameData().graveyardEntryVersion(cd.dyingCard().getId())))
                    .withTriggeringPermanent(cd.dyingPermanent()));
            logAllyNontokenCreatureDeath(match);
            return true;
        }
        StackEntry entry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                match.permanent().getCard(),
                cd.dyingCreatureControllerId(),
                match.permanent().getCard().getName() + "'s ability",
                new ArrayList<>(List.of(effect)),
                null,
                match.permanent().getId()
        );
        Card dyingCard = cd.dyingPermanent() != null ? cd.dyingPermanent().getCard() : cd.dyingCard();
        if (dyingCard != null) {
            entry.setDyingPermanentManaValue(dyingCard.getManaValue());
        }
        // "That card" (Purgatory, Hofri, Ghostforge): the creature that died.
        markDyingCard(match.gameData(), entry, cd.dyingCard());
        entry.setTriggeringPermanent(cd.dyingPermanent());
        match.gameData().stack.add(entry);
        logAllyNontokenCreatureDeath(match);
        return true;
    }

    boolean handleAllyNontokenInterveningIfConditional(TriggerMatchContext match,
            ConditionalEffect conditional, TriggerContext ctx) {
        return handleAllyNontokenDefault(match, conditional, ctx);
    }

    private void logAllyNontokenCreatureDeath(TriggerMatchContext match) {
        gameLogService.append(match.gameData(), GameLog.abilityTriggers(match.permanent().getCard()));
        log.info("Game {} - {} triggers (ally nontoken creature died)", match.gameData().id, match.permanent().getCard().getName());
    }

    boolean handleAnyNontokenCreatureDeathDefault(TriggerMatchContext match,
            CardEffect effect, TriggerContext ctx) {
        StackEntry entry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                match.permanent().getCard(),
                match.controllerId(),
                match.permanent().getCard().getName() + "'s ability",
                new ArrayList<>(List.of(effect)),
                null,
                match.permanent().getId()
        );
        if (ctx instanceof TriggerContext.CreatureDeath death) {
            markDyingCard(match.gameData(), entry, death.dyingCard());
            entry.setTriggeringPermanent(death.dyingPermanent());
        }
        match.gameData().stack.add(entry);
        gameLogService.append(match.gameData(), GameLog.abilityTriggers(match.permanent().getCard()));
        log.info("Game {} - {} triggers (any nontoken creature died)", match.gameData().id, match.permanent().getCard().getName());
        return true;
    }

    boolean handleEnchantedPlayerNontokenCreatureDeathDefault(TriggerMatchContext match,
            CardEffect effect, TriggerContext ctx) {
        TriggerContext.CreatureDeath death = (TriggerContext.CreatureDeath) ctx;
        StackEntry entry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                match.permanent().getCard(),
                match.controllerId(),
                match.permanent().getCard().getName() + "'s ability",
                new ArrayList<>(List.of(effect)),
                null,
                match.permanent().getId());
        if (death.dyingCard() != null) {
            entry.setDyingPermanentManaValue(death.dyingCard().getManaValue());
        }
        markDyingCard(match.gameData(), entry, death.dyingCard());
        entry.setTriggeringPermanent(death.dyingPermanent());
        match.gameData().stack.add(entry);
        log.info("Game {} - {} triggers (enchanted player's nontoken creature died)",
                match.gameData().id, match.permanent().getCard().getName());
        return true;
    }

    boolean handleOpponentCreatureDeathMay(TriggerMatchContext match,
            MayEffect may, TriggerContext ctx) {
        // "You may return target artifact card from your graveyard" (Glissa, the Traitor): the target
        // is chosen as the trigger goes on the stack; the "may" is decided as it resolves.
        if (graveyardTargetingSupport.findTarget(List.of(may)) != null) {
            match.gameData().queueInteraction(new PermanentChoiceContext.DeathTriggerTarget(
                    match.permanent().getCard(), match.controllerId(), new ArrayList<>(List.of(may)),
                    null, new Permanent(match.permanent()), null, null, true));
            logOpponentCreatureDeath(match);
            return true;
        }
        TriggerContext.CreatureDeath cd = (TriggerContext.CreatureDeath) ctx;
        match.gameData().queueMayAbility(match.permanent().getCard(), match.controllerId(), may, null,
                match.permanent().getId(), match.markSourceOncePerTurnOnAcceptance());
        StackEntry entry = match.gameData().stack.getLast();
        markDyingCard(match.gameData(), entry, cd.dyingCard());
        entry.setTriggeringPermanent(cd.dyingPermanent());
        logOpponentCreatureDeath(match);
        return true;
    }

    boolean handleOpponentCreatureDelayedReturnUnderControl(TriggerMatchContext match,
            RegisterDelayedReturnDyingCreatureUnderControlEffect effect, TriggerContext ctx) {
        // Grave Betrayal: the delayed return is registered when this trigger resolves, and the
        // handler reads the dying card id from the entry — the slot's default collector does not
        // stamp it, so bake it in here.
        TriggerContext.CreatureDeath cd = (TriggerContext.CreatureDeath) ctx;
        if (cd.dyingCard() == null) {
            return false;
        }
        StackEntry entry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                match.permanent().getCard(),
                match.controllerId(),
                match.permanent().getCard().getName() + "'s ability",
                new ArrayList<>(List.of(effect)),
                null,
                match.permanent().getId()
        );
        markDyingCard(match.gameData(), entry, cd.dyingCard());
        match.gameData().stack.add(entry);
        logOpponentCreatureDeath(match);
        return true;
    }

    boolean handleOpponentCreatureDeathDefault(TriggerMatchContext match,
            CardEffect effect, TriggerContext ctx) {
        return queueOpponentCreatureDeath(match, effect, effect, ctx);
    }

    private boolean queueOpponentCreatureDeath(TriggerMatchContext match, CardEffect effect,
                                               CardEffect declaredTargetEffect, TriggerContext ctx) {
        TriggerContext.CreatureDeath cd = (TriggerContext.CreatureDeath) ctx;
        // CR 603.3d: a graveyard-targeting trigger (Toshiro Umezawa's "you may cast target instant card
        // from your graveyard") picks its target as it goes on the stack, and is skipped without one.
        if (effect.targetSpec().admits(TargetPredicate.Kind.GRAVEYARD_CARD)) {
            match.gameData().queueInteraction(new PermanentChoiceContext.DeathTriggerTarget(
                    match.permanent().getCard(), match.controllerId(), new ArrayList<>(List.of(effect))
            ));
            logOpponentCreatureDeath(match);
            return true;
        }
        if (effect.targetSpec().admits(TargetPredicate.Kind.PERMANENT)
                || effect.targetSpec().admits(TargetPredicate.Kind.PLAYER)) {
            TargetFilter targetFilter = targetFilterForDeathTrigger(
                    match.permanent().getCard(), declaredTargetEffect);
            PermanentChoiceContext.DeathTriggerTarget target = new PermanentChoiceContext.DeathTriggerTarget(
                    match.permanent().getCard(), match.controllerId(), new ArrayList<>(List.of(effect)),
                    null, null, targetFilter);
            match.gameData().queueInteraction(cd.dyingCard() == null ? target : target.withTriggeringCard(
                    cd.dyingCard().getId(), match.gameData().graveyardEntryVersion(cd.dyingCard().getId())));
            logOpponentCreatureDeath(match);
            return true;
        }
        StackEntry entry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                match.permanent().getCard(),
                match.controllerId(),
                match.permanent().getCard().getName() + "'s ability",
                new ArrayList<>(List.of(effect)),
                cd.dyingCreatureControllerId(),
                match.permanent().getId()
        );
        // "Exile it" (Mari, the Killing Quill; Patron of the Vein): the creature that died.
        markDyingCard(match.gameData(), entry, cd.dyingCard());
        recordDyingCreature(entry, cd);
        match.gameData().stack.add(entry);
        logOpponentCreatureDeath(match);
        return true;
    }

    /**
     * Records the creature that died as {@code entry}'s triggering permanent, as it last existed on the battlefield
     * (CR 603.10a): its snapshot, controller and power, which "that creature's power" ({@code TriggeringPermanentPower})
     * and "its controller" read as the ability resolves.
     */
    private static void recordDyingCreature(StackEntry entry, TriggerContext.CreatureDeath death) {
        entry.setTriggeringPermanentId(death.dyingPermanentId());
        entry.setTriggeringPermanent(death.dyingPermanent(), death.attachments());
        entry.setTriggeringPermanentControllerId(death.dyingCreatureControllerId());
        entry.setTriggeringPermanentPowerAtTrigger(death.dyingCreaturePower());
    }

    private TargetFilter targetFilterForDeathTrigger(Card sourceCard, CardEffect effect) {
        int targetIndex = sourceCard.getEffectTargetIndex(effect);
        if (targetIndex < 0 || targetIndex >= sourceCard.getTargetGroups().size()) {
            return null;
        }
        return sourceCard.getTargetGroups().get(targetIndex).getFilter();
    }

    private void logOpponentCreatureDeath(TriggerMatchContext match) {
        gameLogService.append(match.gameData(), GameLog.abilityTriggers(match.permanent().getCard()));
        log.info("Game {} - {} triggers (opponent creature died)", match.gameData().id, match.permanent().getCard().getName());
    }

    // ── ON_ALLY_AURA_OR_EQUIPMENT_PUT_INTO_GRAVEYARD_FROM_BATTLEFIELD ──

    /**
     * "Whenever an Aura or Equipment you control is put into a graveyard from the battlefield": "that card" is the
     * entry's triggering card. An ability that watches only Auras (Mazzy, Truesword Paladin) says so in its trigger
     * filter, which the pipeline judges on the departed permanent.
     */
    @CollectsWatchTrigger(value = CardEffect.class,
            watch = ZoneChangeWatch.ON_ALLY_AURA_OR_EQUIPMENT_PUT_INTO_GRAVEYARD_FROM_BATTLEFIELD)
    boolean handleAllyAuraOrEquipmentGraveyardDefault(TriggerMatchContext match, CardEffect effect,
            TriggerContext ctx) {
        TriggerContext.AllyAuraOrEquipmentGraveyard aaeg = (TriggerContext.AllyAuraOrEquipmentGraveyard) ctx;
        Card dyingCard = aaeg.dyingCard();

        StackEntry entry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                match.permanent().getCard(),
                match.controllerId(),
                match.permanent().getCard().getName() + "'s ability",
                new ArrayList<>(List.of(effect)),
                null,
                match.permanent().getId());
        markDyingCard(match.gameData(), entry, dyingCard);
        entry.setNonTargeting(true);
        match.gameData().stack.add(entry);

        gameLogService.append(match.gameData(), GameLog.cardTextCard(match.permanent().getCard(),
                "'s ability triggers (", dyingCard, " was put into a graveyard from the battlefield)."));
        log.info("Game {} - {} triggers (Aura or Equipment {} put into a graveyard from the battlefield)",
                match.gameData().id, match.permanent().getCard().getName(), dyingCard.getName());
        return true;
    }
    boolean handleAnyCreatureDeathPutCountersOnSourceCard(TriggerMatchContext match,
            PutCountersOnSourceCardEffect effect, TriggerContext ctx) {
        match.gameData().stack.add(new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                match.permanent().getCard(),
                match.controllerId(),
                match.permanent().getCard().getName() + "'s ability",
                new ArrayList<>(List.of(effect)),
                null,
                match.permanent().getId()
        ));
        logAnyCreatureDeath(match);
        return true;
    }

    /**
     * Gives {@code entry} the card that died ("that card", "it") as its triggering card, with the graveyard
     * incarnation it has now (CR 400.7), for the effects that read it as they resolve; nothing for {@code null}.
     */
    private static void markDyingCard(GameData gameData, StackEntry entry, Card dyingCard) {
        if (dyingCard != null) {
            entry.setTriggeringCard(dyingCard.getId(), gameData.graveyardEntryVersion(dyingCard.getId()));
        }
    }
}

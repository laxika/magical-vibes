package com.github.laxika.magicalvibes.service.trigger;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.TargetSpec;
import com.github.laxika.magicalvibes.model.effect.DamageDealingEffect;
import com.github.laxika.magicalvibes.model.filter.AnyTargetPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentTruePredicate;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilter;
import com.github.laxika.magicalvibes.model.effect.KayaSpiritsJusticeEffect;
import com.github.laxika.magicalvibes.model.filter.ControlledPermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTokenPredicate;
import com.github.laxika.magicalvibes.model.effect.TargetPredicate;
import com.github.laxika.magicalvibes.model.effect.TargetPredicates;
import com.github.laxika.magicalvibes.service.effect.ConditionEvaluationService;
import com.github.laxika.magicalvibes.service.DrawService;
import com.github.laxika.magicalvibes.service.battlefield.ETBTokenTargetService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.graveyard.GraveyardService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Trigger collectors for sacrifice, graveyard and other zone-change watches' events, and the shared
 * record of the events they collect.
 */
@Slf4j
@Service
public class MiscTriggerCollectorService {

    private final GameLogService gameLogService;
    private final GraveyardService graveyardService;
    private final GameQueryService gameQueryService;
    private final DrawService drawService;
    private final ConditionEvaluationService conditionEvaluationService;
    private final ETBTokenTargetService etbTokenTargetService;
    private PermanentRemovalService permanentRemovalService;
    private final DeathTriggerCollectorService deathTriggerCollectorService;

    public MiscTriggerCollectorService(GameLogService gameLogService,
                                       @Lazy GraveyardService graveyardService,
                                       GameQueryService gameQueryService,
                                       @Lazy DrawService drawService,
                                       ConditionEvaluationService conditionEvaluationService,
                                       @Lazy ETBTokenTargetService etbTokenTargetService,
                                       @Lazy PermanentRemovalService permanentRemovalService,
                                       @Lazy DeathTriggerCollectorService deathTriggerCollectorService) {
        this.gameLogService = gameLogService;
        this.graveyardService = graveyardService;
        this.gameQueryService = gameQueryService;
        this.drawService = drawService;
        this.conditionEvaluationService = conditionEvaluationService;
        this.etbTokenTargetService = etbTokenTargetService;
        this.permanentRemovalService = permanentRemovalService;
        this.deathTriggerCollectorService = deathTriggerCollectorService;
    }

    /**
     * "Whenever a player sacrifices a creature": the sacrifice recorded whatever the effects are
     * ({@link DeathTriggerCollectorService#queueDepartureAbility}), the sacrificing player its non-targeting "that
     * player".
     */
    @CollectsWatchTrigger(value = CardEffect.class, watch = ZoneChangeWatch.ON_ANY_CREATURE_SACRIFICED)
    private boolean handleAnyCreatureSacrificedDefault(TriggerMatchContext match, TriggerContext ctx) {
        queueSacrificeAbility(match, (TriggerContext.AllySacrificed) ctx);
        gameLogService.append(match.gameData(), GameLog.abilityTriggers(match.permanent().getCard()));
        return true;
    }

    /**
     * "Whenever you sacrifice a token", from exile (Albiorix, Goose Tyrant, CR 113.6k): the sacrifice recorded whatever
     * the effects are ({@link DeathTriggerCollectorService#queueDepartureAbility}); the source is the exiled card.
     */
    @CollectsWatchTrigger(value = CardEffect.class, watch = ZoneChangeWatch.EXILE_ON_CONTROLLER_TOKEN_SACRIFICED)
    private boolean handleExileTokenSacrificedDefault(TriggerMatchContext match, TriggerContext ctx) {
        if (match.sourceCard() == null) {
            return false;
        }
        queueSacrificeAbility(match, (TriggerContext.AllySacrificed) ctx);
        gameLogService.append(match.gameData(), GameLog.abilityTriggers(match.sourceCard()));
        return true;
    }

    /**
     * The sacrifice {@code sacrificed} as {@link DeathTriggerCollectorService#queueDepartureAbility} records a
     * permanent's leaving the battlefield: "that card", the permanent as it last existed, the sacrificing player as its
     * controller and as the entry's non-targeting "that player".
     */
    private void queueSacrificeAbility(TriggerMatchContext match, TriggerContext.AllySacrificed sacrificed) {
        deathTriggerCollectorService.queueDepartureAbility(match, sacrificed.sacrificedCard(),
                sacrificed.sacrificingPlayerId(), sacrificed.sacrificedPermanent(), sacrificed.sacrificingPlayerId());
    }

    @CollectsWatchTrigger(value = CardEffect.class, watch = ZoneChangeWatch.ON_ANY_PERMANENT_SACRIFICED)
    private boolean handleAnyPermanentSacrificedDefault(TriggerMatchContext match, TriggerContext ctx) {
        TriggerContext.PermanentSacrificed sacrificed = (TriggerContext.PermanentSacrificed) ctx;
        StackEntry entry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                match.permanent().getCard(),
                match.controllerId(),
                match.permanent().getCard().getName() + "'s ability",
                match.stackEffects(),
                null,
                match.permanent().getId()
        );
        match.gameData().handOffTriggerRecord(entry);
        if (match.admits(TargetPredicate.Kind.PERMANENT)
                || match.admits(TargetPredicate.Kind.PLAYER)) {
            match.gameData().queueInteraction(new PermanentChoiceContext.EntersTriggerTarget(
                    match.permanent().getCard(), match.controllerId(),
                    match.stackEffects(), match.permanent().getId()));
            gameLogService.append(match.gameData(), GameLog.abilityTriggers(match.permanent().getCard()));
            return true;
        }
        match.gameData().enqueueTrigger(entry);
        return true;
    }

    /**
     * "Whenever you sacrifice a [filter] permanent": the trigger pipeline has matched the permanent. The sacrificed
     * permanent, as it last existed on the battlefield (CR 603.10a), is the stack entry's triggering permanent and its
     * card the triggering card; a "you may" goes on the stack and is chosen as it resolves (CR 603.5), and "do this
     * only once each turn" is used up by an accepted graveyard return.
     */
    @CollectsWatchTrigger(value = CardEffect.class, watch = ZoneChangeWatch.ON_ALLY_PERMANENT_SACRIFICED)
    private boolean handleSacrificeDefault(TriggerMatchContext match, TriggerContext ctx) {
        TriggerContext.AllySacrificed as = (TriggerContext.AllySacrificed) ctx;
        String cardName = match.permanent().getCard().getName();
        Permanent sacrificed = as.sacrificedPermanent() != null ? as.sacrificedPermanent()
                : as.sacrificedCard() != null ? new Permanent(as.sacrificedCard()) : null;
        StackEntry triggerEntry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                match.permanent().getCard(),
                as.sacrificingPlayerId(),
                cardName + "'s ability",
                match.stackEffects(),
                null,
                match.permanent().getId()
        );
        triggerEntry.setSourcePermanentSnapshot(new Permanent(match.permanent()));
        triggerEntry.setOnceEachTurnActionKey(match.onceEachTurnActionKey());
        triggerEntry.setTriggeringPermanent(sacrificed);
        if (as.sacrificedCard() != null) {
            // CR 400.7: the trigger refers to this stay in the graveyard; if the card leaves and returns before
            // the trigger resolves, it is a new object the trigger no longer finds.
            triggerEntry.setTriggeringCard(as.sacrificedCard().getId(),
                    match.gameData().graveyardEntryVersion(as.sacrificedCard().getId()));
        }
        match.gameData().handOffTriggerRecord(triggerEntry);
        if (match.admits(TargetPredicate.Kind.GRAVEYARD_CARD)) {
            // The card is targeted as the trigger is put on the stack (CR 603.3d), so the may choice
            // comes at resolution. "Do this only once each turn" is used up only by an accepted return.
            // A target restricted by the sacrificed permanent ("target creature card with lesser mana value",
            // Riveteers Ascendancy) reads it from the record: the entry's triggering permanent, as it last existed
            // (CR 603.10a).
            String markOnAcceptance = match.onceEachTurnActionKey();
            match.gameData().queueInteraction(new PermanentChoiceContext.SpellGraveyardTargetTrigger(
                    match.permanent().getCard(), match.controllerId(), match.stackEffects(),
                    null, 0, 0, 0, null, false, null, match.permanent().getId(), markOnAcceptance));
            gameLogService.append(match.gameData(), GameLog.abilityTriggers(match.permanent().getCard()));
            return true;
        }
        if (match.admits(TargetPredicate.Kind.PERMANENT)
                || match.admits(TargetPredicate.Kind.PLAYER)) {
            match.gameData().queueInteraction(new PermanentChoiceContext.EntersTriggerTarget(
                    match.permanent().getCard(),
                    match.controllerId(),
                    match.stackEffects(),
                    match.permanent().getId()).withTriggeringPermanent(sacrificed));
            gameLogService.append(match.gameData(), GameLog.abilityTriggers(match.permanent().getCard()));
            log.info("Game {} - {} triggers on matching permanent sacrifice (awaiting target)",
                    match.gameData().id, cardName);
            return true;
        }
        match.gameData().enqueueTrigger(triggerEntry);
        gameLogService.append(match.gameData(), GameLog.abilityTriggers(match.permanent().getCard()));
        log.info("Game {} - {} triggers on matching permanent sacrifice", match.gameData().id, cardName);
        return true;
    }

    @CollectsWatchTrigger(value = CardEffect.class, watch = ZoneChangeWatch.ON_OPPONENT_PERMANENT_SACRIFICED)
    private boolean handleOpponentPermanentSacrificed(TriggerMatchContext match, TriggerContext ctx) {
        TriggerContext.OpponentPermanentSacrificed sacrificed =
                (TriggerContext.OpponentPermanentSacrificed) ctx;
        StackEntry entry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                match.permanent().getCard(),
                match.controllerId(),
                match.permanent().getCard().getName() + "'s ability",
                match.stackEffects(),
                sacrificed.sacrificingPlayerId(),
                match.permanent().getId());
        entry.setNonTargeting(true);
        match.gameData().enqueueTrigger(entry);
        gameLogService.append(match.gameData(), GameLog.abilityTriggers(match.permanent().getCard()));
        return true;
    }

    /**
     * "Whenever an opponent sacrifices a nontoken permanent" (Tergrid, It That Betrays, the Nazgûl Battle-Mace's grant):
     * the sacrifice recorded whatever the effects are ({@link #queueSacrificeAbility}); "that card" may have been exiled
     * instead (Leyline of the Void), its exile incarnation recorded too.
     */
    @CollectsWatchTrigger(value = CardEffect.class, watch = ZoneChangeWatch.ON_OPPONENT_NONTOKEN_PERMANENT_SACRIFICED)
    private boolean handleOpponentNontokenPermanentSacrificed(TriggerMatchContext match, TriggerContext ctx) {
        queueSacrificeAbility(match, (TriggerContext.AllySacrificed) ctx);
        gameLogService.append(match.gameData(), GameLog.abilityTriggers(match.permanent().getCard()));
        return true;
    }

    @CollectsWatchTrigger(value = CardEffect.class, watch = ZoneChangeWatch.ON_ALLY_LAND_PUT_INTO_GRAVEYARD_FROM_ANYWHERE)
    private boolean handleLandPutIntoGraveyardDefault(TriggerMatchContext match, TriggerContext ctx) {
        var gameData = match.gameData();
        String cardName = match.permanent().getCard().getName();

        gameData.enqueueTrigger(new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                match.permanent().getCard(),
                match.controllerId(),
                cardName + "'s ability",
                match.stackEffects(),
                null,
                match.permanent().getId()
        ));

        gameLogService.append(gameData, GameLog.abilityTriggers(match.permanent().getCard()));
        log.info("Game {} - {} triggers (land put into graveyard)", gameData.id, cardName);
        return true;
    }

    @CollectsWatchTrigger(value = CardEffect.class, watch = ZoneChangeWatch.ON_ALLY_LAND_CARD_MILLED)
    private boolean handleLandCardMilledDefault(TriggerMatchContext match, TriggerContext ctx) {
        var gameData = match.gameData();
        String cardName = match.permanent().getCard().getName();

        gameData.enqueueTrigger(new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                match.permanent().getCard(),
                match.controllerId(),
                cardName + "'s ability",
                match.stackEffects(),
                null,
                match.permanent().getId()
        ));

        gameLogService.append(gameData, GameLog.abilityTriggers(match.permanent().getCard()));
        log.info("Game {} - {} triggers on a land card being milled", gameData.id, cardName);
        return true;
    }

    @CollectsWatchTrigger(value = CardEffect.class,
                    watch = ZoneChangeWatch.ON_ALLY_CREATURE_CARDS_PUT_INTO_GRAVEYARD_FROM_LIBRARY)
    @CollectsWatchTrigger(value = CardEffect.class,
                    watch = ZoneChangeWatch.ON_ANY_CREATURE_CARD_PUT_INTO_GRAVEYARD_FROM_LIBRARY)
    private boolean handleCreatureCardsPutIntoGraveyardFromLibraryDefault(TriggerMatchContext match,
            TriggerContext ctx) {
        var gameData = match.gameData();
        String cardName = match.permanent().getCard().getName();

        StackEntry entry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                match.permanent().getCard(),
                match.controllerId(),
                cardName + "'s ability",
                match.stackEffects(),
                null,
                match.permanent().getId()
        );
        if (ctx instanceof TriggerContext.CreatureCardsPutIntoGraveyardFromLibrary batch) {
            entry.setEventValue(batch.creatureCardCount());
            recordCardsPutIntoGraveyard(gameData, entry, batch.cards());
        } else if (ctx instanceof TriggerContext.CreatureCardPutIntoGraveyardFromLibrary cardPut) {
            recordCardsPutIntoGraveyard(gameData, entry, List.of(cardPut.creatureCard()));
        }
        gameData.enqueueTrigger(entry);

        gameLogService.append(gameData, GameLog.abilityTriggers(match.permanent().getCard()));
        log.info("Game {} - {} triggers on creature cards put into graveyard from library",
                gameData.id, cardName);
        return true;
    }

    @CollectsWatchTrigger(value = CardEffect.class, watch = ZoneChangeWatch.ON_ANY_CARDS_PUT_INTO_LIBRARY)
    private boolean handleCardsPutIntoLibraryDefault(TriggerMatchContext match, TriggerContext ctx) {
        var gameData = match.gameData();
        var source = match.permanent();

        gameData.enqueueTrigger(new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                source.getCard(),
                match.controllerId(),
                source.getCard().getName() + "'s ability",
                match.stackEffects(),
                null,
                source.getId()));

        gameLogService.append(gameData, GameLog.abilityTriggers(source.getCard()));
        return true;
    }

    /**
     * "Whenever a card is put into your graveyard from anywhere" (and from your library, from your hand or library,
     * or from anywhere but the battlefield, of a filter): the cards recorded whatever the effects are
     * ({@link #queueGraveyardCardsAbility}).
     */
    @CollectsWatchTrigger(value = CardEffect.class,
                    watch = ZoneChangeWatch.ON_ALLY_CARD_PUT_INTO_GRAVEYARD_FROM_ANYWHERE)
    @CollectsWatchTrigger(value = CardEffect.class,
                    watch = ZoneChangeWatch.ON_ALLY_ARTIFACT_CARD_PUT_INTO_GRAVEYARD_FROM_NONBATTLEFIELD)
    @CollectsWatchTrigger(value = CardEffect.class,
                    watch = ZoneChangeWatch.ON_ALLY_PERMANENT_CARD_PUT_INTO_GRAVEYARD_FROM_ANYWHERE)
    @CollectsWatchTrigger(value = CardEffect.class,
                    watch = ZoneChangeWatch.ON_ALLY_PERMANENT_CARD_PUT_INTO_GRAVEYARD_FROM_LIBRARY)
    @CollectsWatchTrigger(value = CardEffect.class,
                    watch = ZoneChangeWatch.ON_ALLY_CARDS_PUT_INTO_GRAVEYARD_FROM_LIBRARY)
    private boolean handleCardPutIntoGraveyardDefault(TriggerMatchContext match, TriggerContext ctx) {
        List<Card> cards = switch (ctx) {
            case TriggerContext.CardsPutIntoGraveyardFromLibrary cardsPut -> cardsPut.cards();
            case TriggerContext.CardPutIntoGraveyard cardPut -> List.of(cardPut.card());
            case TriggerContext.PermanentCardPutIntoGraveyard permanentPut -> List.of(permanentPut.permanentCard());
            default -> List.of();
        };
        return queueGraveyardCardsAbility(match, cards, null, null, null,
                "card put into controller's graveyard");
    }

    /** "Whenever a creature card is put into your graveyard from anywhere": {@link #queueGraveyardCardsAbility}. */
    @CollectsWatchTrigger(value = CardEffect.class,
                    watch = ZoneChangeWatch.ON_ALLY_CREATURE_CARD_PUT_INTO_GRAVEYARD_FROM_ANYWHERE)
    @CollectsWatchTrigger(value = CardEffect.class,
                    watch = ZoneChangeWatch.ON_ANY_CREATURE_CARD_PUT_INTO_GRAVEYARD_FROM_NONBATTLEFIELD)
    private boolean handleCreatureCardPutIntoGraveyardDefault(TriggerMatchContext match, TriggerContext ctx) {
        List<Card> cards = ctx instanceof TriggerContext.CreatureCardPutIntoGraveyard creatureCard
                && creatureCard.creatureCard() != null ? List.of(creatureCard.creatureCard()) : List.of();
        return queueGraveyardCardsAbility(match, cards, null, null, null,
                "creature card put into graveyard from anywhere");
    }

    /**
     * "Whenever a nonblack card is put into a player's graveyard from anywhere, that player …" (The Dark Barony) and
     * "whenever a black card is put into an opponent's graveyard from anywhere" (Compost): the card recorded whatever
     * the effects are ({@link #queueGraveyardCardsAbility}), the graveyard's owner the non-targeting "that player".
     */
    @CollectsWatchTrigger(value = CardEffect.class,
            watch = ZoneChangeWatch.ON_NONBLACK_CARD_PUT_INTO_GRAVEYARD_FROM_ANYWHERE)
    @CollectsWatchTrigger(value = CardEffect.class,
            watch = ZoneChangeWatch.ON_BLACK_CARD_PUT_INTO_OPPONENT_GRAVEYARD_FROM_ANYWHERE)
    private boolean handleCardPutIntoPlayersGraveyardDefault(TriggerMatchContext match, TriggerContext ctx) {
        TriggerContext.CardPutIntoGraveyard cardPut = (TriggerContext.CardPutIntoGraveyard) ctx;
        return queueGraveyardCardsAbility(match, List.of(cardPut.card()), cardPut.graveyardOwnerId(),
                cardPut.graveyardOwnerId(), null, "card put into a player's graveyard from anywhere");
    }

    /**
     * "Whenever a creature card is put into an opponent's graveyard from their library" (Undead Alchemist): the card
     * recorded whatever the effects are ({@link #queueGraveyardCardsAbility}), its owner the non-targeting "that
     * player".
     */
    @CollectsWatchTrigger(value = CardEffect.class, watch = ZoneChangeWatch.ON_OPPONENT_CREATURE_CARD_MILLED)
    private boolean handleOpponentCreatureCardMilledDefault(TriggerMatchContext match, TriggerContext ctx) {
        TriggerContext.CardPutIntoGraveyard cardPut = (TriggerContext.CardPutIntoGraveyard) ctx;
        return queueGraveyardCardsAbility(match, List.of(cardPut.card()), cardPut.graveyardOwnerId(),
                cardPut.graveyardOwnerId(), null, "creature card milled");
    }

    /**
     * "Whenever an opponent mills one or more cards" (Lo and Li, Royal Advisors): the mill recorded whatever the
     * effects are ({@link #queueGraveyardCardsAbility}), the number of cards milled the event value and the milling
     * player the non-targeting "that player".
     */
    @CollectsWatchTrigger(value = CardEffect.class, watch = ZoneChangeWatch.ON_OPPONENT_MILLS)
    private boolean handleOpponentMillsDefault(TriggerMatchContext match, TriggerContext ctx) {
        TriggerContext.Mill mill = (TriggerContext.Mill) ctx;
        return queueGraveyardCardsAbility(match, List.of(), mill.milledPlayerId(), mill.milledPlayerId(),
                mill.milledCount(), "opponent milled");
    }

    /**
     * Puts {@code effect}, an effect of an ability that cards put into a graveyard triggered, on the stack whatever the
     * effect is: the entry records {@code cards} ({@link #recordCardsPutIntoGraveyard}: "that card", "them", each with
     * the graveyard stay it went to, CR 400.7), {@code triggeringPlayerId} as its triggering player and
     * {@code thatPlayerId} as its non-targeting "that player", {@code eventValue} ("that many") when not {@code null},
     * the source as it triggered (a permanent's snapshot, a plane's planar object) and the "do this only once each
     * turn" mark (CR 603.2h). An ability that targets waits for its targets as it goes on the stack (CR 603.3d).
     * Returns whether it triggered: an ability of no source doesn't.
     */
    private boolean queueGraveyardCardsAbility(TriggerMatchContext match, List<Card> cards,
                                               UUID triggeringPlayerId, UUID thatPlayerId, Integer eventValue,
                                               String event) {
        GameData gameData = match.gameData();
        Permanent source = match.permanent();
        Card sourceCard = source != null ? source.getCard() : match.sourceCard();
        if (sourceCard == null) {
            return false;
        }
        StackEntry entry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                sourceCard,
                match.controllerId(),
                sourceCard.getName() + "'s ability",
                match.stackEffects(),
                thatPlayerId,
                source == null ? null : source.getId());
        entry.setNonTargeting(true);
        recordCardsPutIntoGraveyard(gameData, entry, cards);
        entry.setTriggeringPlayerId(triggeringPlayerId);
        if (eventValue != null) {
            entry.setEventValue(eventValue);
        }
        // The source as the ability goes on the stack, whatever the effect is (CR 608.2h).
        if (source != null) {
            entry.setSourcePermanentSnapshot(new Permanent(source));
        }
        if (match.sourcePlanarObject() != null) {
            entry.setSourcePlanarObject(match.sourcePlanarObject().copy());
        }
        entry.setOnceEachTurnActionKey(match.onceEachTurnActionKey());
        gameData.handOffTriggerRecord(entry);
        if (source != null && (match.admits(TargetPredicate.Kind.PERMANENT)
                || match.admits(TargetPredicate.Kind.PLAYER)
                || match.admits(TargetPredicate.Kind.GRAVEYARD_CARD))) {
            gameData.queueInteraction(new PermanentChoiceContext.SelfTriggeredAbilityTarget(
                    sourceCard, match.controllerId(), match.stackEffects(), event, source.getId(),
                    new Permanent(source), eventValue));
        } else {
            gameData.enqueueTrigger(entry);
        }
        gameLogService.append(gameData, GameLog.abilityTriggers(sourceCard));
        log.info("Game {} - {} triggers ({})", gameData.id, sourceCard.getName(), event);
        return true;
    }

    /**
     * "Whenever a card is put into an opponent's graveyard from anywhere", from a permanent or, while suspended, from
     * exile (Nihilith): the pipeline judged the ability's options as it triggered and wrapped its intervening "if"
     * around the effect, which checks it again as it resolves (CR 603.4). On the battlefield the graveyard's owner is
     * "that player" (the entry's target), and "that card" is the graveyard object the card became (CR 400.7).
     */
    @CollectsWatchTrigger(value = CardEffect.class,
            watch = ZoneChangeWatch.ON_CARD_PUT_INTO_OPPONENT_GRAVEYARD_FROM_ANYWHERE)
    private boolean handleCardPutIntoOpponentGraveyardDefault(TriggerMatchContext match, TriggerContext ctx) {
        TriggerContext.CardPutIntoGraveyard cardPut = (TriggerContext.CardPutIntoGraveyard) ctx;
        var gameData = match.gameData();
        Card sourceCard = match.permanent() != null ? match.permanent().getCard() : match.sourceCard();
        if (sourceCard == null) return false;
        String cardName = sourceCard.getName();

        StackEntry entry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                sourceCard,
                match.controllerId(),
                cardName + "'s ability",
                match.stackEffects(),
                match.permanent() == null ? null : cardPut.graveyardOwnerId(),
                match.permanent() == null ? null : match.permanent().getId()
        );
        entry.setNonTargeting(!match.declaresTarget());
        entry.setTriggeringPermanentControllerId(cardPut.graveyardOwnerId());
        entry.setTriggeringCard(cardPut.card().getId(), gameData.graveyardEntryVersion(cardPut.card().getId()));
        gameData.enqueueTrigger(entry);

        gameLogService.append(gameData, GameLog.abilityTriggers(sourceCard));
        log.info("Game {} - {} triggers (card put into opponent's graveyard from anywhere)", gameData.id, cardName);
        return true;
    }

    @CollectsWatchTrigger(value = CardEffect.class, watch = ZoneChangeWatch.ON_CREATURE_CARD_PUT_INTO_OPPONENT_GRAVEYARD_FROM_ANYWHERE)
    private boolean handleCreatureCardPutIntoOpponentGraveyardDefault(TriggerMatchContext match, TriggerContext ctx) {
        var gameData = match.gameData();
        String cardName = match.permanent().getCard().getName();

        StackEntry entry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                match.permanent().getCard(),
                match.controllerId(),
                cardName + "'s ability",
                match.stackEffects(),
                null,
                match.permanent().getId());
        if (ctx instanceof TriggerContext.CreatureCardPutIntoGraveyard creatureCard
                && creatureCard.creatureCard() != null) {
            // "That card", as it was put into the graveyard, with its graveyard incarnation (CR 400.7): an effect that
            // returns "it" finds that incarnation there; "its mana value" reads the card as it was in the event.
            UUID cardId = creatureCard.creatureCard().getId();
            entry.setTriggeringCard(cardId, gameData.graveyardEntryVersion(cardId));
            entry.setTriggeringCards(List.of(creatureCard.creatureCard()));
        }
        entry.setNonTargeting(match.effects().stream().allMatch(effect -> effect.targetSpec() == TargetSpec.NONE));
        gameData.enqueueTrigger(entry);

        gameLogService.append(gameData, GameLog.abilityTriggers(match.permanent().getCard()));
        log.info("Game {} - {} triggers (creature card put into opponent's graveyard from anywhere)", gameData.id, cardName);
        return true;
    }

    @CollectsWatchTrigger(value = CardEffect.class, watch = ZoneChangeWatch.ON_NONLAND_CARDS_MILLED)
    private boolean handleNonlandCardsMilled(TriggerMatchContext match, TriggerContext ctx) {
        if (!(ctx instanceof TriggerContext.NonlandCardsMilled milled)) {
            return false;
        }

        var gameData = match.gameData();
        Card sourceCard = match.permanent().getCard();
        StackEntry entry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                sourceCard,
                match.controllerId(),
                sourceCard.getName() + "'s ability",
                match.stackEffects(),
                null,
                match.permanent().getId());
        entry.setEventValue(milled.nonlandCardCount());
        entry.setTriggeringPlayerId(milled.milledPlayerId());
        entry.setSourcePermanentSnapshot(new Permanent(match.permanent()));
        entry.setOnceEachTurnActionKey(match.onceEachTurnActionKey());
        gameData.handOffTriggerRecord(entry);
        if (match.admits(TargetPredicate.Kind.PERMANENT)
                || match.admits(TargetPredicate.Kind.PLAYER)) {
            gameData.queueInteraction(new PermanentChoiceContext.SelfTriggeredAbilityTarget(
                    sourceCard, match.controllerId(), match.stackEffects(),
                    "nonland cards milled", match.permanent().getId(), milled.nonlandCardCount()));
        } else {
            gameData.enqueueTrigger(entry);
        }

        gameLogService.append(gameData, GameLog.abilityTriggers(sourceCard));
        log.info("Game {} - {} triggers on nonland cards being milled",
                gameData.id, sourceCard.getName());
        return true;
    }

    @CollectsWatchTrigger(value = CardEffect.class, watch = ZoneChangeWatch.ON_CONTROLLER_CARDS_LEAVE_GRAVEYARD)
    boolean handleControllerCardsLeaveGraveyard(TriggerMatchContext match, TriggerContext ctx) {
        if (!handleControllerArtifactOrCreatureCardsLeaveGraveyard(match, ctx)) {
            return false;
        }

        gameLogService.append(match.gameData(), GameLog.abilityTriggers(match.permanent().getCard()));
        log.info("Game {} - {} triggers (cards left graveyard)",
                match.gameData().id, match.permanent().getCard().getName());
        return true;
    }

    @CollectsWatchTrigger(value = CardEffect.class,
            watch = ZoneChangeWatch.ON_CONTROLLER_CARD_RETURNED_FROM_GRAVEYARD_TO_HAND)
    boolean handleControllerCardReturnedFromGraveyardToHand(TriggerMatchContext match, TriggerContext ctx) {
        if (!(ctx instanceof TriggerContext.ControllerCardReturnedFromGraveyardToHand returned)) {
            return false;
        }

        Card snapshot = returned.returnedCard().createCardCopy();
        snapshot.freeze();
        StackEntry entry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                match.permanent().getCard(),
                match.controllerId(),
                match.permanent().getCard().getName() + "'s ability",
                match.stackEffects(),
                null,
                match.permanent().getId());
        entry.setTriggeringCardId(returned.returnedCard().getId());
        entry.setTriggeringCardSnapshot(snapshot);
        match.gameData().enqueueTrigger(entry);
        gameLogService.append(match.gameData(), GameLog.abilityTriggers(match.permanent().getCard()));
        log.info("Game {} - {} triggers (card returned from graveyard to hand)",
                match.gameData().id, match.permanent().getCard().getName());
        return true;
    }

    @CollectsWatchTrigger(value = CardEffect.class,
            watch = ZoneChangeWatch.ON_CONTROLLER_CARD_PUT_INTO_HAND_FROM_LIBRARY)
    boolean handleControllerCardPutIntoHandFromLibrary(TriggerMatchContext match, TriggerContext ctx) {
        if (!(ctx instanceof TriggerContext.ControllerCardPutIntoHandFromLibrary moved)) {
            return false;
        }
        return enqueueControllerCardPutIntoHandFromLibraryTrigger(match, moved.card());
    }

    private boolean enqueueControllerCardPutIntoHandFromLibraryTrigger(TriggerMatchContext match, Card movedCard) {
        Card snapshot = movedCard.createCardCopy();
        snapshot.freeze();
        StackEntry entry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                match.permanent().getCard(),
                match.controllerId(),
                match.permanent().getCard().getName() + "'s ability",
                match.stackEffects(),
                null,
                match.permanent().getId());
        entry.setTriggeringCardId(movedCard.getId());
        entry.setTriggeringCardSnapshot(snapshot);
        match.gameData().enqueueTrigger(entry);
        gameLogService.append(match.gameData(), GameLog.abilityTriggers(match.permanent().getCard()));
        log.info("Game {} - {} triggers (card put into hand from library)",
                match.gameData().id, match.permanent().getCard().getName());
        return true;
    }

    @CollectsWatchTrigger(value = CardEffect.class,
            watch = ZoneChangeWatch.ON_CONTROLLER_INSTANT_OR_SORCERY_CARD_LEAVES_GRAVEYARD)
    boolean handleControllerInstantOrSorceryCardLeavesGraveyard(TriggerMatchContext match, TriggerContext ctx) {
        if (!(ctx instanceof TriggerContext.ControllerInstantOrSorceryCardLeavesGraveyard)) {
            return false;
        }
        match.gameData().enqueueTrigger(new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                match.permanent().getCard(),
                match.controllerId(),
                match.permanent().getCard().getName() + "'s ability",
                match.stackEffects(),
                null,
                match.permanent().getId()
        ));
        gameLogService.append(match.gameData(), GameLog.abilityTriggers(match.permanent().getCard()));
        log.info("Game {} - {} triggers (instant or sorcery card left graveyard)",
                match.gameData().id, match.permanent().getCard().getName());
        return true;
    }

    @CollectsWatchTrigger(value = CardEffect.class, watch = ZoneChangeWatch.ON_CONTROLLER_CARDS_EXILED_FROM_LIBRARY_OR_GRAVEYARD)
    boolean handleControllerCardsExiledFromLibraryOrGraveyard(TriggerMatchContext match, TriggerContext ctx) {
        if (!(ctx instanceof TriggerContext.ControllerCardsExiledFromLibraryOrGraveyard exiled)) {
            return false;
        }
        StackEntry entry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                match.permanent().getCard(),
                match.controllerId(),
                match.permanent().getCard().getName() + "'s ability",
                match.stackEffects(),
                null,
                match.permanent().getId()
        );
        entry.setEventValue(exiled.count());
        match.gameData().stack.add(entry);
        gameLogService.append(match.gameData(), GameLog.abilityTriggers(match.permanent().getCard()));
        log.info("Game {} - {} triggers (cards exiled from library or graveyard)",
                match.gameData().id, match.permanent().getCard().getName());
        return true;
    }

    @CollectsWatchTrigger(value = CardEffect.class, watch = ZoneChangeWatch.ON_CONTROLLER_CARDS_EXILED_FROM_GRAVEYARD)
    boolean handleControllerCardsExiledFromGraveyard(TriggerMatchContext match, TriggerContext ctx) {
        if (!(ctx instanceof TriggerContext.ControllerCardsExiledFromGraveyard exiled)) {
            return false;
        }
        StackEntry entry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                match.permanent().getCard(),
                match.controllerId(),
                match.permanent().getCard().getName() + "'s ability",
                match.stackEffects(),
                null,
                match.permanent().getId()
        );
        entry.setEventValue(exiled.count());
        match.gameData().stack.add(entry);
        gameLogService.append(match.gameData(), GameLog.abilityTriggers(match.permanent().getCard()));
        log.info("Game {} - {} triggers (cards exiled from graveyard)",
                match.gameData().id, match.permanent().getCard().getName());
        return true;
    }

    @CollectsWatchTrigger(value = CardEffect.class,
            watch = ZoneChangeWatch.ON_CONTROLLER_CREATURES_OR_CREATURE_CARDS_EXILED)
    boolean handleControllerCreaturesOrCreatureCardsExiled(TriggerMatchContext match, TriggerContext ctx) {
        if (!(ctx instanceof TriggerContext.ControllerCreaturesOrCreatureCardsExiled exiled)) {
            return false;
        }
        StackEntry entry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                match.permanent().getCard(),
                match.controllerId(),
                match.permanent().getCard().getName() + "'s ability",
                match.stackEffects(),
                null,
                match.permanent().getId()
        );
        entry.setEventValue(exiled.count());
        entry.setTriggeringCards(exiled.creatureCards());
        match.gameData().handOffTriggerRecord(entry);
        if (match.effectOf(KayaSpiritsJusticeEffect.class) != null) {
            match.gameData().queueInteraction(new PermanentChoiceContext.DrawTriggerPermanentTarget(
                    match.permanent().getCard(),
                    match.controllerId(),
                    match.stackEffects(),
                    match.permanent().getId(),
                    new ControlledPermanentPredicateTargetFilter(
                            new PermanentIsTokenPredicate(), "Target must be a token you control"))
                    .withTriggeringCards(exiled.creatureCards()));
            gameLogService.append(match.gameData(), GameLog.abilityTriggers(match.permanent().getCard()));
            log.info("Game {} - {} triggers (creatures or creature cards exiled) and needs a token target",
                    match.gameData().id, match.permanent().getCard().getName());
            return true;
        }
        match.gameData().stack.add(entry);
        gameLogService.append(match.gameData(), GameLog.abilityTriggers(match.permanent().getCard()));
        log.info("Game {} - {} triggers (creatures or creature cards exiled)",
                match.gameData().id, match.permanent().getCard().getName());
        return true;
    }

    /**
     * "Whenever a creature is put into exile from the battlefield", its own exile included (CR 603.6c, 603.10a): the
     * entry records, whatever the effects are, "that card" with the exile incarnation it has now (CR 400.7), the
     * creature as it last existed on the battlefield with its controller and power then ("its power" is the event value,
     * CR 608.2h), the source as it triggered and the "do this only once each turn" mark (CR 603.2h). An ability that
     * targets waits for its targets as it goes on the stack (CR 603.3d), its choice carrying the creature.
     */
    @CollectsWatchTrigger(value = CardEffect.class,
            watch = ZoneChangeWatch.ON_ANY_CREATURE_EXILED_FROM_BATTLEFIELD)
    boolean handleAnyCreatureExiledFromBattlefield(TriggerMatchContext match, TriggerContext ctx) {
        if (!(ctx instanceof TriggerContext.CreatureExiledFromBattlefield exiled)) {
            return false;
        }
        Permanent source = match.permanent();
        Permanent lastKnown = exiled.exiledPermanent();
        StackEntry entry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                source.getCard(),
                match.controllerId(),
                source.getCard().getName() + "'s ability",
                match.stackEffects(),
                null,
                source.getId()
        );
        entry.setNonTargeting(true);
        Card exiledCard = lastKnown.getOriginalCard() != null ? lastKnown.getOriginalCard() : lastKnown.getCard();
        entry.setTriggeringCardId(exiledCard.getId());
        entry.setTriggeringCardExileEntryVersion(
                match.gameData().exileEntryVersions.getOrDefault(exiledCard.getId(), 0L));
        entry.setTriggeringPermanentId(lastKnown.getId());
        entry.setTriggeringPermanent(lastKnown);
        entry.setTriggeringPermanentControllerId(exiled.exiledControllerId());
        entry.setTriggeringPermanentPowerAtTrigger(exiled.exiledPowerAtTrigger());
        entry.setEventValue(exiled.exiledPowerAtTrigger());
        entry.setSourcePermanentSnapshot(new Permanent(source));
        entry.setOnceEachTurnActionKey(match.onceEachTurnActionKey());
        match.gameData().handOffTriggerRecord(entry);
        if (match.admits(TargetPredicate.Kind.PERMANENT)
                || match.admits(TargetPredicate.Kind.PLAYER)) {
            match.gameData().queueInteraction(new PermanentChoiceContext.SelfTriggeredAbilityTarget(
                    source.getCard(), match.controllerId(), match.stackEffects(),
                    "creature exiled from the battlefield", source.getId(), new Permanent(source),
                    exiled.exiledPowerAtTrigger()).withTriggeringPermanent(lastKnown));
        } else {
            match.gameData().enqueueTrigger(entry);
        }
        gameLogService.append(match.gameData(), GameLog.abilityTriggers(source.getCard()));
        log.info("Game {} - {} triggers (creature exiled from battlefield)",
                match.gameData().id, source.getCard().getName());
        return true;
    }

    @CollectsWatchTrigger(value = CardEffect.class, watch = ZoneChangeWatch.ON_CONTROLLER_CARDS_EXILED_DURING_TURN)
    boolean handleControllerCardsExiledDuringTurn(TriggerMatchContext match, TriggerContext ctx) {
        if (!(ctx instanceof TriggerContext.CardsExiledDuringTurn)) {
            return false;
        }
        match.gameData().stack.add(new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                match.permanent().getCard(),
                match.controllerId(),
                match.permanent().getCard().getName() + "'s ability",
                match.stackEffects(),
                null,
                match.permanent().getId()
        ));
        gameLogService.append(match.gameData(), GameLog.abilityTriggers(match.permanent().getCard()));
        log.info("Game {} - {} triggers (cards exiled during turn)",
                match.gameData().id, match.permanent().getCard().getName());
        return true;
    }

    @CollectsWatchTrigger(value = CardEffect.class, watch = ZoneChangeWatch.ON_OPPONENT_OWNED_CARD_EXILED)
    boolean handleOpponentOwnedCardExiled(TriggerMatchContext match, TriggerContext ctx) {
        if (!(ctx instanceof TriggerContext.OpponentOwnedCardExiled exiled)
                || match.controllerId().equals(exiled.ownerId())) {
            return false;
        }
        match.gameData().stack.add(new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                match.permanent().getCard(),
                match.controllerId(),
                match.permanent().getCard().getName() + "'s ability",
                match.stackEffects(),
                null,
                match.permanent().getId()
        ));
        gameLogService.append(match.gameData(), GameLog.abilityTriggers(match.permanent().getCard()));
        log.info("Game {} - {} triggers (opponent-owned card exiled)",
                match.gameData().id, match.permanent().getCard().getName());
        return true;
    }

    @CollectsWatchTrigger(value = CardEffect.class, watch = ZoneChangeWatch.ON_ANY_OTHER_PERMANENT_PHASES_OUT)
    @CollectsWatchTrigger(value = CardEffect.class, watch = ZoneChangeWatch.ON_ANY_CARD_EXILED)
    boolean handleGlobalPhaseOutOrCardExileTriggers(TriggerMatchContext match, TriggerContext ctx) {
        if (!(ctx instanceof TriggerContext.PermanentPhasedOut)
                && !(ctx instanceof TriggerContext.CardExiled)) {
            return false;
        }
        match.gameData().stack.add(new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                match.permanent().getCard(),
                match.controllerId(),
                match.permanent().getCard().getName() + "'s ability",
                match.stackEffects(),
                null,
                match.permanent().getId()
        ));
        gameLogService.append(match.gameData(), GameLog.abilityTriggers(match.permanent().getCard()));
        log.info("Game {} - {} triggers on a permanent phasing out or a card being exiled",
                match.gameData().id, match.permanent().getCard().getName());
        return true;
    }

    @CollectsWatchTrigger(value = CardEffect.class, watch = ZoneChangeWatch.ON_CONTROLLER_CARDS_EXILED_FROM_HAND)
    @CollectsWatchTrigger(value = CardEffect.class, watch = ZoneChangeWatch.ON_CONTROLLER_SPELL_OR_ABILITY_EXILES_PERMANENT)
    boolean handleControllerExileTriggers(TriggerMatchContext match, TriggerContext ctx) {
        if (!(ctx instanceof TriggerContext.ControllerCardsExiledFromHand)
                && !(ctx instanceof TriggerContext.ControllerSpellOrAbilityExilesPermanent)) {
            return false;
        }
        StackEntry entry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                match.permanent().getCard(),
                match.controllerId(),
                match.permanent().getCard().getName() + "'s ability",
                match.stackEffects(),
                null,
                match.permanent().getId());
        match.gameData().stack.add(entry);
        if (ctx instanceof TriggerContext.ControllerCardsExiledFromHand exiled) {
            entry.setEventValue(exiled.count());
        } else {
            entry.setEventValue(1);
        }
        gameLogService.append(match.gameData(), GameLog.abilityTriggers(match.permanent().getCard()));
        log.info("Game {} - {} triggers on cards or permanents being exiled",
                match.gameData().id, match.permanent().getCard().getName());
        return true;
    }

    @CollectsWatchTrigger(value = CardEffect.class, watch = ZoneChangeWatch.ON_CONTROLLER_CREATURE_CARDS_LEAVE_GRAVEYARD)
    @CollectsWatchTrigger(value = CardEffect.class, watch = ZoneChangeWatch.ON_CONTROLLER_CREATURE_CARD_LEAVES_GRAVEYARD)
    boolean handleControllerCreatureCardsLeaveGraveyard(TriggerMatchContext match, TriggerContext ctx) {
        match.gameData().stack.add(new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                match.permanent().getCard(),
                match.controllerId(),
                match.permanent().getCard().getName() + "'s ability",
                match.stackEffects(),
                null,
                match.permanent().getId()
        ));
        gameLogService.append(match.gameData(), GameLog.abilityTriggers(match.permanent().getCard()));
        log.info("Game {} - {} triggers (creature cards left graveyard)",
                match.gameData().id, match.permanent().getCard().getName());
        return true;
    }

    @CollectsWatchTrigger(value = CardEffect.class,
            watch = ZoneChangeWatch.ON_CONTROLLER_ARTIFACT_OR_CREATURE_CARDS_LEAVE_GRAVEYARD)
    boolean handleControllerArtifactOrCreatureCardsLeaveGraveyard(TriggerMatchContext match, TriggerContext ctx) {
        if (!(ctx instanceof TriggerContext.ControllerCardsLeaveGraveyard cardsLeft)) {
            return false;
        }

        StackEntry entry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                match.permanent().getCard(),
                match.controllerId(),
                match.permanent().getCard().getName() + "'s ability",
                match.stackEffects(),
                null,
                match.permanent().getId()
        );
        entry.setTriggeringCards(cardsLeft.cards());
        match.gameData().handOffTriggerRecord(entry);
        if (match.admits(TargetPredicate.Kind.PERMANENT)
                || match.admits(TargetPredicate.Kind.PLAYER)) {
            match.gameData().queueInteraction(new PermanentChoiceContext.SelfTriggeredAbilityTarget(
                    match.permanent().getCard(), match.controllerId(), match.stackEffects(),
                    "leaves-the-graveyard", match.permanent().getId()));
        } else {
            match.gameData().enqueueTrigger(entry);
        }
        return true;
    }

    @CollectsWatchTrigger(value = CardEffect.class,
            watch = ZoneChangeWatch.ON_CONTROLLER_ARTIFACT_CARDS_LEAVE_GRAVEYARD)
    boolean handleControllerArtifactCardsLeaveGraveyard(TriggerMatchContext match, TriggerContext ctx) {
        StackEntry entry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                match.permanent().getCard(),
                match.controllerId(),
                match.permanent().getCard().getName() + "'s ability",
                match.stackEffects(),
                null,
                match.permanent().getId()
        );
        match.gameData().handOffTriggerRecord(entry);
        if (match.admits(TargetPredicate.Kind.PERMANENT)
                || match.admits(TargetPredicate.Kind.PLAYER)) {
            match.gameData().queueInteraction(new PermanentChoiceContext.SelfTriggeredAbilityTarget(
                    match.permanent().getCard(), match.controllerId(), match.stackEffects(),
                    "artifact-leaves-the-graveyard", match.permanent().getId()));
        } else {
            match.gameData().enqueueTrigger(entry);
        }
        return true;
    }

    @CollectsWatchTrigger(value = CardEffect.class,
            watch = ZoneChangeWatch.ON_CARDS_EXILED_FROM_GRAVEYARDS_OR_BATTLEFIELD_DURING_YOUR_TURN)
    boolean handleCardsExiledFromGraveyardsOrBattlefieldDuringYourTurn(TriggerMatchContext match, TriggerContext ctx) {
        if (!(ctx instanceof TriggerContext.CardsExiledFromGraveyardsOrBattlefield exiled)) {
            return false;
        }
        StackEntry entry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                match.permanent().getCard(),
                match.controllerId(),
                match.permanent().getCard().getName() + "'s ability",
                match.stackEffects(),
                null,
                match.permanent().getId()
        );
        entry.setEventValue(exiled.count());
        match.gameData().stack.add(entry);
        gameLogService.append(match.gameData(), GameLog.abilityTriggers(match.permanent().getCard()));
        log.info("Game {} - {} triggers (cards exiled from graveyards or battlefield during your turn)",
                match.gameData().id, match.permanent().getCard().getName());
        return true;
    }

    /**
     * Records on {@code entry} the cards a put-into-a-graveyard event was about, whatever its effects are: as its
     * triggering cards ("them", "one of them"), each with the graveyard stay it went to (a card that left the graveyard
     * and returned is a new object, CR 400.7), and a lone card also as its triggering card ("that card").
     */
    static void recordCardsPutIntoGraveyard(GameData gameData, StackEntry entry, List<Card> cards) {
        entry.setTriggeringCards(cards);
        for (Card card : cards) {
            entry.getEventCardGraveyardEntryVersions().put(card.getId(), gameData.graveyardEntryVersion(card.getId()));
        }
        if (cards.size() == 1) {
            entry.setTriggeringCard(cards.getFirst().getId(), gameData.graveyardEntryVersion(cards.getFirst().getId()));
        }
    }
}

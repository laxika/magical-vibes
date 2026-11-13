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

    /**
     * "Whenever a card is put into your graveyard from anywhere" (and from your library, from your hand or library,
     * or from anywhere but the battlefield, of a filter): the cards recorded whatever the effects are
     * ({@link #queueGraveyardCardsAbility}).
     */
    @CollectsWatchTrigger(value = CardEffect.class,
                    watch = ZoneChangeWatch.ON_ALLY_CARD_PUT_INTO_GRAVEYARD_FROM_ANYWHERE)
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

    /**
     * "Whenever a nonblack card is put into a player's graveyard from anywhere, that player …" (The Dark Barony) and
     * "whenever a black card is put into an opponent's graveyard from anywhere" (Compost): the card recorded whatever
     * the effects are ({@link #queueGraveyardCardsAbility}), the graveyard's owner the non-targeting "that player".
     */
    @CollectsWatchTrigger(value = CardEffect.class,
            watch = ZoneChangeWatch.ON_NONBLACK_CARD_PUT_INTO_GRAVEYARD_FROM_ANYWHERE)
    private boolean handleCardPutIntoPlayersGraveyardDefault(TriggerMatchContext match, TriggerContext ctx) {
        TriggerContext.CardPutIntoGraveyard cardPut = (TriggerContext.CardPutIntoGraveyard) ctx;
        return queueGraveyardCardsAbility(match, List.of(cardPut.card()), cardPut.graveyardOwnerId(),
                cardPut.graveyardOwnerId(), null, "card put into a player's graveyard from anywhere");
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

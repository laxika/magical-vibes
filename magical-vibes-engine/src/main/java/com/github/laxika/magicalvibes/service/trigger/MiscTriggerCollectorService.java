package com.github.laxika.magicalvibes.service.trigger;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;
import com.github.laxika.magicalvibes.service.effect.ConditionContext;
import com.github.laxika.magicalvibes.service.trigger.pipeline.TriggeredAbilityRequest;
import com.github.laxika.magicalvibes.service.trigger.pipeline.TriggeredAbilityStackService;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
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

import java.util.List;
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

    @org.springframework.beans.factory.annotation.Autowired
    @Lazy
    private TriggeredAbilityStackService triggeredAbilityStackService;

    public MiscTriggerCollectorService(GameLogService gameLogService,
                                       @Lazy GraveyardService graveyardService,
                                       GameQueryService gameQueryService,
                                       @Lazy DrawService drawService,
                                       ConditionEvaluationService conditionEvaluationService,
                                       @Lazy ETBTokenTargetService etbTokenTargetService,
                                       @Lazy PermanentRemovalService permanentRemovalService) {
        this.gameLogService = gameLogService;
        this.graveyardService = graveyardService;
        this.gameQueryService = gameQueryService;
        this.drawService = drawService;
        this.conditionEvaluationService = conditionEvaluationService;
        this.etbTokenTargetService = etbTokenTargetService;
        this.permanentRemovalService = permanentRemovalService;
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
     * Puts the ability of {@code match}'s effects that cards put into a graveyard triggered on the stack through the
     * stack door, whatever its effects are: its record holds {@code cards} ({@link #recordCardsPutIntoGraveyard}:
     * "that card", "them", each with the graveyard stay it went to, CR 400.7), {@code triggeringPlayerId} as its
     * triggering player and {@code thatPlayerId} as its non-targeting "that player", {@code eventValue} ("that many")
     * when not {@code null}, the source as it triggered (a permanent's snapshot, a plane's planar object, CR 608.2h)
     * and the "do this only once each turn" mark (CR 603.2h). Returns whether it triggered: an ability of no source
     * doesn't.
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
        Permanent snapshot = source == null ? null : new Permanent(source);
        String onceEachTurnActionKey = match.onceEachTurnActionKey();
        TriggeredAbility ability = new TriggeredAbility(
                Triggers.putIntoGraveyardFromAnywhere(Triggers.NONBLACK_CARD, ObjectRelation.ANOTHER),
                match.stackEffects().toArray(CardEffect[]::new));
        boolean triggered = triggeredAbilityStackService.put(gameData, new TriggeredAbilityRequest(ability,
                sourceCard, source, match.controllerId(), sourceCard.getName() + "'s ability",
                source != null ? ConditionContext.forPermanent(source, match.controllerId())
                        : ConditionContext.forCard(sourceCard, match.controllerId()), entry -> {
                    entry.setTargetId(thatPlayerId);
                    entry.setNonTargeting(true);
                    recordCardsPutIntoGraveyard(gameData, entry, cards);
                    entry.setTriggeringPlayerId(triggeringPlayerId);
                    if (eventValue != null) {
                        entry.setEventValue(eventValue);
                    }
                    if (snapshot != null) {
                        entry.setSourcePermanentSnapshot(snapshot);
                    }
                    if (match.sourcePlanarObject() != null) {
                        entry.setSourcePlanarObject(match.sourcePlanarObject().copy());
                    }
                    if (onceEachTurnActionKey != null) {
                        entry.setOnceEachTurnActionKey(onceEachTurnActionKey);
                    }
                }));
        if (triggered) {
            gameLogService.append(gameData, GameLog.abilityTriggers(sourceCard));
            log.info("Game {} - {} triggers ({})", gameData.id, sourceCard.getName(), event);
        }
        return triggered;
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

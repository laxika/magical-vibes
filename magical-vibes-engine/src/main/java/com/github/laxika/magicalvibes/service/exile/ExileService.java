package com.github.laxika.magicalvibes.service.exile;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ExilePlayGrantBySource;
import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.model.effect.ExilePlayPermissionEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.SubjectResolutionService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Puts cards into exile, tracked with the permanent that exiled them when there is one. A card exiled with a source
 * that has a this-turn "you may play cards exiled with this" grant ({@link ExilePlayGrantBySource}) gets that
 * permission as it arrives, when the grant covers it (CR 611.2c).
 */
@Service
public class ExileService {

    private final GameQueryService gameQueryService;
    private final PredicateEvaluationService predicateEvaluationService;

    @Autowired
    public ExileService(GameQueryService gameQueryService, PredicateEvaluationService predicateEvaluationService) {
        this.gameQueryService = gameQueryService;
        this.predicateEvaluationService = predicateEvaluationService;
    }

    public ExileService(GameQueryService gameQueryService) {
        this(gameQueryService, null);
    }

    /**
     * Exiles a card, adding it to the specified player's exile zone.
     */
    public void exileCard(GameData gameData, UUID ownerId, Card card) {
        gameData.addToExile(ownerId, card);
    }

    /**
     * Exiles a card and tracks it with a source permanent (imprint).
     */
    public void exileCard(GameData gameData, UUID ownerId, Card card, UUID sourcePermanentId) {
        gameData.addToExile(ownerId, card, sourcePermanentId);
        grantSourcePlayPermission(gameData, card, sourcePermanentId);
    }

    /** Exiles a card face up, tracked with a source permanent, while remembering which player exiled it. */
    public void exileCard(GameData gameData, UUID ownerId, Card card, UUID sourcePermanentId, UUID exilerId) {
        gameData.addToExile(ownerId, card, sourcePermanentId, false, exilerId);
        grantSourcePlayPermission(gameData, card, sourcePermanentId);
    }

    /**
     * Exiles a card face down (CR 406.3), optionally tracked with a source permanent.
     */
    public void exileCardFaceDown(GameData gameData, UUID ownerId, Card card, UUID sourcePermanentId) {
        gameData.addToExile(ownerId, card, sourcePermanentId, true);
        grantSourcePlayPermission(gameData, card, sourcePermanentId);
    }

    /** Exiles a card face down while remembering which player exiled it. */
    public void exileCardFaceDown(GameData gameData, UUID ownerId, Card card, UUID sourcePermanentId,
                                  UUID exilerId) {
        gameData.addToExile(ownerId, card, sourcePermanentId, true, exilerId);
        grantSourcePlayPermission(gameData, card, sourcePermanentId);
    }

    /**
     * Records an exiled card as the imprint of the permanent that exiled it. No-op if the
     * source permanent has already left the battlefield.
     */
    public void setImprintedCardOnPermanent(GameData gameData, UUID sourcePermanentId, Card card) {
        Permanent source = gameQueryService.findPermanentById(gameData, sourcePermanentId);
        if (source != null) {
            gameData.setImprintedCard(source.getCard(), card);
        }
        gameData.exiledCards.replaceAll(exiled -> exiled.card().getId().equals(card.getId())
                ? new com.github.laxika.magicalvibes.model.ExiledCardEntry(exiled.card(), exiled.ownerId(),
                        sourcePermanentId, exiled.faceDown(), exiled.exilerId(), exiled.exiledTurnNumber(),
                        exiled.controllerTurnsTakenAtExile(), exiled.abilityLink())
                : exiled);
    }

    /**
     * Gives the card just exiled with {@code sourcePermanentId} the source's this-turn play grant when the grant covers
     * it: the cards a grant over the cards exiled with the source names include the ones exiled with it after it began
     * (CR 611.2c). A card a replacement kept out of exile, or a source no longer on the battlefield, gets nothing.
     */
    private void grantSourcePlayPermission(GameData gameData, Card card, UUID sourcePermanentId) {
        ExilePlayGrantBySource grant = sourcePermanentId == null ? null
                : gameData.exilePlayGrantBySource(sourcePermanentId);
        if (grant == null) {
            return;
        }
        ExiledCardEntry exiled = gameData.findExiledCard(card.getId());
        Permanent source = gameQueryService.findPermanentById(gameData, sourcePermanentId);
        ExilePlayPermissionEffect permission = grant.permission();
        if (exiled == null || source == null || !SubjectResolutionService.namesExiledCard(source,
                permission.holderId(), exiled, grant.cards(), (judged, ownerId, filter) ->
                        predicateEvaluationService != null && predicateEvaluationService.matchesCardPredicate(
                                judged, filter, source.getCard().getId(), gameData, ownerId))) {
            return;
        }
        gameData.grantExilePlayPermission(card.getId(),
                card.hasType(CardType.LAND) ? permission.withCostModifier(null) : permission,
                null, permission.holderId(), Duration.UNTIL_END_OF_TURN);
    }
}

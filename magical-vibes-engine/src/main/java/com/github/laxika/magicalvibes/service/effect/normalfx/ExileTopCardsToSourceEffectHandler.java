package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.AllowCastFromCardsExiledWithSourceEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardsToSourceEffect;
import com.github.laxika.magicalvibes.model.ExileAccessScope;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.LibraryScope;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import com.github.laxika.magicalvibes.service.exile.ExileService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import java.util.List;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Exiles the top N cards of one or more libraries, tracked "exiled with" the source permanent.
 * The {@code scope} picks the exiling players: the controller (Colfenor's Plans, Duplicity, Search
 * the City), a single opponent (Grimoire Thief face down, Nightveil Specter face up), or every
 * player (Knowledge Pool).
 */
@Slf4j
@Component
public class ExileTopCardsToSourceEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final AmountEvaluationService amountEvaluationService;
    private final ExileService exileService;
    private final PredicateEvaluationService predicateEvaluationService;

    @Autowired
    public ExileTopCardsToSourceEffectHandler(GameQueryService gameQueryService,
                                              GameLogService gameLogService,
                                              AmountEvaluationService amountEvaluationService,
                                              ExileService exileService,
                                              PredicateEvaluationService predicateEvaluationService) {
        this.gameQueryService = gameQueryService;
        this.gameLogService = gameLogService;
        this.amountEvaluationService = amountEvaluationService;
        this.exileService = exileService;
        this.predicateEvaluationService = predicateEvaluationService;
    }

    public ExileTopCardsToSourceEffectHandler(GameQueryService gameQueryService,
                                              GameLogService gameLogService,
                                              ExileService exileService) {
        this(gameQueryService, gameLogService, null, exileService, null);
    }

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileTopCardsToSourceEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (ExileTopCardsToSourceEffect) effect;
        UUID controllerId = entry.getControllerId();

        // Find the source permanent so exiled cards are tracked "with" it.
        UUID sourcePermanentId = entry.getSourcePermanentId();
        Permanent sourcePermanent = sourcePermanentId != null
                ? gameQueryService.findPermanentById(gameData, sourcePermanentId) : null;
        if (sourcePermanent == null) {
            List<Permanent> bf = gameData.playerBattlefields.get(controllerId);
            if (bf != null) {
                for (Permanent p : bf) {
                    if (p.getCard().getId().equals(entry.getCard().getId())) {
                        sourcePermanent = p;
                        sourcePermanentId = p.getId();
                        break;
                    }
                }
            }
        }

        Card sourceCard = sourcePermanent != null ? sourcePermanent.getCard() : entry.getCard();
        List<AllowCastFromCardsExiledWithSourceEffect> persistentPermissions = sourceCard
                .getEffects(EffectSlot.STATIC).stream()
                .filter(AllowCastFromCardsExiledWithSourceEffect.class::isInstance)
                .map(AllowCastFromCardsExiledWithSourceEffect.class::cast)
                .filter(AllowCastFromCardsExiledWithSourceEffect::persistsAfterSourceLeaves)
                .toList();
        if (sourcePermanent == null && persistentPermissions.isEmpty()) {
            log.info("Game {} - Source permanent no longer on battlefield, exile-top-cards fizzles", gameData.id);
            return;
        }

        if (e.toGraveyardOnControlLoss() && sourcePermanent != null) {
            gameData.exiledCardsToGraveyardOnControlLossWatch.put(sourcePermanentId, controllerId);
        }

        for (UUID playerId : exilingPlayers(gameData, entry, e.scope(), controllerId)) {
            exileTopCards(gameData, e, playerId, sourceCard, sourcePermanentId,
                    effectiveCount(gameData, entry, sourcePermanent, e), persistentPermissions,
                    controllerId);
        }
    }

    private int effectiveCount(GameData gameData, StackEntry entry, Permanent sourcePermanent,
                               ExileTopCardsToSourceEffect effect) {
        if (effect.dynamicCount() == null) {
            return effect.count();
        }
        return Math.max(0, amountEvaluationService.evaluate(gameData, effect.dynamicCount(),
                AmountContext.forStackEntry(entry, sourcePermanent)));
    }

    /** The players who exile, in the order they do so. */
    private List<UUID> exilingPlayers(GameData gameData, StackEntry entry, LibraryScope scope,
                                      UUID controllerId) {
        return switch (scope) {
            case CONTROLLER -> List.of(controllerId);
            case TARGET_PLAYER -> entry.getTargetId() != null
                    && gameData.orderedPlayerIds.contains(entry.getTargetId())
                    ? List.of(entry.getTargetId()) : List.of();
            case EACH_PLAYER -> List.copyOf(gameData.orderedPlayerIds);
            case TARGET_OPPONENT -> {
                // Combat-damage triggers bind the damaged player as the target, while attack
                // triggers retain the attacked player or planeswalker in attackedTargetId.
                UUID opponentId = entry.getTargetId() != null
                        && gameData.orderedPlayerIds.contains(entry.getTargetId())
                        && !entry.getTargetId().equals(controllerId)
                        ? entry.getTargetId()
                        : defendingPlayerId(gameData, entry.getAttackedTargetId(), controllerId);
                yield opponentId == null ? List.of() : List.of(opponentId);
            }
        };
    }

    private UUID defendingPlayerId(GameData gameData, UUID attackedTargetId, UUID controllerId) {
        if (attackedTargetId != null) {
            if (gameData.playerIds.contains(attackedTargetId)) {
                return attackedTargetId;
            }
            UUID planeswalkerControllerId = gameQueryService.findPermanentController(gameData, attackedTargetId);
            if (planeswalkerControllerId != null) {
                return planeswalkerControllerId;
            }
        }
        return gameData.orderedPlayerIds.stream()
                .filter(id -> !id.equals(controllerId))
                .findFirst().orElse(null);
    }

    private void exileTopCards(GameData gameData, ExileTopCardsToSourceEffect e, UUID playerId,
                               Card sourceCard, UUID sourcePermanentId, int count,
                               List<AllowCastFromCardsExiledWithSourceEffect> persistentPermissions,
                               UUID sourceControllerId) {
        List<Card> deck = gameData.playerDecks.get(playerId);
        if (deck == null) {
            return;
        }

        int toExile = Math.min(count, deck.size());
        for (int i = 0; i < toExile; i++) {
            Card card = deck.removeFirst();
            if (e.faceDown()) {
                exileService.exileCardFaceDown(gameData, playerId, card, sourcePermanentId);
            } else {
                exileService.exileCard(gameData, playerId, card, sourcePermanentId);
            }
            for (AllowCastFromCardsExiledWithSourceEffect permission : persistentPermissions) {
                if (permission.filter() == null || (predicateEvaluationService != null
                        && predicateEvaluationService.matchesCardPredicate(card, permission.filter(), null))) {
                    UUID permittedPlayer = permission.accessScope() == ExileAccessScope.EXILER
                            ? playerId : sourceControllerId;
                    gameData.exilePlayPermissions.put(card.getId(), permittedPlayer);
                    if (permission.anyManaType()) {
                        gameData.exilePlayAnyManaTypeWhileExiled.add(card.getId());
                    }
                    break;
                }
            }
        }

        if (toExile > 0) {
            String playerName = gameData.playerIdToName.get(playerId);
            // Card names are never listed: for a face-down exile that would leak information
            // CR 406.3 keeps hidden.
            String visibility = e.faceDown() ? " face down" : "";
            gameLogService.append(gameData, GameLog.builder()
                    .text(playerName + " exiles the top " + toExile + " card"
                            + (toExile != 1 ? "s" : "") + " of their library" + visibility + " (")
                    .card(sourceCard).text(").").build());
            log.info("Game {} - {} exiles {} cards from library to {}",
                    gameData.id, playerName, toExile, sourceCard.getName());
        }
    }
}

package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ExiledCardsControlLossWatch;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.condition.Condition;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
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
import com.github.laxika.magicalvibes.service.trigger.TriggerCollectionService;
import java.util.ArrayList;
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
    private TriggerCollectionService triggerCollectionService;

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

    @Autowired(required = false)
    void setTriggerCollectionService(TriggerCollectionService triggerCollectionService) {
        this.triggerCollectionService = triggerCollectionService;
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
        if (sourcePermanentId == null) {
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

        Card sourceCard = sourcePermanent != null
                ? sourcePermanent.getCard() : entry.getTargetingCard();
        List<PersistentPermission> persistentPermissions = sourceCard
                .getEffects(EffectSlot.STATIC).stream()
                .map(ExileTopCardsToSourceEffectHandler::persistentPermission)
                .filter(java.util.Objects::nonNull)
                .toList();
        if (e.toGraveyardOnControlLoss() && sourcePermanent != null) {
            UUID currentControllerId = gameData.findControllerOf(sourcePermanent);
            gameData.exiledCardsToGraveyardOnControlLossWatch.put(sourcePermanentId,
                    new ExiledCardsControlLossWatch(currentControllerId, sourceCard));
        }

        List<UUID> exiledCardIds = new ArrayList<>();
        for (UUID playerId : exilingPlayers(gameData, entry, e.scope(), controllerId)) {
            exileTopCards(gameData, e, playerId, sourceCard, sourcePermanentId,
                    effectiveCount(gameData, entry, sourcePermanent, e), persistentPermissions,
                    controllerId, exiledCardIds);
        }
        entry.setResolutionExiledCardIds(exiledCardIds);
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
            case EACH_OPPONENT -> gameData.orderedPlayerIds.stream()
                    .filter(id -> !id.equals(controllerId))
                    .toList();
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

    /**
     * A static play permission that survives its source leaving the battlefield, together with the
     * condition (for example "during any turn you attacked with a commander") that must hold
     * whenever the exiled card is played, or {@code null} when the permission is unconditional.
     */
    private record PersistentPermission(AllowCastFromCardsExiledWithSourceEffect effect, Condition condition) {
    }

    private static PersistentPermission persistentPermission(CardEffect staticEffect) {
        Condition condition = null;
        CardEffect inner = staticEffect;
        if (staticEffect instanceof ConditionalEffect conditional) {
            condition = conditional.condition();
            inner = conditional.wrapped();
        }
        if (inner instanceof AllowCastFromCardsExiledWithSourceEffect permission
                && permission.persistsAfterSourceLeaves()) {
            return new PersistentPermission(permission, condition);
        }
        return null;
    }

    /** Gives a card just exiled with the source the source's active "until end of turn, you may play" permission. */
    private void grantUntilEndOfTurnPlayPermission(GameData gameData, Card card, UUID sourcePermanentId) {
        var grant = sourcePermanentId == null ? null
                : gameData.exilePlayUntilEndOfTurnGrantsBySource.get(sourcePermanentId);
        if (grant == null) {
            return;
        }
        gameData.exilePlayPermissions.put(card.getId(), grant.permittedPlayerId());
        gameData.exilePlayPermissionsExpireEndOfTurn.add(card.getId());
        if (!card.hasType(CardType.LAND) && grant.amount() < 0) {
            gameData.exilePlayCostModifiers.put(card.getId(), grant);
        }
    }

    private void exileTopCards(GameData gameData, ExileTopCardsToSourceEffect e, UUID playerId,
                               Card sourceCard, UUID sourcePermanentId, int count,
                               List<PersistentPermission> persistentPermissions,
                               UUID sourceControllerId, List<UUID> exiledCardIds) {
        List<Card> deck = gameData.playerDecks.get(playerId);
        if (deck == null) {
            return;
        }

        int toExile = Math.min(count, deck.size());
        for (int i = 0; i < toExile; i++) {
            Card card = deck.removeFirst();
            exiledCardIds.add(card.getId());
            if (e.faceDown()) {
                exileService.exileCardFaceDown(gameData, playerId, card, sourcePermanentId, sourceControllerId);
            } else if (persistentPermissions.isEmpty()) {
                exileService.exileCard(gameData, playerId, card, sourcePermanentId);
            } else {
                exileService.exileCard(gameData, playerId, card, sourcePermanentId, sourceControllerId);
            }
            if (e.faceDown() && (e.exilerMayLookWhileExiled()
                    || e.mayLookAtFaceDownCards() && sourcePermanentId == null)) {
                // A spell has no permanent whose face-down exile pile the view could reveal, so the
                // controller's "you may look at" permission is recorded per card instead.
                gameData.exileLookPermissions.put(card.getId(), sourceControllerId);
            }
            grantUntilEndOfTurnPlayPermission(gameData, card, sourcePermanentId);
            if (e.markWithIntelCounters()) {
                gameData.exiledCardsWithIntelCounters.add(card.getId());
            }
            for (PersistentPermission persistent : persistentPermissions) {
                AllowCastFromCardsExiledWithSourceEffect permission = persistent.effect();
                if (permission.filter() == null || (predicateEvaluationService != null
                        && predicateEvaluationService.matchesCardPredicate(card, permission.filter(), null))) {
                    UUID permittedPlayer = permission.accessScope() == ExileAccessScope.EXILER
                            ? playerId : sourceControllerId;
                    gameData.exilePlayPermissions.put(card.getId(), permittedPlayer);
                    if (persistent.condition() != null) {
                        gameData.exilePlayPermissionConditions.put(card.getId(), persistent.condition());
                    }
                    if (permission.thisTurnOnly()) {
                        gameData.exilePlayPermissionsExpireAtTurnEnd.put(card.getId(), gameData.turnNumber);
                    }
                    if (permission.anyManaType()) {
                        if (permission.thisTurnOnly()) {
                            gameData.exilePlayAnyManaType.add(card.getId());
                        } else {
                            gameData.exilePlayAnyManaTypeWhileExiled.add(card.getId());
                        }
                    }
                    break;
                }
            }
        }

        if (toExile > 0) {
            if (triggerCollectionService != null) {
                triggerCollectionService.checkControllerCardsExiledFromLibraryOrGraveyardTriggers(
                        gameData, playerId, toExile);
            }
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

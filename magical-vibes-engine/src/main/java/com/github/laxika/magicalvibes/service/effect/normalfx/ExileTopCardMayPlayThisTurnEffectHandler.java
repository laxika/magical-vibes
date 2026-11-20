package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.StepOutcome;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardMayPlayThisTurnEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import com.github.laxika.magicalvibes.service.effect.SubjectResolutionService;
import com.github.laxika.magicalvibes.service.exile.ExileService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Oracle's Vault: exile the top card of the controller's library; until end of turn the controller
 * may play that card (any type). The free variant additionally lets it be cast without paying its
 * mana cost. Another library ("that player exiles the top card of their library", Fallen Shinobi): each player the
 * effect's {@code library} names exiles from their own, in APNAP order, and the controller (or the player the effect's
 * {@code player} names, as the effect resolves) may play the cards for the effect's duration, whose "your" is that
 * player's (face down, that player may look at them); with no such player, nothing is exiled.
 * Records the {@link StepKind#EXILE} step: the cards exiled and their owners.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ExileTopCardMayPlayThisTurnEffectHandler implements NormalEffectHandlerBean {

    private final ExileService exileService;
    private final GameLogService gameLogService;
    private final GameQueryService gameQueryService;
    private final AmountEvaluationService amountEvaluationService;
    private final PredicateEvaluationService predicateEvaluationService;
    private final SubjectResolutionService subjectResolutionService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileTopCardMayPlayThisTurnEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        ExileTopCardMayPlayThisTurnEffect exileEffect = (ExileTopCardMayPlayThisTurnEffect) effect;
        entry.recordStep(StepKind.EXILE, StepOutcome.NONE);
        Permanent source = entry.getSourcePermanentId() == null ? null
                : gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        if (source == null) {
            source = entry.getSourcePermanentSnapshot();
        }
        int count = amountEvaluationService.evaluate(gameData, exileEffect.count(),
                AmountContext.forStackEntry(entry, source));
        if (count <= 0) {
            return;
        }
        UUID holderId = subjectResolutionService.player(gameData, entry, effect, exileEffect.player());
        if (holderId == null) {
            return;
        }
        for (UUID libraryOwnerId : subjectResolutionService.players(gameData, entry, effect, exileEffect.library())) {
            exileFrom(gameData, exileEffect, entry, source, count, libraryOwnerId, holderId);
        }
    }

    private void exileFrom(GameData gameData, ExileTopCardMayPlayThisTurnEffect exileEffect, StackEntry entry,
                           Permanent source, int count, UUID libraryOwnerId, UUID holderId) {
        boolean withoutPaying = exileEffect.withoutPayingManaCost();
        Duration duration = exileEffect.duration();
        List<Card> deck = gameData.playerDecks.get(libraryOwnerId);
        String ownerName = gameData.playerIdToName.get(libraryOwnerId);

        if (deck == null || deck.isEmpty()) {
            gameLogService.append(gameData,
                    GameLog.text(ownerName + "'s library is empty — nothing to exile."));
            return;
        }

        String verb = exileEffect.castOnly() ? "cast" : "play";
        String player = libraryOwnerId.equals(holderId) ? "" : gameData.playerIdToName.get(holderId) + " ";
        String playNote = " (" + player + "may " + verb + " it"
                + (withoutPaying ? " without paying its mana cost" : "")
                + durationText(gameData, holderId, duration) + ")";

        for (int i = 0; i < count && !deck.isEmpty(); i++) {
            Card topCard = deck.removeFirst();
            if (exileEffect.faceDown()) {
                exileService.exileCardFaceDown(gameData, libraryOwnerId, topCard, null, holderId);
            } else {
                exileService.exileCard(gameData, libraryOwnerId, topCard);
            }
            entry.addToStep(StepKind.EXILE, StepOutcome.ofObjects(List.of(topCard.getId()), List.of(libraryOwnerId)));

            boolean permitted = !exileEffect.castOnly() || !topCard.hasType(CardType.LAND);
            if (permitted) {
                // Play permission for the effect's duration ("you may cast": not for a land card).
                gameData.grantExilePlayPermission(topCard.getId(), holderId, duration);
                if (exileEffect.duringTurnsLoreCounterPutOnSource() && source != null) {
                    gameData.grantExilePlayPermissionRenewalOnLoreCounter(topCard.getId(), source.getId(), holderId);
                }
                if (exileEffect.permissionCondition() != null) {
                    gameData.updateExilePlayPermission(topCard.getId(),
                            granted -> granted.withCondition(exileEffect.permissionCondition()));
                }
                if (exileEffect.forLife()) {
                    gameData.updateExilePlayPermission(topCard.getId(),
                            granted -> granted.withForLifeEqualToManaValue(true));
                }
                boolean freeCast = withoutPaying || exileEffect.freeCastFilter() != null
                        && predicateEvaluationService.matchesCardPredicate(
                                topCard, exileEffect.freeCastFilter(), null);
                if (freeCast) {
                    gameData.setExilePlayWithoutPayingManaCost(topCard.getId(), true);
                } else if (exileEffect.anyManaType() && !topCard.hasType(CardType.LAND)) {
                    gameData.grantExileAnyManaType(topCard.getId(), holderId, duration);
                }
            }

            GameLog.Builder line = GameLog.builder().text(ownerName + " exiles ");
            if (exileEffect.faceDown()) {
                line.text("a card face down");
            } else {
                line.card(topCard);
            }
            gameLogService.append(gameData, line
                    .text(" from the top of their library" + (permitted ? playNote : "") + ".").build());
            log.info("Game {} - {} exiles {} from library top (withoutPaying={}, duration={})",
                    gameData.id, ownerName, topCard.getName(), withoutPaying, duration);
        }
    }

    /** How long the permission lasts, as the log says it; "your" is the permission holder's. */
    private static String durationText(GameData gameData, UUID controllerId, Duration duration) {
        String controllerName = gameData.playerIdToName.get(controllerId);
        if (Duration.UNTIL_END_OF_TURN.equals(duration)) {
            return " this turn";
        } else if (Duration.UNTIL_YOUR_NEXT_END_STEP.equals(duration)) {
            return " until " + controllerName + "'s next end step";
        } else if (Duration.UNTIL_END_OF_YOUR_NEXT_TURN.equals(duration)) {
            return " until the end of " + controllerName + "'s next turn";
        } else if (Duration.DURING_YOUR_NEXT_TURN.equals(duration)) {
            return " during " + controllerName + "'s next turn";
        } else if (Duration.UNTIL_CONTROLLERS_NEXT_UPKEEP.equals(duration)) {
            return " until " + controllerName + "'s next upkeep";
        } else if (Duration.PERMANENT.equals(duration)) {
            return " for as long as it remains exiled";
        }
        return "";
    }
}

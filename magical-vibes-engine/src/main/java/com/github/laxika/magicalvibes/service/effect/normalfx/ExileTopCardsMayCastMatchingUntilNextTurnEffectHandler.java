package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardsMayCastMatchingUntilNextTurnEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import com.github.laxika.magicalvibes.service.exile.ExileService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class ExileTopCardsMayCastMatchingUntilNextTurnEffectHandler implements NormalEffectHandlerBean {

    private final ExileService exileService;
    private final ExileSupport exileSupport;
    private final GameLogService gameLogService;
    private final GameQueryService gameQueryService;
    private final AmountEvaluationService amountEvaluationService;
    private final PredicateEvaluationService predicateEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileTopCardsMayCastMatchingUntilNextTurnEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        ExileTopCardsMayCastMatchingUntilNextTurnEffect exileEffect =
                (ExileTopCardsMayCastMatchingUntilNextTurnEffect) effect;
        Permanent source = entry.getSourcePermanentId() != null
                ? gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId())
                : null;
        if (source == null) {
            source = entry.getSourcePermanentSnapshot();
        }
        int count = amountEvaluationService.evaluate(gameData, exileEffect.count(),
                AmountContext.forStackEntry(entry, source));
        if (count <= 0) {
            return;
        }

        UUID controllerId = entry.getControllerId();
        List<Card> deck = gameData.playerDecks.get(controllerId);
        String controllerName = gameData.playerIdToName.get(controllerId);
        if (deck == null || deck.isEmpty()) {
            gameLogService.append(gameData,
                    GameLog.text(controllerName + "'s library is empty — nothing to exile."));
            return;
        }

        List<Card> exiled = new ArrayList<>();
        List<String> castableNames = new ArrayList<>();
        List<UUID> permittedCardIds = new ArrayList<>();
        for (int i = 0; i < count && !deck.isEmpty(); i++) {
            Card topCard = deck.removeFirst();
            exileService.exileCard(gameData, controllerId, topCard);
            exiled.add(topCard);

            boolean matchingFace = predicateEvaluationService.matchesCardPredicate(topCard, exileEffect.filter(), null)
                    || topCard.getBackFaceCard() != null
                    && (topCard.isModalDoubleFaced()
                        || topCard.getCastingOption(com.github.laxika.magicalvibes.model.AdventureCast.class).isPresent())
                    && predicateEvaluationService.matchesCardPredicate(
                            topCard.getBackFaceCard(), exileEffect.filter(), null);
            if (matchingFace) {
                gameData.grantExilePlayPermission(topCard.getId(), controllerId, exileEffect.expireAtTurnBeginning()
                        ? Duration.UNTIL_YOUR_NEXT_TURN : Duration.UNTIL_END_OF_YOUR_NEXT_TURN);
                castableNames.add(topCard.getName());
                permittedCardIds.add(topCard.getId());
                if (exileEffect.filter() != null) {
                    gameData.updateExilePlayPermission(topCard.getId(), granted -> granted.withSpellFilter(exileEffect.filter()));
                }
            }
        }

        if (exileEffect.maximumSpells() > 0) {
            gameData.registerExilePlayPermissionGroup(UUID.randomUUID(),
                    exileEffect.maximumSpells(), permittedCardIds);
        }

        GameLog.Builder logEntry = GameLog.builder().text(controllerName + " exiles ");
        for (int i = 0; i < exiled.size(); i++) {
            if (i > 0) {
                logEntry.text(", ");
            }
            logEntry.card(exiled.get(i));
        }
        String castNote = castableNames.isEmpty()
                ? ""
                : " (may cast until " + (exileEffect.expireAtTurnBeginning() ? "the beginning" : "the end")
                        + " of next turn: " + String.join(", ", castableNames) + ")";
        gameLogService.append(gameData,
                logEntry.text(" from the top of their library" + castNote + ".").build());
        log.info("Game {} - {} exiles {} cards from library top ({} castable until next turn)",
                gameData.id, controllerName, exiled.size(), castableNames.size());
    }
}

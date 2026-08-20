package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTopUntilNonlandMayCastOrPutIntoHandEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.exile.ExileService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ExileTopUntilNonlandMayCastOrPutIntoHandEffectHandler implements NormalEffectHandlerBean {

    private final ExileService exileService;
    private final GameLogService gameLogService;
    private final AmountEvaluationService amountEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileTopUntilNonlandMayCastOrPutIntoHandEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (ExileTopUntilNonlandMayCastOrPutIntoHandEffect) effect;
        UUID controllerId = entry.getControllerId();
        List<Card> deck = gameData.playerDecks.get(controllerId);
        String playerName = gameData.playerIdToName.get(controllerId);
        String sourceName = entry.getCard().getName();
        int maxManaValue = amountEvaluationService.evaluate(gameData, e.maxManaValue(),
                AmountContext.forStackEntry(entry, null));

        if (deck == null || deck.isEmpty()) {
            gameLogService.append(gameData,
                    GameLog.text(playerName + "'s library is empty (" + sourceName + ")."));
            return;
        }

        Card hit = null;
        int exiledCount = 0;
        while (!deck.isEmpty()) {
            Card top = deck.removeFirst();
            exileService.exileCard(gameData, controllerId, top);
            exiledCount++;
            if (!top.hasType(CardType.LAND)) {
                hit = top;
                break;
            }
        }

        if (hit == null) {
            gameLogService.append(gameData, GameLog.text(
                    playerName + " exiles " + exiledCount + " card(s) from the top of their library"
                            + " — no nonland card found (" + sourceName + ")."));
            return;
        }

        int manaValue = hit.getManaValue();
        if (manaValue > maxManaValue) {
            gameData.removeFromExile(hit.getId());
            gameData.addCardToHand(controllerId, hit);
            gameLogService.append(gameData, GameLog.builder()
                    .text(playerName + " exiles cards until ").card(hit)
                    .text(" (mana value " + manaValue + ") and puts it into their hand ("
                            + sourceName + ").")
                    .build());
            return;
        }

        String prompt = "You may cast " + hit.getName()
                + " without paying its mana cost. If you don't, put it into your hand.";
        gameData.pendingMayAbilities.addFirst(new PendingMayAbility(
                entry.getCard(), controllerId, List.of(effect), prompt, hit.getId()));

        gameLogService.append(gameData, GameLog.builder()
                .text(playerName + " exiles cards until ").card(hit)
                .text(" (mana value " + manaValue + ") and may cast it without paying its mana cost (")
                .text(sourceName + ").")
                .build());
        log.info("Game {} - {} exiled {} cards with {} and may cast {} for free",
                gameData.id, playerName, exiledCount, sourceName, hit.getName());
    }
}

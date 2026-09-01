package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTopUntilNonlandMayCastByDiscardEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.exile.ExileService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Resolves The Infamous Cruelclaw's combat-damage trigger.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ExileTopUntilNonlandMayCastByDiscardEffectHandler implements NormalEffectHandlerBean {

    private final ExileService exileService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileTopUntilNonlandMayCastByDiscardEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID controllerId = entry.getControllerId();
        List<Card> library = gameData.playerDecks.get(controllerId);
        String playerName = gameData.playerIdToName.get(controllerId);

        if (library == null || library.isEmpty()) {
            gameLogService.append(gameData, GameLog.text(playerName + "'s library is empty."));
            return;
        }

        Card hit = null;
        int exiledCount = 0;
        while (!library.isEmpty()) {
            Card top = library.removeFirst();
            exileService.exileCard(gameData, controllerId, top);
            exiledCount++;
            if (!top.hasType(CardType.LAND)) {
                hit = top;
                break;
            }
        }

        if (hit == null) {
            gameLogService.append(gameData, GameLog.text(
                    playerName + " exiles " + exiledCount + " card(s) until a nonland card is found, but finds none."));
            return;
        }

        List<Card> hand = gameData.playerHands.get(controllerId);
        if (hand == null || hand.isEmpty()) {
            gameLogService.append(gameData, GameLog.builder()
                    .text(playerName + " exiles cards until ").card(hit)
                    .text(" but has no card to discard.")
                    .build());
            return;
        }

        String prompt = "You may cast " + hit.getName()
                + " by discarding a card rather than paying its mana cost.";
        gameData.pendingMayAbilities.addFirst(new PendingMayAbility(
                entry.getCard(), controllerId, List.of(effect), prompt, hit.getId()));

        gameLogService.append(gameData, GameLog.builder()
                .text(playerName + " exiles cards until ").card(hit)
                .text(" and may cast it by discarding a card instead of paying its mana cost.")
                .build());
        log.info("Game {} - {} exiled {} card(s) until {} and may cast it by discarding",
                gameData.id, playerName, exiledCount, hit.getName());
    }
}

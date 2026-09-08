package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardMayRevealMatchingTransformEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/** Offers the controller a choice to reveal a matching top card before transforming the source. */
@Slf4j
@Component
@RequiredArgsConstructor
public class LookAtTopCardMayRevealMatchingTransformEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return LookAtTopCardMayRevealMatchingTransformEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        LookAtTopCardMayRevealMatchingTransformEffect typed =
                (LookAtTopCardMayRevealMatchingTransformEffect) effect;
        UUID controllerId = entry.getControllerId();
        List<Card> deck = gameData.playerDecks.get(controllerId);
        String playerName = gameData.playerIdToName.get(controllerId);
        String sourceName = entry.getCard().getName();

        if (deck == null || deck.isEmpty()) {
            gameLogService.append(gameData,
                    GameLog.text(playerName + "'s library is empty (" + sourceName + ")."));
            return;
        }

        Card topCard = deck.getFirst();
        gameLogService.append(gameData,
                GameLog.text(playerName + " looks at the top card of their library (" + sourceName + ")."));
        log.info("Game {} - {} looks at top card: {} ({})",
                gameData.id, playerName, topCard.getName(), sourceName);

        gameData.pendingMayAbilities.addFirst(new PendingMayAbility(
                entry.getCard(),
                controllerId,
                List.of(typed),
                sourceName + " — Reveal " + topCard.getName() + "?",
                null,
                null,
                entry.getSourcePermanentId()
        ));
    }
}

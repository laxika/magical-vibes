package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRandomCardThenMassDamageEffect;
import com.github.laxika.magicalvibes.model.effect.MassDamageEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.amount.LastDiscardedCardManaValue;
import com.github.laxika.magicalvibes.service.GameLogService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/** Resolves the random-discard, mana-value mass-damage, and repeat sequence. */
@Slf4j
@Component
@RequiredArgsConstructor
public class DiscardRandomCardThenMassDamageEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final MassDamageEffectHandler massDamageEffectHandler;
    private final PlayerInteractionSupport playerInteractionSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DiscardRandomCardThenMassDamageEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID controllerId = entry.getControllerId();
        List<Card> hand = gameData.playerHands.get(controllerId);
        String playerName = gameData.playerIdToName.get(controllerId);
        String sourceName = entry.getCard().getName();

        if (hand == null || hand.isEmpty()) {
            gameLogService.append(gameData, GameLog.text(playerName + " has no cards to discard."));
            log.info("Game {} - {} has no cards to discard for {}", gameData.id, playerName, sourceName);
            return;
        }

        gameData.discardCausedByOpponent = false;
        playerInteractionSupport.resolveRandomDiscardCards(gameData, controllerId, sourceName, 1);

        massDamageEffectHandler.resolve(gameData, entry,
                new MassDamageEffect(new LastDiscardedCardManaValue(), false));

        if (!gameData.playerHands.get(controllerId).isEmpty()) {
            entry.insertEffectsToResolve(gameData.pendingEffectResolutionIndex + 1,
                    List.of(new MayEffect(
                            new DiscardRandomCardThenMassDamageEffect(),
                            "Repeat the discard and damage process?")));
        }
    }
}

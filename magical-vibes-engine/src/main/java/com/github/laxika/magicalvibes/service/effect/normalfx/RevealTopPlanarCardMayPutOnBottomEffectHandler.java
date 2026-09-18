package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.RevealTopPlanarCardMayPutOnBottomEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
@RequiredArgsConstructor
public class RevealTopPlanarCardMayPutOnBottomEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final MayEffectHandler mayEffectHandler;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RevealTopPlanarCardMayPutOnBottomEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var revealEffect = (RevealTopPlanarCardMayPutOnBottomEffect) effect;
        if (gameData.planechase == null || gameData.planechase.deck.isEmpty()) {
            return;
        }

        if (revealEffect.revealedCard() == null) {
            Card topCard = gameData.planechase.deck.getFirst();
            gameLogService.append(gameData, GameLog.builder()
                    .text("The top card of the planar deck is revealed: ")
                    .card(topCard)
                    .text(".")
                    .build());

            int effectIndex = entry.getEffectsToResolve().indexOf(effect);
            if (effectIndex < 0) {
                throw new IllegalStateException("Planar reveal effect is not present on its stack entry");
            }
            MayEffect choice = new MayEffect(
                    new RevealTopPlanarCardMayPutOnBottomEffect(topCard),
                    "Put it on the bottom of the planar deck?");
            entry.replaceEffectToResolve(effectIndex, choice);
            mayEffectHandler.resolve(gameData, entry, choice);
            return;
        }

        Card revealedCard = revealEffect.revealedCard();
        boolean removed = gameData.planechase.deck.removeIf(card ->
                Objects.equals(card.getId(), revealedCard.getId()));
        if (removed) {
            gameData.planechase.deck.add(revealedCard);
        }
    }
}

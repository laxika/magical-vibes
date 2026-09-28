package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameData.GraveyardCardAnimation;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.AnimateTargetGraveyardCardEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class AnimateTargetGraveyardCardEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return AnimateTargetGraveyardCardEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        AnimateTargetGraveyardCardEffect animation = (AnimateTargetGraveyardCardEffect) effect;
        UUID targetCardId = entry.getTargetCardIds().isEmpty()
                ? entry.getTargetId()
                : entry.getTargetCardIds().getFirst();
        Card targetCard = targetCardId == null
                ? null
                : gameQueryService.findCardInGraveyardById(gameData, targetCardId);
        if (targetCard == null || targetCard.getType() == null || !targetCard.getType().isPermanentType()) {
            return;
        }

        gameData.temporaryGraveyardCardAnimationsUntilEndOfTurn.put(targetCardId,
                new GraveyardCardAnimation(
                        animation.power(),
                        animation.toughness(),
                        animation.grantedColors(),
                        Set.copyOf(animation.grantedSubtypes()),
                        animation.grantedCardTypes(),
                        gameData.graveyardEntryVersion(targetCardId)));
    }
}

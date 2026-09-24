package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnTargetCardThenTargetPlayerReturnsCardFromGraveyardToHandEffect;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ReturnTargetCardThenTargetPlayerReturnsCardFromGraveyardToHandEffectHandler
        implements NormalEffectHandlerBean {

    private final ReturnCardFromGraveyardEffectHandler returnCardFromGraveyardEffectHandler;
    private final TargetPlayerReturnsCardFromGraveyardToHandEffectHandler
            targetPlayerReturnsCardFromGraveyardToHandEffectHandler;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ReturnTargetCardThenTargetPlayerReturnsCardFromGraveyardToHandEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var orderedEffect = (ReturnTargetCardThenTargetPlayerReturnsCardFromGraveyardToHandEffect) effect;
        Integer savedTargetGroup = entry.getResolvingEffectTargetGroup();
        try {
            entry.setResolvingEffectTargetGroup(entry.getTargetingCard()
                    .getEffectTargetIndex(orderedEffect.targetCardReturn()));
            returnCardFromGraveyardEffectHandler.resolve(gameData, entry, orderedEffect.targetCardReturn());
            if (gameData.interaction.isAwaitingInput()) {
                return;
            }
            entry.setResolvingEffectTargetGroup(entry.getTargetingCard()
                    .getEffectTargetIndex(orderedEffect.targetPlayerReturn()));
            targetPlayerReturnsCardFromGraveyardToHandEffectHandler.resolve(
                    gameData, entry, orderedEffect.targetPlayerReturn());
        } finally {
            entry.setResolvingEffectTargetGroup(savedTargetGroup);
        }
    }
}

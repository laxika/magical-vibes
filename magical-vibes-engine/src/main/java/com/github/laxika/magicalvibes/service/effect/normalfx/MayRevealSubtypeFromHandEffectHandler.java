package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.MayRevealSubtypeFromHandEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class MayRevealSubtypeFromHandEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final MayEffectHandler mayEffectHandler;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return MayRevealSubtypeFromHandEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        MayRevealSubtypeFromHandEffect reveal = (MayRevealSubtypeFromHandEffect) effect;
        List<Card> hand = gameData.playerHands.get(entry.getControllerId());
        if (hand == null || hand.stream().noneMatch(card -> gameQueryService.cardHasSubtype(
                card, reveal.subtype(), gameData, entry.getControllerId()))) {
            return;
        }
        MayEffect may = new MayEffect(reveal.thenEffect(), reveal.prompt());
        int effectIndex = entry.getEffectsToResolve().indexOf(effect);
        if (effectIndex < 0) {
            throw new IllegalStateException("Reveal effect is not on its stack entry");
        }
        entry.replaceEffectToResolve(effectIndex, may);
        mayEffectHandler.resolve(gameData, entry, may);
    }
}

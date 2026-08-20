package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.MayRevealSubtypeFromHandEffect;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class MayRevealSubtypeFromHandEffectHandler implements NormalEffectHandlerBean {

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return MayRevealSubtypeFromHandEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        MayRevealSubtypeFromHandEffect reveal = (MayRevealSubtypeFromHandEffect) effect;
        List<Card> hand = gameData.playerHands.get(entry.getControllerId());
        if (hand == null || hand.stream().noneMatch(card -> card.getSubtypes().contains(reveal.subtype()))) {
            return;
        }
        gameData.queueMayAbility(entry.getCard(), entry.getControllerId(),
                new MayEffect(reveal.thenEffect(), reveal.prompt()),
                entry.getTargetId(), entry.getSourcePermanentId());
    }
}

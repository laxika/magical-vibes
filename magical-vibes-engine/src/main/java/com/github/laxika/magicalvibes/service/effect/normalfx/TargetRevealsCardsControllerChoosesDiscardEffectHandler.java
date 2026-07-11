package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.TargetRevealsCardsControllerChoosesDiscardEffect;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * "Target player reveals N cards from their hand and you choose one of them. That player discards
 * that card." (Blackmail). Delegates the two-stage reveal/discard flow to
 * {@link PlayerInteractionSupport#beginRevealCardsChooseDiscard}.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TargetRevealsCardsControllerChoosesDiscardEffectHandler implements NormalEffectHandlerBean {

    private final PlayerInteractionSupport playerInteractionSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return TargetRevealsCardsControllerChoosesDiscardEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (TargetRevealsCardsControllerChoosesDiscardEffect) effect;
        playerInteractionSupport.beginRevealCardsChooseDiscard(gameData, entry, e.revealCount(), e.discardCount());
    }
}

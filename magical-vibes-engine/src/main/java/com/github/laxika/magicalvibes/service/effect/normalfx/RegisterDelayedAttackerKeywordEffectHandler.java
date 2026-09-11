package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.action.DelayedAttackerKeywordGrant;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterDelayedAttackerKeywordEffect;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/** Resolves a delayed attack-trigger keyword grant for the rest of the turn. */
@Slf4j
@Component
@RequiredArgsConstructor
public class RegisterDelayedAttackerKeywordEffectHandler implements NormalEffectHandlerBean {

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RegisterDelayedAttackerKeywordEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (RegisterDelayedAttackerKeywordEffect) effect;
        gameData.queueDelayedAction(new DelayedAttackerKeywordGrant(
                entry.getControllerId(), e.keywords(), entry.getCard()));
        log.info("Game {} - {} registers delayed attacker keyword grant {} until end of turn",
                gameData.id, entry.getCard().getName(), e.keywords());
    }
}

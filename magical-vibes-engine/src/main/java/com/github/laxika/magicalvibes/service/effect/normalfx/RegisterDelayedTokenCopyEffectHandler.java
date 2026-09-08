package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.action.DelayedCreateTokenCopy;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterDelayedTokenCopyEffect;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
public class RegisterDelayedTokenCopyEffectHandler implements NormalEffectHandlerBean {

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RegisterDelayedTokenCopyEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var register = (RegisterDelayedTokenCopyEffect) effect;
        Card copiedCard = register.copiedCard() != null ? register.copiedCard() : entry.getCard();
        if (copiedCard == null) {
            log.info("Game {} - Cannot register delayed token copy without a copied card", gameData.id);
            return;
        }
        UUID controllerId = entry.getControllerId();
        gameData.queueDelayedAction(new DelayedCreateTokenCopy(controllerId, entry.getCard(), copiedCard));
        log.info("Game {} - Registers delayed token copy of {} at next end step",
                gameData.id, copiedCard.getName());
    }
}

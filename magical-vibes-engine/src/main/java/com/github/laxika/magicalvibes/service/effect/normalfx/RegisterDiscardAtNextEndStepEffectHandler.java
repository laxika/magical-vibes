package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterDiscardAtNextEndStepEffect;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import com.github.laxika.magicalvibes.model.TemporaryGlobalTriggeredAbility;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@Slf4j
@Component
public class RegisterDiscardAtNextEndStepEffectHandler implements NormalEffectHandlerBean {

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RegisterDiscardAtNextEndStepEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (RegisterDiscardAtNextEndStepEffect) effect;
        gameData.temporaryGlobalTriggeredAbilities.add(TemporaryGlobalTriggeredAbility.atNextStep(
                Triggers.endStep(ActivePlayer.ANY), entry.getControllerId(), entry.getCard(),
                entry.getSourcePermanentId(), null, new DiscardEffect(e.count(), DiscardRecipient.CONTROLLER)));
        log.info("Game {} - {} registers delayed discard of {} card(s) at next end step",
                gameData.id, entry.getCard().getName(), e.count());
    }
}

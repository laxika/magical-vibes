package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterDelayedTokenCopyEffect;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import com.github.laxika.magicalvibes.model.TemporaryGlobalTriggeredAbility;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfCardEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@Slf4j
@Component
public class RegisterDelayedTokenCopyEffectHandler implements NormalEffectHandlerBean {

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RegisterDelayedTokenCopyEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        // "That artifact": the sacrificed permanent as it last existed on the battlefield (CR 603.10a).
        Card copiedCard = entry.getTriggeringPermanentSnapshot() != null
                ? entry.getTriggeringPermanentSnapshot().getCard() : entry.getCard();
        if (copiedCard == null) {
            log.info("Game {} - Cannot register delayed token copy without a copied card", gameData.id);
            return;
        }
        gameData.temporaryGlobalTriggeredAbilities.add(TemporaryGlobalTriggeredAbility.atNextStep(
                Triggers.endStep(ActivePlayer.ANY), entry.getControllerId(), entry.getCard(),
                entry.getSourcePermanentId(), null,
                new CreateTokenCopyOfCardEffect(copiedCard, new CreateTokenCopyOfTargetPermanentEffect())));
        log.info("Game {} - Registers delayed token copy of {} at next end step",
                gameData.id, copiedCard.getName());
    }
}

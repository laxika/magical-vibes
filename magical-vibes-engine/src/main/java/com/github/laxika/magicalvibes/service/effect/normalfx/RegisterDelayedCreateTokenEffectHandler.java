package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterDelayedCreateTokenEffect;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;
import com.github.laxika.magicalvibes.model.TemporaryGlobalTriggeredAbility;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@Slf4j
@Component
@RequiredArgsConstructor
public class RegisterDelayedCreateTokenEffectHandler implements NormalEffectHandlerBean {

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RegisterDelayedCreateTokenEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (RegisterDelayedCreateTokenEffect) effect;
        UUID controllerId = entry.getControllerId();
        gameData.temporaryGlobalTriggeredAbilities.add(TemporaryGlobalTriggeredAbility.atNextStep(
                Triggers.endStep(ActivePlayer.ANY), controllerId, entry.getCard(), entry.getSourcePermanentId(), null,
                e.tokenEffect()));

        String playerName = gameData.playerIdToName.get(controllerId);
        log.info("Game {} - {} registers delayed token creation at next end step",
                gameData.id, playerName);
    }
}

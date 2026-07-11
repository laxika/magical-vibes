package com.github.laxika.magicalvibes.service.effect.normalfx;
import com.github.laxika.magicalvibes.model.action.DelayedCombatDamageLoot;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterDelayedCombatDamageLootEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RegisterDelayedCombatDamageLootEffectHandler implements NormalEffectHandlerBean {

    private final GameBroadcastService gameBroadcastService;
    private final PlayerInteractionSupport playerInteractionSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RegisterDelayedCombatDamageLootEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (RegisterDelayedCombatDamageLootEffect) effect;

        UUID controllerId = entry.getControllerId();
        gameData.queueDelayedAction(
                new DelayedCombatDamageLoot(controllerId, e.drawAmount(), e.discardAmount(), entry.getCard()));
        String playerName = gameData.playerIdToName.get(controllerId);
        log.info("Game {} - {} registers delayed combat damage loot trigger (draw {}, discard {})",
                gameData.id, playerName, e.drawAmount(), e.discardAmount());
    
    }
}

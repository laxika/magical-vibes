package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.action.DelayedVehicleAttack;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterDelayedVehicleAttackEffect;
import org.springframework.stereotype.Component;

@Component
public class RegisterDelayedVehicleAttackEffectHandler implements NormalEffectHandlerBean {

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RegisterDelayedVehicleAttackEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        RegisterDelayedVehicleAttackEffect registration = (RegisterDelayedVehicleAttackEffect) effect;
        if (entry.getSourcePermanentId() == null || entry.getTriggeringPermanentId() == null) {
            return;
        }
        if (gameData.hasDelayedAction(DelayedVehicleAttack.class, action ->
                action.sourcePermanentId().equals(entry.getSourcePermanentId())
                        && action.vehicleId().equals(entry.getTriggeringPermanentId()))) {
            return;
        }
        gameData.queueDelayedAction(new DelayedVehicleAttack(
                entry.getControllerId(),
                entry.getSourcePermanentId(),
                entry.getTriggeringPermanentId(),
                entry.getCard(),
                registration.effect()));
    }
}

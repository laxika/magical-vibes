package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayLandDropEffect;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class MayPayLandDropEffectHandler implements NormalEffectHandlerBean {

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return MayPayLandDropEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        MayPayLandDropEffect mayPay = (MayPayLandDropEffect) effect;
        gameData.resolvingMayEffectFromStack = true;
        gameData.pendingMayAbilities.addFirst(new PendingMayAbility(
                entry.getCard(),
                entry.getControllerId(),
                List.of(mayPay),
                entry.getCard().getName() + " - " + mayPay.prompt(),
                entry.getTargetId(),
                null,
                entry.getSourcePermanentId(),
                null,
                0,
                0,
                entry.getAttackedTargetId(),
                entry.getActivePlayerId(),
                null,
                entry.getSourcePermanentSnapshot(),
                entry.getControllerId(),
                entry.getTriggeringCardId(),
                entry.getEventValue()));
    }
}

package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayLifeEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayPayer;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class MayPayLifeEffectResolutionHandler implements NormalEffectHandlerBean {

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return MayPayLifeEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        MayPayLifeEffect mayPayLife = (MayPayLifeEffect) effect;
        UUID payer = mayPayLife.payer() == MayPayPayer.TRIGGERING_PLAYER
                ? entry.getTargetId() : entry.getControllerId();
        if (payer == null) {
            return;
        }
        gameData.pendingMayAbilities.addFirst(new PendingMayAbility(
                entry.getCard(),
                payer,
                List.of(mayPayLife),
                entry.getCard().getName() + " - " + mayPayLife.prompt(),
                entry.getTargetId(),
                null,
                entry.getSourcePermanentId()));
    }
}

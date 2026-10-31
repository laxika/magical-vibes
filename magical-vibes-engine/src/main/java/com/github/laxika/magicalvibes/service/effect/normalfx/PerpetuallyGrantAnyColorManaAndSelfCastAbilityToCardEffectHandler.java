package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PerpetuallyGrantAnyColorManaAndSelfCastAbilityToCardEffect;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class PerpetuallyGrantAnyColorManaAndSelfCastAbilityToCardEffectHandler
        implements NormalEffectHandlerBean {

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PerpetuallyGrantAnyColorManaAndSelfCastAbilityToCardEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var grant = (PerpetuallyGrantAnyColorManaAndSelfCastAbilityToCardEffect) effect;
        UUID cardId = grant.cardId();
        if (cardId == null) {
            return;
        }

        gameData.perpetualAnyColorManaForCastCardIds.add(cardId);
        gameData.grantPerpetualAbilityOnce(cardId, EffectSlot.ON_SELF_CAST, grant.selfCastAbility());
    }
}

package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PerpetuallyGrantAnyColorManaAndSelfCastAbilityToCardEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;
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
        if (entry.getChosenObjectCard() == null) {
            return;
        }
        UUID cardId = entry.getChosenObjectCard().getId();

        gameData.perpetualAnyColorManaForCastCardIds.add(cardId);
        gameData.grantPerpetualAbilityOnce(cardId, new TriggeredAbility(Triggers.youCastThisSpell(), grant.selfCastAbility()));
    }
}

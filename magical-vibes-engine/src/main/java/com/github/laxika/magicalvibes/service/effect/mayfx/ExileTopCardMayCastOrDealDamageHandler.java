package com.github.laxika.magicalvibes.service.effect.mayfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardMayCastOrDealDamageEffect;
import com.github.laxika.magicalvibes.service.effect.normalfx.ChandraTorchExileCastSupport;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ExileTopCardMayCastOrDealDamageHandler implements MayEffectHandlerBean {

    private final ChandraTorchExileCastSupport support;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileTopCardMayCastOrDealDamageEffect.class;
    }

    @Override
    public void handle(GameData gameData, Player player, boolean accepted, PendingMayAbility ability) {
        ExileTopCardMayCastOrDealDamageEffect effect = ability.effects().stream()
                .filter(ExileTopCardMayCastOrDealDamageEffect.class::isInstance)
                .map(ExileTopCardMayCastOrDealDamageEffect.class::cast)
                .findFirst()
                .orElseThrow();
        if (accepted) {
            support.beginCast(gameData, player, ability.sourceCard(), ability.targetCardId(), effect.damage());
        } else {
            support.dealDamage(gameData, ability.controllerId(), ability.sourceCard(), effect.damage());
        }
    }
}

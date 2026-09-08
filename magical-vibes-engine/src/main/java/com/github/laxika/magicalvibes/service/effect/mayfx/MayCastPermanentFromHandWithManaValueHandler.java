package com.github.laxika.magicalvibes.service.effect.mayfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.MayCastPermanentFromHandWithManaValueEffect;
import com.github.laxika.magicalvibes.service.input.MayCastHandlerService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MayCastPermanentFromHandWithManaValueHandler implements MayEffectHandlerBean {

    private final MayCastHandlerService mayCastHandlerService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return MayCastPermanentFromHandWithManaValueEffect.class;
    }

    @Override
    public void handle(GameData gameData, Player player, boolean accepted, PendingMayAbility ability) {
        boolean scryIfDeclined = ability.effects().stream()
                .filter(MayCastPermanentFromHandWithManaValueEffect.class::isInstance)
                .map(MayCastPermanentFromHandWithManaValueEffect.class::cast)
                .anyMatch(MayCastPermanentFromHandWithManaValueEffect::scryIfDeclined);
        mayCastHandlerService.handleMayCastFromHandWithoutPaying(
                gameData, player, accepted, ability,
                MayCastPermanentFromHandWithManaValueEffect.class, false, scryIfDeclined);
    }
}

package com.github.laxika.magicalvibes.service.effect.mayfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EachOpponentSacrificesPermanentUnlessPaysEffect;
import com.github.laxika.magicalvibes.service.effect.normalfx.EachOpponentSacrificesPermanentUnlessPaysEffectHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EachOpponentSacrificesPermanentUnlessPaysHandler implements MayEffectHandlerBean {

    private final EachOpponentSacrificesPermanentUnlessPaysEffectHandler effectHandler;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return EachOpponentSacrificesPermanentUnlessPaysEffect.class;
    }

    @Override
    public void handle(GameData gameData, Player player, boolean accepted, PendingMayAbility ability) {
        EachOpponentSacrificesPermanentUnlessPaysEffect effect = ability.effects().stream()
                .filter(EachOpponentSacrificesPermanentUnlessPaysEffect.class::isInstance)
                .map(EachOpponentSacrificesPermanentUnlessPaysEffect.class::cast)
                .findFirst()
                .orElseThrow();
        effectHandler.handleChoice(gameData, player, accepted, ability, effect);
    }
}

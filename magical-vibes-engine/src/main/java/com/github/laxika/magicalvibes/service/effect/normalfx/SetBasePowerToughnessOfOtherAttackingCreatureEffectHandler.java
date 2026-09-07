package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.SetBasePowerToughnessEffect;
import com.github.laxika.magicalvibes.model.effect.SetBasePowerToughnessOfOtherAttackingCreatureEffect;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SetBasePowerToughnessOfOtherAttackingCreatureEffectHandler implements NormalEffectHandlerBean {

    private final SetBasePowerToughnessEffectHandler setBasePowerToughnessEffectHandler;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return SetBasePowerToughnessOfOtherAttackingCreatureEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (SetBasePowerToughnessOfOtherAttackingCreatureEffect) effect;
        setBasePowerToughnessEffectHandler.resolve(gameData, entry,
                new SetBasePowerToughnessEffect(e.power(), e.toughness()));
    }
}

package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;
import com.github.laxika.magicalvibes.model.effect.ScryIfPlayerDealtDamageThisWayEffect;
import java.util.List;
import org.springframework.stereotype.Component;

/** Resolves the conditional scry rider for damage that reached a player earlier in the entry. */
@Component
public class ScryIfPlayerDealtDamageThisWayEffectHandler implements NormalEffectHandlerBean {

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ScryIfPlayerDealtDamageThisWayEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        if (!entry.getPlayersDealtDamageThisResolution().isEmpty()) {
            int effectIndex = entry.getEffectsToResolve().indexOf(effect);
            entry.insertEffectsToResolve(effectIndex + 1, List.of(new ScryEffect(1)));
        }
    }
}

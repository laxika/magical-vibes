package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PreventNextColorDamageToControllerEffect;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;

@Component
public class PreventNextColorDamageToControllerEffectHandler implements NormalEffectHandlerBean {

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PreventNextColorDamageToControllerEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (PreventNextColorDamageToControllerEffect) effect;
        CardColor chosenColor = e.chosenColor();
        if (chosenColor == null) return;

        UUID controllerId = entry.getControllerId();
        gameData.playerColorDamagePreventionCount
                .computeIfAbsent(controllerId, k -> new ConcurrentHashMap<>())
                .merge(chosenColor, 1, Integer::sum);
    }
}

package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.AdditionalDamageToPlayersAndBattlesEffect;
import com.github.laxika.magicalvibes.model.effect.AdditionalDamageToPlayersAndBattlesUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.layer.FloatingContinuousEffect;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class AdditionalDamageToPlayersAndBattlesUntilEndOfTurnEffectHandler
        implements NormalEffectHandlerBean {

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return AdditionalDamageToPlayersAndBattlesUntilEndOfTurnEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var bonus = (AdditionalDamageToPlayersAndBattlesUntilEndOfTurnEffect) effect;
        gameData.addFloatingEffect(new FloatingContinuousEffect(
                UUID.randomUUID(),
                entry.getCard().getName(),
                null,
                entry.getControllerId(),
                new AdditionalDamageToPlayersAndBattlesEffect(bonus.amount()),
                null,
                null,
                null,
                EffectDuration.UNTIL_END_OF_TURN,
                0));
    }
}

package com.github.laxika.magicalvibes.service.effect.mayfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.WormsOfTheEarthEffect;
import com.github.laxika.magicalvibes.service.effect.normalfx.WormsOfTheEarthEffectHandler;
import com.github.laxika.magicalvibes.service.input.InputCompletionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** Handles Worms of the Earth's optional sacrifice and damage choices. */
@Component
@RequiredArgsConstructor
public class WormsOfTheEarthHandler implements MayEffectHandlerBean {

    private final WormsOfTheEarthEffectHandler effectHandler;
    private final InputCompletionService inputCompletionService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return WormsOfTheEarthEffect.class;
    }

    @Override
    public void handle(GameData gameData, Player player, boolean accepted, PendingMayAbility ability) {
        WormsOfTheEarthEffect effect = (WormsOfTheEarthEffect) ability.effects().getFirst();
        UUID playerId = ability.controllerId();

        if (effect.damageChoice()) {
            if (accepted) {
                effectHandler.dealDamageAndDestroy(gameData, ability.sourceCard(), effect, playerId);
            } else {
                effectHandler.advance(gameData, ability.sourceCard(), effect, playerId);
            }
            inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
            return;
        }

        if (accepted) {
            List<UUID> lands = effectHandler.landIds(gameData, playerId);
            if (lands.size() >= 2) {
                if (lands.size() > 2) {
                    effectHandler.beginLandChoice(gameData, ability.sourceCard(), effect, playerId, lands);
                } else {
                    effectHandler.sacrificeAndDestroy(
                            gameData, ability.sourceCard(), effect, lands, playerId);
                    inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
                }
                return;
            }
        }

        if (!accepted || effectHandler.landIds(gameData, playerId).size() < 2) {
            effectHandler.promptNext(gameData, ability.sourceCard(), new WormsOfTheEarthEffect(
                    List.of(playerId), effect.abilityControllerId(), effect.sourcePermanentId(), true));
            inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
        }
    }
}

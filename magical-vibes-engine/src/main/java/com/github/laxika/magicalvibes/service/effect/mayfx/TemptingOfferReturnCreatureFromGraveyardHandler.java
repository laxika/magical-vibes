package com.github.laxika.magicalvibes.service.effect.mayfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.TemptingOfferReturnCreatureFromGraveyardEffect;
import com.github.laxika.magicalvibes.service.effect.normalfx.TemptingOfferReturnCreatureFromGraveyardEffectHandler;
import com.github.laxika.magicalvibes.service.input.InputCompletionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Handles one opponent's choice for Tempt with Immortality. */
@Component
@RequiredArgsConstructor
public class TemptingOfferReturnCreatureFromGraveyardHandler implements MayEffectHandlerBean {

    private final TemptingOfferReturnCreatureFromGraveyardEffectHandler effectHandler;
    private final InputCompletionService inputCompletionService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return TemptingOfferReturnCreatureFromGraveyardEffect.class;
    }

    @Override
    public void handle(GameData gameData, Player player, boolean accepted, PendingMayAbility ability) {
        TemptingOfferReturnCreatureFromGraveyardEffect effect =
                (TemptingOfferReturnCreatureFromGraveyardEffect) ability.effects().getFirst();
        effectHandler.completeChoice(gameData, ability, effect, accepted);
        inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
    }
}

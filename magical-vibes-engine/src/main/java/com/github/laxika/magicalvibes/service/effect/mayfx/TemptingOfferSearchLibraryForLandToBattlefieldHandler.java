package com.github.laxika.magicalvibes.service.effect.mayfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.TemptingOfferSearchLibraryForLandToBattlefieldEffect;
import com.github.laxika.magicalvibes.service.effect.normalfx.TemptingOfferSearchLibraryForLandToBattlefieldEffectHandler;
import com.github.laxika.magicalvibes.service.input.InputCompletionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Handles one opponent's choice for Tempt with Discovery. */
@Component
@RequiredArgsConstructor
public class TemptingOfferSearchLibraryForLandToBattlefieldHandler implements MayEffectHandlerBean {

    private final TemptingOfferSearchLibraryForLandToBattlefieldEffectHandler effectHandler;
    private final InputCompletionService inputCompletionService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return TemptingOfferSearchLibraryForLandToBattlefieldEffect.class;
    }

    @Override
    public void handle(GameData gameData, Player player, boolean accepted, PendingMayAbility ability) {
        TemptingOfferSearchLibraryForLandToBattlefieldEffect effect =
                (TemptingOfferSearchLibraryForLandToBattlefieldEffect) ability.effects().getFirst();
        effectHandler.completeOpponentChoice(gameData, ability, effect, accepted);
        inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
    }
}

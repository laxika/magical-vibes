package com.github.laxika.magicalvibes.service.effect.mayfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.TemptingOfferCreateTokensEffect;
import com.github.laxika.magicalvibes.service.effect.normalfx.TemptingOfferCreateTokensEffectHandler;
import com.github.laxika.magicalvibes.service.input.InputCompletionService;
import org.springframework.stereotype.Component;

/** Handles one opponent's choice in a Tempting offer. */
@Component
public class TemptingOfferCreateTokensHandler implements MayEffectHandlerBean {

    private final TemptingOfferCreateTokensEffectHandler effectHandler;
    private final InputCompletionService inputCompletionService;

    public TemptingOfferCreateTokensHandler(TemptingOfferCreateTokensEffectHandler effectHandler,
                                            InputCompletionService inputCompletionService) {
        this.effectHandler = effectHandler;
        this.inputCompletionService = inputCompletionService;
    }

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return TemptingOfferCreateTokensEffect.class;
    }

    @Override
    public void handle(GameData gameData, Player player, boolean accepted, PendingMayAbility ability) {
        TemptingOfferCreateTokensEffect effect = ability.effects().stream()
                .filter(TemptingOfferCreateTokensEffect.class::isInstance)
                .map(TemptingOfferCreateTokensEffect.class::cast)
                .findFirst()
                .orElseThrow();
        effectHandler.completeChoice(gameData, ability, effect, accepted);
        inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
    }
}

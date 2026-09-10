package com.github.laxika.magicalvibes.service.effect.mayfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EachOpponentCreatesTokenUnlessSacrificesCreatureEffect;
import com.github.laxika.magicalvibes.service.effect.normalfx.EachOpponentCreatesTokenUnlessSacrificesCreatureEffectHandler;
import com.github.laxika.magicalvibes.service.input.InputCompletionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Handles the opponent's choice in Acererak's sacrifice-or-token ability. */
@Component
@RequiredArgsConstructor
public class EachOpponentCreatesTokenUnlessSacrificesCreatureHandler implements MayEffectHandlerBean {

    private final EachOpponentCreatesTokenUnlessSacrificesCreatureEffectHandler effectHandler;
    private final InputCompletionService inputCompletionService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return EachOpponentCreatesTokenUnlessSacrificesCreatureEffect.class;
    }

    @Override
    public void handle(GameData gameData, Player player, boolean accepted, PendingMayAbility ability) {
        EachOpponentCreatesTokenUnlessSacrificesCreatureEffect effect = ability.effects().stream()
                .filter(EachOpponentCreatesTokenUnlessSacrificesCreatureEffect.class::isInstance)
                .map(EachOpponentCreatesTokenUnlessSacrificesCreatureEffect.class::cast)
                .findFirst()
                .orElseThrow();
        effectHandler.resolveMayChoice(gameData, ability, accepted, effect);
        if (!gameData.interaction.isAwaitingInput()) {
            inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
        }
    }
}

package com.github.laxika.magicalvibes.service.effect.mayfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.effect.AnyOpponentMayDrawOrCreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.service.effect.normalfx.AnyOpponentMayDrawOrCreateTokenEffectHandler;
import com.github.laxika.magicalvibes.service.input.InputCompletionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Handles one opponent's choice for Development. */
@Component
@RequiredArgsConstructor
public class AnyOpponentMayDrawOrCreateTokenHandler implements MayEffectHandlerBean {

    private final AnyOpponentMayDrawOrCreateTokenEffectHandler effectHandler;
    private final InputCompletionService inputCompletionService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return AnyOpponentMayDrawOrCreateTokenEffect.class;
    }

    @Override
    public void handle(GameData gameData, Player player, boolean accepted, PendingMayAbility ability) {
        var effect = (AnyOpponentMayDrawOrCreateTokenEffect) ability.effects().getFirst();
        effectHandler.completeChoice(gameData, ability, effect, accepted);
        inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
    }
}

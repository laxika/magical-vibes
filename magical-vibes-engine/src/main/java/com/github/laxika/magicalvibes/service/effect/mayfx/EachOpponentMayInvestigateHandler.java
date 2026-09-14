package com.github.laxika.magicalvibes.service.effect.mayfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EachOpponentMayInvestigateEffect;
import com.github.laxika.magicalvibes.service.effect.normalfx.EachOpponentMayInvestigateEffectHandler;
import com.github.laxika.magicalvibes.service.input.InputCompletionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Handles one opponent's choice in Wernog's investigate ability. */
@Component
@RequiredArgsConstructor
public class EachOpponentMayInvestigateHandler implements MayEffectHandlerBean {

    private final EachOpponentMayInvestigateEffectHandler effectHandler;
    private final InputCompletionService inputCompletionService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return EachOpponentMayInvestigateEffect.class;
    }

    @Override
    public void handle(GameData gameData, Player player, boolean accepted, PendingMayAbility ability) {
        EachOpponentMayInvestigateEffect effect = ability.effects().stream()
                .filter(EachOpponentMayInvestigateEffect.class::isInstance)
                .map(EachOpponentMayInvestigateEffect.class::cast)
                .findFirst()
                .orElseThrow();
        effectHandler.completeChoice(gameData, ability, effect, accepted);
        inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
    }
}

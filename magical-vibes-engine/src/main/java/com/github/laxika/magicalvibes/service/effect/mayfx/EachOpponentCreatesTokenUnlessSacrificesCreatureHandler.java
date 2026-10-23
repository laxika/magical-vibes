package com.github.laxika.magicalvibes.service.effect.mayfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EachOpponentCreatesTokenUnlessSacrificesCreatureEffect;
import com.github.laxika.magicalvibes.service.effect.normalfx.EachOpponentCreatesTokenUnlessSacrificesCreatureEffectHandler;
import com.github.laxika.magicalvibes.service.input.InputCompletionService;
import com.github.laxika.magicalvibes.service.input.PermanentChoiceHandlerService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

/**
 * Handles the opponent's choice in Acererak's sacrifice-or-token ability. An accepted sacrifice
 * changes nothing before it (a lone creature is sacrificed at once, several open a choice), so a
 * creature with competing death replacements is asked about before it leaves (CR 616.1) and the
 * answer is completed again with that choice; a declined one only creates the token.
 */
@Component
@RequiredArgsConstructor
public class EachOpponentCreatesTokenUnlessSacrificesCreatureHandler implements MayEffectHandlerBean {

    private final EachOpponentCreatesTokenUnlessSacrificesCreatureEffectHandler effectHandler;
    private final InputCompletionService inputCompletionService;
    private final ObjectProvider<PermanentChoiceHandlerService> permanentChoiceHandlerServiceProvider;

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
        if (accepted) {
            permanentChoiceHandlerServiceProvider.getObject().answerAskingDeathReplacementsFirst(
                    gameData, player, ability, true, () -> resolve(gameData, ability, true, effect));
        } else {
            resolve(gameData, ability, false, effect);
        }
    }

    private void resolve(GameData gameData, PendingMayAbility ability, boolean accepted,
                         EachOpponentCreatesTokenUnlessSacrificesCreatureEffect effect) {
        effectHandler.resolveMayChoice(gameData, ability, accepted, effect);
        if (!gameData.interaction.isAwaitingInput()) {
            inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
        }
    }
}

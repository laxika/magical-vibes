package com.github.laxika.magicalvibes.service.effect.mayfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.effect.AnyPlayerMayPayLifeToCounterSpellEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.service.effect.normalfx.AnyPlayerMayPayLifeToCounterSpellEffectHandler;
import com.github.laxika.magicalvibes.service.input.InputCompletionService;
import com.github.laxika.magicalvibes.service.input.PermanentChoiceHandlerService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

/** Handles one player's Dash Hopes life-payment choice. */
@Component
@RequiredArgsConstructor
public class AnyPlayerMayPayLifeToCounterSpellHandler implements MayEffectHandlerBean {

    private final AnyPlayerMayPayLifeToCounterSpellEffectHandler effectHandler;
    private final InputCompletionService inputCompletionService;
    private final ObjectProvider<PermanentChoiceHandlerService> permanentChoiceHandlerServiceProvider;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return AnyPlayerMayPayLifeToCounterSpellEffect.class;
    }

    @Override
    public void handle(GameData gameData, Player player, boolean accepted, PendingMayAbility ability) {
        AnyPlayerMayPayLifeToCounterSpellEffect effect = ability.effects().stream()
                .filter(AnyPlayerMayPayLifeToCounterSpellEffect.class::isInstance)
                .map(AnyPlayerMayPayLifeToCounterSpellEffect.class::cast)
                .findFirst()
                .orElseThrow();

        if (accepted && effectHandler.canPayLife(gameData, ability.controllerId(), effect.lifeCost())) {
            // Paying life is losing it (CR 119.4): a Lich's Duel Mastery sacrifice it causes is asked about first.
            if (permanentChoiceHandlerServiceProvider.getObject().answerAskingDeathReplacementsFirst(
                    gameData, player, ability, true, () -> effectHandler.payLife(
                            gameData, ability.controllerId(), effect.lifeCost(), ability.sourceCard()))) {
                return;
            }
            effectHandler.counterSpell(gameData, ability.sourceCard(), effect);
        }

        effectHandler.advance(gameData, ability.sourceCard(), effect, ability.controllerId());
        inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
    }
}

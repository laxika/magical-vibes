package com.github.laxika.magicalvibes.service.effect.mayfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.effect.AnyPlayerMayDiscardCardsToCounterSpellEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.service.effect.normalfx.AnyPlayerMayDiscardCardsToCounterSpellEffectHandler;
import com.github.laxika.magicalvibes.service.input.InputCompletionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Handles one player's choice to discard cards to counter a triggering spell. */
@Component
@RequiredArgsConstructor
public class AnyPlayerMayDiscardCardsToCounterSpellHandler implements MayEffectHandlerBean {

    private final AnyPlayerMayDiscardCardsToCounterSpellEffectHandler effectHandler;
    private final InputCompletionService inputCompletionService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return AnyPlayerMayDiscardCardsToCounterSpellEffect.class;
    }

    @Override
    public void handle(GameData gameData, Player player, boolean accepted, PendingMayAbility ability) {
        AnyPlayerMayDiscardCardsToCounterSpellEffect effect =
                (AnyPlayerMayDiscardCardsToCounterSpellEffect) ability.effects().getFirst();
        if (accepted && effectHandler.canDiscard(gameData, ability.controllerId(), effect.cardsToDiscard())) {
            effectHandler.beginDiscard(gameData, ability.controllerId(), ability.sourceCard(), effect);
            return;
        }

        effectHandler.advance(gameData, ability.sourceCard(), effect, ability.controllerId());
        inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
    }
}

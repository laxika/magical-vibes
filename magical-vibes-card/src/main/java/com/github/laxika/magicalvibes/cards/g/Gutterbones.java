package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.condition.AllConditions;
import com.github.laxika.magicalvibes.model.condition.ControllerTurn;
import com.github.laxika.magicalvibes.model.condition.OpponentLostLifeThisTurn;
import com.github.laxika.magicalvibes.model.effect.EntersTappedEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.filter.CardIsSelfPredicate;

import java.util.List;

@CardRegistration(set = "RNA", collectorNumber = "76")
public class Gutterbones extends Card {

    public Gutterbones() {
        // This creature enters tapped.
        addEffect(EffectSlot.STATIC, new EntersTappedEffect());

        // {1}{B}: Return this card from your graveyard to your hand. Activate only during your turn
        // and only if an opponent lost life this turn.
        addGraveyardActivatedAbility(new ActivatedAbility(
                false,
                "{1}{B}",
                List.of(ReturnCardFromGraveyardEffect.builder()
                        .destination(GraveyardChoiceDestination.HAND)
                        .filter(new CardIsSelfPredicate())
                        .returnAll(true)
                        .build()),
                "{1}{B}: Return this card from your graveyard to your hand. Activate only during your turn and only if an opponent lost life this turn."
        ).withActivationCondition(
                new AllConditions(List.of(new ControllerTurn(), new OpponentLostLifeThisTurn(1))),
                "Activate only during your turn and only if an opponent lost life this turn"
        ));
    }
}

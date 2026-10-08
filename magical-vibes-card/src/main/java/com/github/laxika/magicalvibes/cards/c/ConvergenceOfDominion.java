package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.ControllerControlsCommander;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.MillRecipient;
import com.github.laxika.magicalvibes.model.effect.ReduceGraveyardCardActivatedAbilityCostEffect;
import com.github.laxika.magicalvibes.model.filter.CardTruePredicate;

import java.util.List;

@CardRegistration(set = "40K", collectorNumber = "154")
public class ConvergenceOfDominion extends Card {

    public ConvergenceOfDominion() {
        // As long as you control your commander, activated abilities of cards in your graveyard
        // cost {2} less to activate.
        addEffect(EffectSlot.STATIC, new ConditionalEffect(
                new ControllerControlsCommander(),
                new ReduceGraveyardCardActivatedAbilityCostEffect(new CardTruePredicate(), 2, 1)));

        // {3}, {T}: Mill three cards.
        addActivatedAbility(new ActivatedAbility(
                true,
                "{3}",
                List.of(new MillEffect(3, MillRecipient.CONTROLLER)),
                "{3}, {T}: Mill three cards."
        ));
    }
}

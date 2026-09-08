package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DiscardCardTypeCost;
import com.github.laxika.magicalvibes.model.effect.PreventDamageFromChosenSourceEffect;

import java.util.List;

@CardRegistration(set = "MMQ", collectorNumber = "8")
public class ChoArrimAlchemist extends Card {

    public ChoArrimAlchemist() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{1}{W}{W}",
                List.of(
                        new DiscardCardTypeCost(null, null),
                        PreventDamageFromChosenSourceEffect.nextDamageToYouAndGainLife()
                ),
                "{1}{W}{W}, {T}, Discard a card: The next time a source of your choice would deal damage to you this turn, prevent that damage. You gain life equal to the damage prevented this way."
        ));
    }
}

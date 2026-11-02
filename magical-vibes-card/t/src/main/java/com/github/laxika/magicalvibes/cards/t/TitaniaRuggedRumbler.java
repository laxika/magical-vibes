package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CounterUnlessDiscardsOrPaysEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardCardOrPayManaCost;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MSH", collectorNumber = "235")
public class TitaniaRuggedRumbler extends Card {

    public TitaniaRuggedRumbler() {
        addCastingCost(new DiscardCardOrPayManaCost("{2}"));
        addTrigger(Triggers.becomesTargetOfOpponentSpellOrAbility(),
                new CounterUnlessDiscardsOrPaysEffect(2));
    }
}

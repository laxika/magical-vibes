package com.github.laxika.magicalvibes.cards.r;

import java.util.List;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.ChooseBasicLandTypeOnEnterEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSourceChosenSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MIR", collectorNumber = "237")
public class RootsOfLife extends Card {

    public RootsOfLife() {
        // As this enchantment enters, choose Island or Swamp.
        addStaticEffect(new ChooseBasicLandTypeOnEnterEffect(List.of(CardSubtype.ISLAND, CardSubtype.SWAMP)));
        // Whenever a land of the chosen type an opponent controls becomes tapped, you gain 1 life.
        // Mandatory twin of Thoughtleech; fires on any tap (for mana or forced), not just mana taps.
        addTrigger(Triggers.permanentOpponentControlsBecomesTapped(), new TriggeringPermanentConditionalEffect(
                new PermanentHasSourceChosenSubtypePredicate(),
                new GainLifeEffect(1)));
    }
}

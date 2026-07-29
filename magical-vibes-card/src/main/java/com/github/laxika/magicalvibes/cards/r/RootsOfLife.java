package com.github.laxika.magicalvibes.cards.r;

import java.util.List;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChooseBasicLandTypeOnEnterEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSourceChosenSubtypePredicate;

@CardRegistration(set = "MIR", collectorNumber = "237")
public class RootsOfLife extends Card {

    public RootsOfLife() {
        // As this enchantment enters, choose Island or Swamp.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new ChooseBasicLandTypeOnEnterEffect(List.of(CardSubtype.ISLAND, CardSubtype.SWAMP)));
        // Whenever a land of the chosen type an opponent controls becomes tapped, you gain 1 life.
        // Mandatory twin of Thoughtleech; fires on any tap (for mana or forced), not just mana taps.
        addEffect(EffectSlot.ON_OPPONENT_PERMANENT_BECOMES_TAPPED, new TriggeringPermanentConditionalEffect(
                new PermanentHasSourceChosenSubtypePredicate(),
                new GainLifeEffect(1)));
    }
}

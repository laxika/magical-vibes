package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.DelveCost;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsEffect;
import com.github.laxika.magicalvibes.model.effect.LookDestination;

@CardRegistration(set = "KTK", collectorNumber = "36")
public class DigThroughTime extends Card {

    public DigThroughTime() {
        addEffect(EffectSlot.SPELL, new DelveCost());
        addEffect(EffectSlot.SPELL, new LookAtTopCardsEffect(
                new Fixed(7), new Fixed(2), null, LookDestination.BOTTOM_OF_LIBRARY, false));
    }
}

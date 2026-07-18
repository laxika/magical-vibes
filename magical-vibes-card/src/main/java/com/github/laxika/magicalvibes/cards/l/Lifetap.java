package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

@CardRegistration(set = "5ED", collectorNumber = "99")
@CardRegistration(set = "4ED", collectorNumber = "81")
public class Lifetap extends Card {

    public Lifetap() {
        // Whenever a Forest an opponent controls becomes tapped, you gain 1 life.
        // Fires on any tap of an opponent's Forest (for mana or forced), not just taps for mana.
        addEffect(EffectSlot.ON_OPPONENT_PERMANENT_BECOMES_TAPPED, new TriggeringPermanentConditionalEffect(
                new PermanentHasSubtypePredicate(CardSubtype.FOREST),
                new GainLifeEffect(1)));
    }
}

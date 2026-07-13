package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

@CardRegistration(set = "7ED", collectorNumber = "274")
public class Thoughtleech extends Card {

    public Thoughtleech() {
        // Whenever an Island an opponent controls becomes tapped, you may gain 1 life.
        // Opponent-scoped twin of Judge of Currents; fires on any tap of an opponent's Island
        // (for mana or forced, e.g. Icy Manipulator), not just taps for mana.
        addEffect(EffectSlot.ON_OPPONENT_PERMANENT_BECOMES_TAPPED, new TriggeringPermanentConditionalEffect(
                new PermanentHasSubtypePredicate(CardSubtype.ISLAND),
                new MayEffect(new GainLifeEffect(1), "Gain 1 life?")));
    }
}

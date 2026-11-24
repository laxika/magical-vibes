package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.ControlledNonlandPermanentsAreColorEffect;
import com.github.laxika.magicalvibes.model.effect.SpendWhiteManaAsAnyColorEffect;
import com.github.laxika.magicalvibes.model.effect.GrantSubtypeEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;

@CardRegistration(set = "MIR", collectorNumber = "6")
@CardRegistration(set = "6ED", collectorNumber = "7")
@CardRegistration(set = "TSB", collectorNumber = "3")
public class CelestialDawn extends Card {

    public CelestialDawn() {
        // Lands you control are Plains.
        addStaticEffect(new GrantSubtypeEffect(CardSubtype.PLAINS, Subjects.landsYouControl(), true));

        // Nonland permanents you control, spells you control, and nonland cards you own outside the
        // battlefield are white. The shared color query applies this effect outside the battlefield.
        addStaticEffect(new ControlledNonlandPermanentsAreColorEffect(CardColor.WHITE));

        // You may spend white mana as though it were mana of any color. You may spend other mana
        // only as though it were colorless mana.
        addStaticEffect(new SpendWhiteManaAsAnyColorEffect());
    }
}

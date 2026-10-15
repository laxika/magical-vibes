package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AlternateHandCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaCastingCost;
import com.github.laxika.magicalvibes.model.SacrificePermanentsCost;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

import java.util.List;

@CardRegistration(set = "BOK", collectorNumber = "19")
public class PatronOfTheKitsune extends Card {

    public PatronOfTheKitsune() {
        addCastingOption(AlternateHandCast.offering(List.of(
                new ManaCastingCost("{4}{W}{W}"),
                new SacrificePermanentsCost(1, new PermanentHasSubtypePredicate(CardSubtype.FOX))
        )));
        // Whenever a creature attacks, you may gain 1 life. Fires once per attacking creature,
        // regardless of who controls it or whom it attacks.
        addEffect(EffectSlot.ON_ANY_CREATURE_ATTACKS, new MayEffect(new GainLifeEffect(1), "Gain 1 life?"));
    }
}

package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CopyPermanentOnEnterEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.Set;

@CardRegistration(set = "PIP", collectorNumber = "40")
@CardRegistration(set = "PIP", collectorNumber = "330")
@CardRegistration(set = "PIP", collectorNumber = "568")
@CardRegistration(set = "PIP", collectorNumber = "858")
public class SynthInfiltrator extends Card {

    public SynthInfiltrator() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new CopyPermanentOnEnterEffect(
                new PermanentIsCreaturePredicate(), "creature",
                Set.of(CardType.ARTIFACT), Set.of(CardSubtype.SYNTH)
        ));
    }
}

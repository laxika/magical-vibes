package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.EachPlayerChoosesLandsThenSacrificeRestEffect;
import com.github.laxika.magicalvibes.model.effect.MassDamageEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;

@CardRegistration(set = "EOC", collectorNumber = "12")
@CardRegistration(set = "EOC", collectorNumber = "32")
public class PlanetaryAnnihilation extends Card {

    public PlanetaryAnnihilation() {
        addEffect(EffectSlot.SPELL, SequenceEffect.of(
                new EachPlayerChoosesLandsThenSacrificeRestEffect(6),
                new MassDamageEffect(6)
        ));
    }
}

package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.EachPlayerDiscardsFractionOfHandRoundedDownEffect;
import com.github.laxika.magicalvibes.model.effect.EachPlayerLosesFractionOfLifeRoundedDownEffect;
import com.github.laxika.magicalvibes.model.effect.EachPlayerSacrificesFractionRoundedDownEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentTruePredicate;

@CardRegistration(set = "SOS", collectorNumber = "94")
public class PoxPlague extends Card {

    public PoxPlague() {
        addEffect(EffectSlot.SPELL, new EachPlayerLosesFractionOfLifeRoundedDownEffect(2));
        addEffect(EffectSlot.SPELL, new EachPlayerDiscardsFractionOfHandRoundedDownEffect(2));
        addEffect(EffectSlot.SPELL,
                new EachPlayerSacrificesFractionRoundedDownEffect(2, new PermanentTruePredicate()));
    }
}

package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourceCardPredicate;

import java.util.List;

@CardRegistration(set = "TLA", collectorNumber = "13")
public class CompassionateHealer extends Card {

    public CompassionateHealer() {
        // Whenever this creature becomes tapped, you gain 1 life and scry 1.
        addEffect(EffectSlot.ON_ALLY_PERMANENT_BECOMES_TAPPED,
                new TriggeringPermanentConditionalEffect(
                        new PermanentIsSourceCardPredicate(),
                        new SequenceEffect(List.of(
                                new GainLifeEffect(1),
                                new ScryEffect(1)
                        ))));
    }
}

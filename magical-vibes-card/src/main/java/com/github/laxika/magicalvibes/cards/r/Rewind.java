package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CounterSpellEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;

@CardRegistration(set = "9ED", collectorNumber = "94")
@CardRegistration(set = "8ED", collectorNumber = "96")
@CardRegistration(set = "M13", collectorNumber = "65")
public class Rewind extends Card {

    public Rewind() {
        addEffect(EffectSlot.SPELL, new CounterSpellEffect());
        addEffect(EffectSlot.SPELL,
                new UntapPermanentsEffect(TapUntapScope.CONTROLLED, new PermanentIsLandPredicate(), 4));
    }
}

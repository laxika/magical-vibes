package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.EachPlayerMayShuffleZonesIntoLibraryAndDrawEffect;
import com.github.laxika.magicalvibes.model.effect.ExileSpellEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

@CardRegistration(set = "TMT", collectorNumber = "55")
@CardRegistration(set = "TMT", collectorNumber = "263")
@CardRegistration(set = "TMT", collectorNumber = "284")
@CardRegistration(set = "TMT", collectorNumber = "294")
public class TurtlesInTime extends Card {

    public TurtlesInTime() {
        addEffect(EffectSlot.SPELL, ReturnToHandEffect.allPermanentsMatching(new PermanentIsCreaturePredicate()));
        addEffect(EffectSlot.SPELL, new EachPlayerMayShuffleZonesIntoLibraryAndDrawEffect(7));
        addEffect(EffectSlot.SPELL, new ExileSpellEffect());
    }
}

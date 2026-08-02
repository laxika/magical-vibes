package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DiscardHandEffect;
import com.github.laxika.magicalvibes.model.effect.ExileAllPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentTruePredicate;

@CardRegistration(set = "TMP", collectorNumber = "162")
public class Apocalypse extends Card {

    public Apocalypse() {
        // Exile all permanents. You discard your hand.
        addEffect(EffectSlot.SPELL, new ExileAllPermanentsEffect(new PermanentTruePredicate()));
        addEffect(EffectSlot.SPELL, new DiscardHandEffect());
    }
}

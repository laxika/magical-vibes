package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DiscardHandEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentTruePredicate;

@CardRegistration(set = "TMP", collectorNumber = "162")
public class Apocalypse extends Card {

    public Apocalypse() {
        // Exile all permanents. You discard your hand.
        addSpellEffect(ExileTargetPermanentEffect.all(new PermanentTruePredicate()));
        addSpellEffect(new DiscardHandEffect());
    }
}

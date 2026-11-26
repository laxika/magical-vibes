package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ReverseMiracleCast;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

@CardRegistration(set = "MB1", collectorNumber = "47")
public class Sunimret extends Card {

    public Sunimret() {
        addCastingOption(new ReverseMiracleCast("{B}"));
        addSpellEffect(ExileTargetPermanentEffect.all(new PermanentIsCreaturePredicate()));
    }
}

package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ExileAllPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

@CardRegistration(set = "BOK", collectorNumber = "4")
public class FinalJudgment extends Card {

    public FinalJudgment() {
        // Exile all creatures.
        addEffect(EffectSlot.SPELL, new ExileAllPermanentsEffect(new PermanentIsCreaturePredicate()));
    }
}

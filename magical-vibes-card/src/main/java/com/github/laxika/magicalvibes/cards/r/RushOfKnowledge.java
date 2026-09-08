package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.GreatestManaValueAmongControlled;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentTruePredicate;

@CardRegistration(set = "SCG", collectorNumber = "49")
public class RushOfKnowledge extends Card {

    public RushOfKnowledge() {
        addEffect(EffectSlot.SPELL, new DrawCardEffect(
                new GreatestManaValueAmongControlled(new PermanentTruePredicate())));
    }
}

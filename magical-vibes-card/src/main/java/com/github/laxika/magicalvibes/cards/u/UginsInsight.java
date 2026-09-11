package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.GreatestManaValueAmongControlled;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentTruePredicate;

@CardRegistration(set = "BFZ", collectorNumber = "87")
public class UginsInsight extends Card {

    public UginsInsight() {
        addEffect(EffectSlot.SPELL, new ScryEffect(
                new GreatestManaValueAmongControlled(new PermanentTruePredicate())));
        addEffect(EffectSlot.SPELL, new DrawCardEffect(3));
    }
}

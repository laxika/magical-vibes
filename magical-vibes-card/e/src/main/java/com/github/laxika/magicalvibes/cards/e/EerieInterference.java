package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PreventDamageEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

@CardRegistration(set = "WOE", collectorNumber = "12")
public class EerieInterference extends Card {

    public EerieInterference() {
        addEffect(EffectSlot.SPELL, PreventDamageEffect.allToControllerAndCreaturesFromMatchingSources(
                new PermanentIsCreaturePredicate()));
    }
}

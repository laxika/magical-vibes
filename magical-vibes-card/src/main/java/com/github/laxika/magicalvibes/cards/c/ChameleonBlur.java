package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PreventDamageEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

@CardRegistration(set = "TSP", collectorNumber = "192")
public class ChameleonBlur extends Card {

    public ChameleonBlur() {
        addEffect(EffectSlot.SPELL, PreventDamageEffect.allToPlayersFromMatchingSources(
                new PermanentIsCreaturePredicate()));
    }
}

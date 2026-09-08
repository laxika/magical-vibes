package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.BoostAllOwnCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

@CardRegistration(set = "GRN", collectorNumber = "157")
public class Camaraderie extends Card {

    public Camaraderie() {
        PermanentCount creatures = new PermanentCount(new PermanentIsCreaturePredicate(), CountScope.CONTROLLER);

        // You gain X life and draw X cards, where X is the number of creatures you control.
        addEffect(EffectSlot.SPELL, new GainLifeEffect(creatures));
        addEffect(EffectSlot.SPELL, new DrawCardEffect(creatures));

        // Creatures you control get +1/+1 until end of turn.
        addEffect(EffectSlot.SPELL, new BoostAllOwnCreaturesEffect(1, 1));
    }
}

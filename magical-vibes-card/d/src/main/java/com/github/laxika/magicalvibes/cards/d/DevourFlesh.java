package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.AmountThisWay;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

@CardRegistration(set = "GTC", collectorNumber = "63")
@CardRegistration(set = "PIO", collectorNumber = "90")
public class DevourFlesh extends Card {

    public DevourFlesh() {
        addSpellEffect(new SacrificePermanentsEffect(1, new PermanentIsCreaturePredicate(), Subjects.targetPlayer()));
        addSpellEffect(new GainLifeEffect(AmountThisWay.sacrificedToughness(), Subjects.targetPlayerDeclaredElsewhere()));
    }
}

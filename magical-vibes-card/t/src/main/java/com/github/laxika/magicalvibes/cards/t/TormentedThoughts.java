package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeCreatureCost;
import com.github.laxika.magicalvibes.model.effect.Subjects;

@CardRegistration(set = "ORI", collectorNumber = "122")
@CardRegistration(set = "JOU", collectorNumber = "86")
public class TormentedThoughts extends Card {

    public TormentedThoughts() {
        addCastingCost(new SacrificeCreatureCost(false, true));
        addSpellEffect(new DiscardEffect(new XValue(), Subjects.targetPlayer()));
    }
}

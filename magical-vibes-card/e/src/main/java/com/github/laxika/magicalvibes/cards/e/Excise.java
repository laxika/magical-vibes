package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.model.effect.UnlessPaysEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.PayManaCost;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "PCY", collectorNumber = "8")
public class Excise extends Card {

    public Excise() {
        target(TargetFilters.attackingCreature())
                .addSpellEffect(UnlessPaysEffect.unlessPays(Subjects.controllerOf(Subjects.target()),
                        PayManaCost.x(), new ExileTargetPermanentEffect()));
    }
}

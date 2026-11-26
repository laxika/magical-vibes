package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.model.effect.UnlessPaysEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "PCY", collectorNumber = "54")
public class Withdraw extends Card {

    public Withdraw() {
        target(TargetFilters.creature())
                .addSpellEffect(ReturnToHandEffect.target());
        target(TargetFilters.creature())
                .addSpellEffect(UnlessPaysEffect.unlessPays(Subjects.controllerOf(Subjects.target()), "{1}",
                        ReturnToHandEffect.target()));
    }
}

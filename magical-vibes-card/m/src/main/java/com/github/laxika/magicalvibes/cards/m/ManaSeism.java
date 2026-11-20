package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.amount.AmountThisWay;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;

/**
 * Mana Seism — {1}{R} sorcery: "Sacrifice any number of lands, then add that much {C}."
 */
@CardRegistration(set = "CHK", collectorNumber = "179")
public class ManaSeism extends Card {

    public ManaSeism() {
        addSpellEffect(SacrificePermanentsEffect.anyNumber(new PermanentIsLandPredicate(), Subjects.you()));
        addSpellEffect(new AwardManaEffect(ManaColor.COLORLESS, new AmountThisWay(StepKind.SACRIFICE)));
    }
}

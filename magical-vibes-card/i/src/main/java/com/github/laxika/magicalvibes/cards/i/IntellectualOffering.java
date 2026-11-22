package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.effect.ChoosePlayerEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.PlayerSubject;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

@CardRegistration(set = "C14", collectorNumber = "15")
@CardRegistration(set = "BLC", collectorNumber = "168")
public class IntellectualOffering extends Card {

    public IntellectualOffering() {
        PlayerSubject youAndThatPlayer = Subjects.allOf(Subjects.you(), Subjects.playersThisWay(StepKind.CHOOSE));
        addSpellEffect(ChoosePlayerEffect.opponent());
        addSpellEffect(new DrawCardEffect(3, youAndThatPlayer));
        addSpellEffect(ChoosePlayerEffect.opponent());
        addSpellEffect(new UntapPermanentsEffect(Subjects.controlledBy(
                youAndThatPlayer, new PermanentNotPredicate(new PermanentIsLandPredicate()))));
    }
}

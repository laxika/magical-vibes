package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.amount.AmountThisWay;
import com.github.laxika.magicalvibes.model.amount.Scaled;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.EscalateCost;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.List;
import com.github.laxika.magicalvibes.model.effect.EachPlayerPaysAnyManaEffect.Resource;
import com.github.laxika.magicalvibes.model.effect.EachPlayerPaysAnyManaEffect;

@CardRegistration(set = "5DN", collectorNumber = "57")
public class PlungeIntoDarkness extends Card {

    public PlungeIntoDarkness() {
        addCastingCost(EscalateCost.entwine("{B}"));
        addSpellEffect(ChooseOneEffect.oneOrMore(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Sacrifice any number of creatures and gain 3 life for each",
                        List.of(
                                SacrificePermanentsEffect.anyNumber(new PermanentIsCreaturePredicate(), Subjects.you()),
                                new GainLifeEffect(new Scaled(new AmountThisWay(StepKind.SACRIFICE), 3)))),
                new ChooseOneEffect.ChooseOneOption(
                        "Pay any amount of life, then look at that many cards",
                        SequenceEffect.of(
                                EachPlayerPaysAnyManaEffect.byYou(Resource.LIFE),
                                LookAtTopCardsEffect.chooseOneToHandRestToExile(AmountThisWay.lifePaid())))
        )));
    }
}

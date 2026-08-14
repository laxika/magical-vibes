package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.amount.Scaled;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.EscalateManaCost;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsEffect;
import com.github.laxika.magicalvibes.model.effect.PayXLifeEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeAnyNumberOfPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.List;

@CardRegistration(set = "5DN", collectorNumber = "57")
public class PlungeIntoDarkness extends Card {

    public PlungeIntoDarkness() {
        addEffect(EffectSlot.SPELL, new EscalateManaCost("{B}"));
        addEffect(EffectSlot.SPELL, ChooseOneEffect.oneOrMore(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Sacrifice any number of creatures and gain 3 life for each",
                        List.of(
                                new SacrificeAnyNumberOfPermanentsEffect(new PermanentIsCreaturePredicate()),
                                new GainLifeEffect(new Scaled(new EventValue(), 3)))),
                new ChooseOneEffect.ChooseOneOption(
                        "Pay any amount of life, then look at that many cards",
                        SequenceEffect.of(
                                new PayXLifeEffect(),
                                LookAtTopCardsEffect.chooseOneToHandRestToExile(new EventValue())))
        )));
    }
}

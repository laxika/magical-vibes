package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.PutCountersEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.amount.AmountThisWay;
import com.github.laxika.magicalvibes.model.amount.CountersOnSource;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.EnterWithCountersEffect;
import com.github.laxika.magicalvibes.model.effect.NoteAmountEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.CardHasXInManaCostPredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "SOC", collectorNumber = "40")
@CardRegistration(set = "SOC", collectorNumber = "88")
public class LatticeLibrary extends Card {

    public LatticeLibrary() {
        addStaticEffect(new EnterWithCountersEffect(CounterType.STUDY, new XValue()));

        addTrigger(Triggers.enters(ObjectRelation.THIS), createFractalWithStudyCounters());

        addTrigger(Triggers.youCastSpell().withFilter(new CardHasXInManaCostPredicate()).nthThisTurn(1),
                createFractalWithStudyCounters());
    }

    private static CardEffect createFractalWithStudyCounters() {
        return SequenceEffect.of(
                new CreateTokenEffect(
                        "Fractal",
                        0,
                        0,
                        CardColor.GREEN,
                        Set.of(CardColor.GREEN, CardColor.BLUE),
                        List.of(CardSubtype.FRACTAL)),
                new NoteAmountEffect(new CountersOnSource(CounterType.STUDY)),
                new PutCountersEffect(Subjects.createdThisResolution(),
                        CounterType.PLUS_ONE_PLUS_ONE, AmountThisWay.noted()));
    }
}

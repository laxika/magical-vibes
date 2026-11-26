package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.amount.AmountThisWay;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenThenPutCountersEffect;

import java.util.List;
import java.util.Set;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

@CardRegistration(set = "SOC", collectorNumber = "322")
@CardRegistration(set = "C21", collectorNumber = "72")
@CardRegistration(set = "DSC", collectorNumber = "228")
public class Oversimplify extends Card {

    public Oversimplify() {
        // "The total power of creatures they controlled that were exiled this way": each as it last existed.
        addSpellEffect(SequenceEffect.of(
                ExileTargetPermanentEffect.all(new PermanentIsCreaturePredicate()),
                new CreateTokenThenPutCountersEffect(
                        new CreateTokenEffect("Fractal", 0, 0, CardColor.GREEN,
                                Set.of(CardColor.GREEN, CardColor.BLUE), List.of(CardSubtype.FRACTAL))
                                .forController(Subjects.eachPlayer()),
                        CounterType.PLUS_ONE_PLUS_ONE,
                        AmountThisWay.exiledPower().controlledByYou())));
    }
}

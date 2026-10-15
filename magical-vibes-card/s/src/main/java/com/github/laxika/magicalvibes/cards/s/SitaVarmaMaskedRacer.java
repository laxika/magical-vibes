package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.amount.SourcePower;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.SetAllOwnCreaturesBasePowerToughnessEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourcePermanentPredicate;

import java.util.List;

@CardRegistration(set = "DFT", collectorNumber = "223")
public class SitaVarmaMaskedRacer extends Card {

    public SitaVarmaMaskedRacer() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{X}{G}{G}{U}",
                List.of(
                        new PutCountersOnSelfEffect(CounterType.PLUS_ONE_PLUS_ONE, new XValue()),
                        new MayEffect(
                                new SetAllOwnCreaturesBasePowerToughnessEffect(
                                        new SourcePower(),
                                        new SourcePower(),
                                        new PermanentNotPredicate(new PermanentIsSourcePermanentPredicate())),
                                "Have the base power and toughness of each other creature you control become equal to Sita Varma's power?")
                ),
                "Exhaust — {X}{G}{G}{U}: Put X +1/+1 counters on Sita Varma. Then you may have the base power and toughness of each other creature you control become equal to Sita Varma's power."
                        + " (Activate each exhaust ability only once.)"
        ).withMaxActivationsPerGame(1).withExhaust());
    }
}

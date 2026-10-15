package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnEachControlledPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.List;

@CardRegistration(set = "M11", collectorNumber = "214")
@CardRegistration(set = "M20", collectorNumber = "239")
@CardRegistration(set = "DDF", collectorNumber = "44")
@CardRegistration(set = "MPS", collectorNumber = "27")
@CardRegistration(set = "PIP", collectorNumber = "241")
@CardRegistration(set = "PIP", collectorNumber = "487")
@CardRegistration(set = "PIP", collectorNumber = "769")
@CardRegistration(set = "PIP", collectorNumber = "1015")
@CardRegistration(set = "C21", collectorNumber = "267")
@CardRegistration(set = "EOC", collectorNumber = "144")
@CardRegistration(set = "BRC", collectorNumber = "164")
@CardRegistration(set = "FDC", collectorNumber = "290")
public class SteelOverseer extends Card {

    public SteelOverseer() {
        // {T}: Put a +1/+1 counter on each artifact creature you control.
        addActivatedAbility(new ActivatedAbility(true, null,
                List.of(new PutCounterOnEachControlledPermanentEffect(
                        CounterType.PLUS_ONE_PLUS_ONE, 1,
                        new PermanentAllOfPredicate(List.of(
                                new PermanentIsArtifactPredicate(),
                                new PermanentIsCreaturePredicate()
                        ))
                )),
                "{T}: Put a +1/+1 counter on each artifact creature you control."));
    }
}

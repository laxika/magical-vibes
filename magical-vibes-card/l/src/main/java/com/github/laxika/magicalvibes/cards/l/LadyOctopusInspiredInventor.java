package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.MayCastArtifactFromHandWithManaValueAtMostSourceCountersEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "SPM", collectorNumber = "35")
@CardRegistration(set = "SPM", collectorNumber = "252")
@CardRegistration(set = "OM1", collectorNumber = "34")
public class LadyOctopusInspiredInventor extends Card {

    public LadyOctopusInspiredInventor() {
        addTrigger(List.of(Triggers.youDrawNthCardEachTurn(1), Triggers.youDrawSecondCardEachTurn()),
                new PutCountersOnSelfEffect(CounterType.INGENUITY));

        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new MayCastArtifactFromHandWithManaValueAtMostSourceCountersEffect(
                        CounterType.INGENUITY)),
                "{T}: You may cast an artifact spell from your hand with mana value less than or equal "
                        + "to the number of ingenuity counters on Lady Octopus without paying its mana cost."
        ));
    }
}

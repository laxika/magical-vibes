package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.FirstOrSecondCardDrawTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.MayCastArtifactFromHandWithManaValueAtMostSourceCountersEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;

import java.util.List;

@CardRegistration(set = "SPM", collectorNumber = "35")
public class LadyOctopusInspiredInventor extends Card {

    public LadyOctopusInspiredInventor() {
        addEffect(EffectSlot.ON_CONTROLLER_DRAWS,
                new FirstOrSecondCardDrawTriggerEffect(
                        new PutCountersOnSelfEffect(CounterType.INGENUITY)));

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

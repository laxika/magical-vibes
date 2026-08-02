package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.amount.CountersOnSource;
import com.github.laxika.magicalvibes.model.amount.Scaled;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

import java.util.List;

@CardRegistration(set = "CHK", collectorNumber = "104")
public class BloodthirstyOgre extends Card {

    public BloodthirstyOgre() {
        // {T}: Put a devotion counter on this creature.
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new PutCountersOnSelfEffect(CounterType.DEVOTION)),
                "{T}: Put a devotion counter on this creature."
        ));

        // {T}: Target creature gets -X/-X until end of turn, where X is the number of devotion
        // counters on this creature. Activate only if you control a Demon.
        Scaled minusDevotion = new Scaled(new CountersOnSource(CounterType.DEVOTION), -1);

        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new BoostTargetCreatureEffect(minusDevotion, minusDevotion)),
                "{T}: Target creature gets -X/-X until end of turn, where X is the number of "
                        + "devotion counters on this creature. Activate only if you control a Demon."
        ).withRequiredControlledPermanents(
                new PermanentHasSubtypePredicate(CardSubtype.DEMON), 1, "Demons"));
    }
}

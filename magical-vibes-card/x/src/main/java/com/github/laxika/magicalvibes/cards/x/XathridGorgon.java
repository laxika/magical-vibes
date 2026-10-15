package com.github.laxika.magicalvibes.cards.x;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.AddCardTypeToTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.BecomeColorlessIndefinitelyEffect;
import com.github.laxika.magicalvibes.model.effect.BuffTargetCreatureIndefinitelyEffect;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.LockTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "M13", collectorNumber = "118")
public class XathridGorgon extends Card {

    public XathridGorgon() {
        // {2}{B}, {T}: Put a petrification counter on target creature. It gains defender and becomes
        // a colorless artifact in addition to its other types. Its activated abilities can't be activated.
        // None of the three continuous effects has a stated duration, so all of them last indefinitely.
        addActivatedAbility(new ActivatedAbility(
                true,
                "{2}{B}",
                List.of(
                        new PutCounterOnTargetPermanentEffect(CounterType.PETRIFICATION, 1),
                        new BuffTargetCreatureIndefinitelyEffect(0, 0, Set.of(Keyword.DEFENDER)),
                        new AddCardTypeToTargetPermanentEffect(CardType.ARTIFACT, true),
                        new BecomeColorlessIndefinitelyEffect(true),
                        new LockTargetPermanentEffect(false, false, true, EffectDuration.PERMANENT)
                ),
                "{2}{B}, {T}: Put a petrification counter on target creature. It gains defender and "
                        + "becomes a colorless artifact in addition to its other types. Its activated "
                        + "abilities can't be activated.",
                TargetFilters.creature()
        ));
    }
}

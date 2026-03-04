package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutMinusOneMinusOneCounterOnTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.TapOrUntapTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "MBS", collectorNumber = "36")
public class VedalkenAnatomist extends Card {

    public VedalkenAnatomist() {
        // {2}{U}, {T}: Put a -1/-1 counter on target creature. You may tap or untap that creature.
        addActivatedAbility(new ActivatedAbility(
                true,
                "{2}{U}",
                List.of(
                        new PutMinusOneMinusOneCounterOnTargetCreatureEffect(),
                        new MayEffect(new TapOrUntapTargetPermanentEffect(), "You may tap or untap that creature?")
                ),
                "{2}{U}, {T}: Put a -1/-1 counter on target creature. You may tap or untap that creature.",
                new PermanentPredicateTargetFilter(
                        new PermanentIsCreaturePredicate(),
                        "Target must be a creature"
                )
        ));
    }
}

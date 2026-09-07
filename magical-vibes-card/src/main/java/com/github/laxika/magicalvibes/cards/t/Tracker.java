package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.SourceFightsTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "DRK", collectorNumber = "89")
public class Tracker extends Card {

    public Tracker() {
        addActivatedAbility(new ActivatedAbility(
                true, "{G}{G}",
                List.of(new SourceFightsTargetCreatureEffect(false)),
                "{G}{G}, {T}: This creature deals damage equal to its power to target creature. "
                        + "That creature deals damage equal to its power to this creature.",
                TargetFilters.creature()
        ));
    }
}

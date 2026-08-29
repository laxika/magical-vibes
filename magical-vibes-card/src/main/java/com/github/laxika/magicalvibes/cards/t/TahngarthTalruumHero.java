package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.SourceFightsTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "PLS", collectorNumber = "74")
public class TahngarthTalruumHero extends Card {

    public TahngarthTalruumHero() {
        addActivatedAbility(new ActivatedAbility(
                true, "{1}{R}",
                List.of(new SourceFightsTargetCreatureEffect()),
                "{1}{R}, {T}: Tahngarth deals damage equal to its power to target creature. "
                        + "That creature deals damage equal to its power to Tahngarth.",
                TargetFilters.creature()
        ));
    }
}

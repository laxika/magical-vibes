package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.RegenerateEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "LEG", collectorNumber = "250")
public class Ragnar extends Card {

    public Ragnar() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{G}{W}{U}",
                List.of(new RegenerateEffect(true)),
                "{G}{W}{U}, {T}: Regenerate target creature.",
                TargetFilters.creature()
        ));
    }
}

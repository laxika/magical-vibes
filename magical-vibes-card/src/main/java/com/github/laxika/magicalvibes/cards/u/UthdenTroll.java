package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.RegenerateEffect;

import java.util.List;

@CardRegistration(set = "4ED", collectorNumber = "228")
public class UthdenTroll extends Card {

    public UthdenTroll() {
        addActivatedAbility(new ActivatedAbility(false, "{R}", List.of(new RegenerateEffect()), "{R}: Regenerate Uthden Troll."));
    }
}

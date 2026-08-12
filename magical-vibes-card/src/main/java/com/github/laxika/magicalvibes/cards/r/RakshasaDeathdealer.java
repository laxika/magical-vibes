package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.RegenerateEffect;

import java.util.List;

@CardRegistration(set = "KTK", collectorNumber = "192")
public class RakshasaDeathdealer extends Card {

    public RakshasaDeathdealer() {
        addActivatedAbility(new ActivatedAbility(false, "{B}{G}", List.of(new BoostSelfEffect(2, 2)),
                "{B}{G}: Rakshasa Deathdealer gets +2/+2 until end of turn."));
        addActivatedAbility(new ActivatedAbility(false, "{B}{G}", List.of(new RegenerateEffect()),
                "{B}{G}: Regenerate Rakshasa Deathdealer."));
    }
}

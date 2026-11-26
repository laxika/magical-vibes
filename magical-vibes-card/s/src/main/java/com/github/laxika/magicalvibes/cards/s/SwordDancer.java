package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;

import java.util.List;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "8ED", collectorNumber = "53")
@CardRegistration(set = "PCY", collectorNumber = "25")
public class SwordDancer extends Card {

    public SwordDancer() {
        addActivatedAbility(new ActivatedAbility(false, "{W}{W}",
                List.of(BoostEffect.target(-1, 0)),
                "{W}{W}: Target attacking creature gets -1/-0 until end of turn.",
                TargetFilters.attackingCreature()));
    }
}

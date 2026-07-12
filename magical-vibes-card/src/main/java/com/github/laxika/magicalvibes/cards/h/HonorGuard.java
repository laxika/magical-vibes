package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;

import java.util.List;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "10E", collectorNumber = "23")
@CardRegistration(set = "9ED", collectorNumber = "20")
@CardRegistration(set = "8ED", collectorNumber = "25")
public class HonorGuard extends Card {

    public HonorGuard() {
        addActivatedAbility(new ActivatedAbility(false, "{W}", List.of(new BoostSelfEffect(0, 1)), "{W}: Honor Guard gets +0/+1 until end of turn."));
    }
}

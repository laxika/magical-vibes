package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.RegenerateEffect;

import java.util.List;

@CardRegistration(set = "STH", collectorNumber = "103")
@CardRegistration(set = "TPR", collectorNumber = "167")
public class Carnassid extends Card {

    public Carnassid() {
        addActivatedAbility(new ActivatedAbility(false, "{1}{G}", List.of(new RegenerateEffect()),
                "{1}{G}: Regenerate Carnassid."));
    }
}

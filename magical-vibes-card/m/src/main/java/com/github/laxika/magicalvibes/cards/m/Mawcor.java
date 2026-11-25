package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;

import java.util.List;

@CardRegistration(set = "7ED", collectorNumber = "87")
@CardRegistration(set = "TMP", collectorNumber = "75")
@CardRegistration(set = "TPR", collectorNumber = "59")
public class Mawcor extends Card {

    public Mawcor() {
        addActivatedAbility(new ActivatedAbility(true, null, List.of(DealDamageEffect.toAnyTarget(1)), "{T}: Mawcor deals 1 damage to any target."));
    }
}

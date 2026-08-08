package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;

import java.util.List;

@CardRegistration(set = "M19", collectorNumber = "63")
public class MysticArchaeologist extends Card {

    public MysticArchaeologist() {
        addActivatedAbility(new ActivatedAbility(false, "{3}{U}{U}", List.of(new DrawCardEffect(2)), "{3}{U}{U}: Draw two cards."));
    }
}

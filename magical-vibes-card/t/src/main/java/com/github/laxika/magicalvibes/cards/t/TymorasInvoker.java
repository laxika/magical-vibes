package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;

import java.util.List;

@CardRegistration(set = "HBG", collectorNumber = "135")
public class TymorasInvoker extends Card {

    public TymorasInvoker() {
        // Sleight of Hand — {8}: Draw two cards.
        addActivatedAbility(new ActivatedAbility(false, "{8}", List.of(new DrawCardEffect(2)), "{8}: Draw two cards."));
    }
}

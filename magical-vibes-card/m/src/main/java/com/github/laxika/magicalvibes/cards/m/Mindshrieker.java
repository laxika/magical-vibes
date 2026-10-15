package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.MillTargetPlayerAndBoostSelfByManaValueEffect;

import java.util.List;

@CardRegistration(set = "ISD", collectorNumber = "67")
public class Mindshrieker extends Card {

    public Mindshrieker() {
        // {2}: Target player mills a card. Mindshrieker gets +X/+X until end of turn,
        // where X is the milled card's mana value.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}",
                List.of(new MillTargetPlayerAndBoostSelfByManaValueEffect()),
                "{2}: Target player mills a card. Mindshrieker gets +X/+X until end of turn, where X is the milled card's mana value."
        ));
    }
}

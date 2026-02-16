package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DiscardCardEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;

import java.util.List;

public class MerfolkLooter extends Card {

    public MerfolkLooter() {
        addActivatedAbility(new ActivatedAbility(true, null, List.of(new DrawCardEffect(), new DiscardCardEffect()), false, "{T}: Draw a card, then discard a card."));
    }
}

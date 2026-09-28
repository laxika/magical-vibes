package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.MayCastCardExiledWithSourceEffect;

import java.util.List;

public class CosmiumCatalyst extends Card {

    public CosmiumCatalyst() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{1}{R}",
                List.of(MayCastCardExiledWithSourceEffect.randomSelection()),
                "{1}{R}, {T}: Choose an exiled card used to craft this artifact at random. You may cast that card without paying its mana cost."
        ));
    }
}

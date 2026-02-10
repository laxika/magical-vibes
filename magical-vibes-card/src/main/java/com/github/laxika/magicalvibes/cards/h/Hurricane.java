package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.DealDamageToFlyingAndPlayersEffect;

import java.util.List;
import java.util.Set;

public class Hurricane extends Card {

    public Hurricane() {
        super("Hurricane", CardType.SORCERY, List.of(), null, List.of(), "{X}{G}", null, null, Set.of(), List.of(), List.of(new DealDamageToFlyingAndPlayersEffect()));
    }
}

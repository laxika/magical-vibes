package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.DealDamageToFlyingAndPlayersEffect;

import java.util.List;

public class Hurricane extends Card {

    public Hurricane() {
        super("Hurricane", CardType.SORCERY, "{X}{G}", CardColor.GREEN);

        setCardText("Hurricane deals X damage to each creature with flying and each player.");
        setSpellEffects(List.of(new DealDamageToFlyingAndPlayersEffect()));
    }
}

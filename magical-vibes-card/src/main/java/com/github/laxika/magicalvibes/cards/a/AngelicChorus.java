package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.GainLifeEqualToToughnessEffect;

import java.util.List;

public class AngelicChorus extends Card {

    public AngelicChorus() {
        super("Angelic Chorus", CardType.ENCHANTMENT, "{3}{W}{W}", CardColor.WHITE);

        setCardText("Whenever a creature you control enters, you gain life equal to its toughness.");
        setOnAllyCreatureEntersBattlefieldEffects(List.of(new GainLifeEqualToToughnessEffect()));
    }
}

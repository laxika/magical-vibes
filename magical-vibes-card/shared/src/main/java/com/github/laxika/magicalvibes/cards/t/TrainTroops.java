package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;

import java.util.List;
import java.util.Set;

public class TrainTroops extends Card {

    public TrainTroops() {
        addEffect(EffectSlot.SPELL, new CreateTokenEffect(
                2,
                "Knight",
                2,
                2,
                CardColor.WHITE,
                List.of(CardSubtype.KNIGHT),
                Set.of(Keyword.VIGILANCE),
                Set.of()));
    }
}

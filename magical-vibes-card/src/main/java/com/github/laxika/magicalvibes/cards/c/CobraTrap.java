package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AlternateHandCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaCastingCost;
import com.github.laxika.magicalvibes.model.condition.NoncreaturePermanentDestroyedByOpponentThisTurn;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "ZEN", collectorNumber = "160")
public class CobraTrap extends Card {

    public CobraTrap() {
        addCastingOption(new AlternateHandCast(
                List.of(new ManaCastingCost("{G}")),
                new NoncreaturePermanentDestroyedByOpponentThisTurn(),
                false));
        addEffect(EffectSlot.SPELL, new CreateTokenEffect(4, "Snake", 1, 1,
                CardColor.GREEN, List.of(CardSubtype.SNAKE), Set.of(), Set.of()));
    }
}

package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateCreatureTokenEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "ISD", collectorNumber = "87")
public class ArmyOfTheDamned extends Card {

    public ArmyOfTheDamned() {
        addEffect(EffectSlot.SPELL, new CreateCreatureTokenEffect(
                13,
                "Zombie",
                2,
                2,
                CardColor.BLACK,
                List.of(CardSubtype.ZOMBIE),
                Set.of(),
                Set.of(),
                true
        ));
        setFlashbackCost("{7}{B}{B}{B}");
    }
}

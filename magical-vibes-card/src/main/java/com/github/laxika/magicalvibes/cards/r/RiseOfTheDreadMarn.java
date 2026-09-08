package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ForetellCast;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.NontokenCreatureDeathsThisTurn;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "KHM", collectorNumber = "107")
public class RiseOfTheDreadMarn extends Card {

    public RiseOfTheDreadMarn() {
        addEffect(EffectSlot.SPELL, new CreateTokenEffect(
                new NontokenCreatureDeathsThisTurn(CountScope.ANY_PLAYER),
                "Zombie Berserker", 2, 2, CardColor.BLACK,
                List.of(CardSubtype.ZOMBIE, CardSubtype.BERSERKER), Set.of(), Set.of()));
        addCastingOption(new ForetellCast("{B}"));
    }
}

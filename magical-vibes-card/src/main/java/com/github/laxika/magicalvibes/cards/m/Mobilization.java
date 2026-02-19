package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostCreaturesBySubtypeEffect;
import com.github.laxika.magicalvibes.model.effect.CreateCreatureTokenEffect;

import java.util.List;
import java.util.Set;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "10E", collectorNumber = "29")
public class Mobilization extends Card {

    public Mobilization() {
        addEffect(EffectSlot.STATIC, new BoostCreaturesBySubtypeEffect(Set.of(CardSubtype.SOLDIER), 0, 0, Set.of(Keyword.VIGILANCE)));
        addActivatedAbility(new ActivatedAbility(false, "{2}{W}", List.of(new CreateCreatureTokenEffect("Soldier", 1, 1, CardColor.WHITE, List.of(CardSubtype.SOLDIER), Set.of(), Set.of())), false, "{2}{W}: Create a 1/1 white Soldier creature token."));
    }
}

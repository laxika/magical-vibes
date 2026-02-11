package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostCreaturesBySubtypeEffect;
import com.github.laxika.magicalvibes.model.effect.CreateCreatureTokenEffect;

import java.util.List;
import java.util.Set;

public class Mobilization extends Card {

    public Mobilization() {
        super("Mobilization", CardType.ENCHANTMENT, "{2}{W}", CardColor.WHITE);

        setCardText("Soldier creatures have vigilance.\n{2}{W}: Create a 1/1 white Soldier creature token.");
        setStaticEffects(List.of(new BoostCreaturesBySubtypeEffect(Set.of(CardSubtype.SOLDIER), 0, 0, Set.of(Keyword.VIGILANCE))));
        setManaActivatedAbilityEffects(List.of(new CreateCreatureTokenEffect("Soldier", 1, 1, CardColor.WHITE, List.of(CardSubtype.SOLDIER))));
        setManaActivatedAbilityCost("{2}{W}");
    }
}

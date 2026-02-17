package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CreateCreatureTokenEffect;

import java.util.List;
import java.util.Set;

public class DragonRoost extends Card {

    public DragonRoost() {
        addActivatedAbility(new ActivatedAbility(false, "{5}{R}{R}", List.of(new CreateCreatureTokenEffect("Dragon", 5, 5, CardColor.RED, List.of(CardSubtype.DRAGON), Set.of(Keyword.FLYING), Set.of())), false, "{5}{R}{R}: Create a 5/5 red Dragon creature token with flying."));
    }
}

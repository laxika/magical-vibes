package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.MakeTargetUnblockableEffect;
import com.github.laxika.magicalvibes.model.filter.MaxPowerTargetFilter;

import java.util.List;

public class CraftyPathmage extends Card {

    public CraftyPathmage() {
        super("Crafty Pathmage", CardType.CREATURE, "{2}{U}", CardColor.BLUE);

        setSubtypes(List.of(CardSubtype.HUMAN, CardSubtype.WIZARD));
        setCardText("{T}: Target creature with power 2 or less can't be blocked this turn.");
        setPower(1);
        setToughness(1);
        addActivatedAbility(new ActivatedAbility(true, null, List.of(new MakeTargetUnblockableEffect()), true, "{T}: Target creature with power 2 or less can't be blocked this turn.", new MaxPowerTargetFilter(2)));
    }
}

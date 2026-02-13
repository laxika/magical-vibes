package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.TapTargetPermanentEffect;

import java.util.List;
import java.util.Set;

public class IcyManipulator extends Card {

    public IcyManipulator() {
        super("Icy Manipulator", CardType.ARTIFACT, "{4}", null);

        setCardText("{1}, {T}: Tap target artifact, creature, or land.");
        addActivatedAbility(new ActivatedAbility(true, "{1}", List.of(new TapTargetPermanentEffect(Set.of(CardType.ARTIFACT, CardType.CREATURE, CardType.BASIC_LAND))), true, "{1}, {T}: Tap target artifact, creature, or land."));
    }
}

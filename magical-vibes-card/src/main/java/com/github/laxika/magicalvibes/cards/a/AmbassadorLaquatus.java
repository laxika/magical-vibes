package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.MillTargetPlayerEffect;

import java.util.List;
import java.util.Set;

public class AmbassadorLaquatus extends Card {

    public AmbassadorLaquatus() {
        super("Ambassador Laquatus", CardType.CREATURE, "{1}{U}{U}", CardColor.BLUE);

        setSupertypes(Set.of(CardSupertype.LEGENDARY));
        setSubtypes(List.of(CardSubtype.MERFOLK, CardSubtype.WIZARD));
        setCardText("{3}: Target player mills three cards.");
        setPower(1);
        setToughness(3);
        addActivatedAbility(new ActivatedAbility(false, "{3}", List.of(new MillTargetPlayerEffect(3)), true, "{3}: Target player mills three cards."));
    }
}

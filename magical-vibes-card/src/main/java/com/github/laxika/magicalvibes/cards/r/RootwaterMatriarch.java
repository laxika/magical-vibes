package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.GainControlOfEnchantedTargetEffect;

import java.util.List;

public class RootwaterMatriarch extends Card {

    public RootwaterMatriarch() {
        super("Rootwater Matriarch", CardType.CREATURE, "{2}{U}{U}", CardColor.BLUE);

        setSubtypes(List.of(CardSubtype.MERFOLK));
        setPower(2);
        setToughness(3);
        setCardText("{T}: Gain control of target creature for as long as that creature is enchanted.");
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new GainControlOfEnchantedTargetEffect()),
                true,
                "{T}: Gain control of target creature for as long as that creature is enchanted."
        ));
    }
}

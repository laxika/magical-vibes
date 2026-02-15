package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CounterUnlessPaysEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;

import java.util.List;
import java.util.Set;

public class SpiketailHatchling extends Card {

    public SpiketailHatchling() {
        super("Spiketail Hatchling", CardType.CREATURE, "{1}{U}", CardColor.BLUE);

        setSubtypes(List.of(CardSubtype.DRAKE));
        setCardText("Flying\nSacrifice Spiketail Hatchling: Counter target spell unless its controller pays {1}.");
        setKeywords(Set.of(Keyword.FLYING));
        setPower(1);
        setToughness(1);
        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(new SacrificeSelfCost(), new CounterUnlessPaysEffect(1)),
                false,
                true,
                "Sacrifice Spiketail Hatchling: Counter target spell unless its controller pays {1}.",
                null
        ));
    }
}

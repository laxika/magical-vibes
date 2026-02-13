package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;

import java.util.List;

public class SpiritWeaver extends Card {

    public SpiritWeaver() {
        super("Spirit Weaver", CardType.CREATURE, "{1}{W}", CardColor.WHITE);

        setSubtypes(List.of(CardSubtype.HUMAN, CardSubtype.WIZARD));
        setCardText("{2}: Target green or blue creature gets +0/+1 until end of turn.");
        setPower(2);
        setToughness(1);
        addActivatedAbility(new ActivatedAbility(false, "{2}", List.of(new BoostTargetCreatureEffect(0, 1)), true, "{2}: Target green or blue creature gets +0/+1 until end of turn."));
    }
}

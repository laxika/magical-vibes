package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CantAttackUnlessDefenderControlsLandTypeEffect;

import java.util.List;

public class SeaMonster extends Card {

    public SeaMonster() {
        super("Sea Monster", CardType.CREATURE, "{4}{U}{U}", CardColor.BLUE);

        setSubtypes(List.of(CardSubtype.SERPENT));
        setCardText("Sea Monster can't attack unless defending player controls an Island.");
        addEffect(EffectSlot.STATIC, new CantAttackUnlessDefenderControlsLandTypeEffect(CardSubtype.ISLAND));
        setPower(6);
        setToughness(6);
    }
}

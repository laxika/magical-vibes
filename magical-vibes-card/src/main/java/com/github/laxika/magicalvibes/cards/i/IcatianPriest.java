package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;

import java.util.List;

public class IcatianPriest extends Card {

    public IcatianPriest() {
        super("Icatian Priest", CardType.CREATURE, "{W}", CardColor.WHITE);

        setSubtypes(List.of(CardSubtype.HUMAN, CardSubtype.CLERIC));
        setCardText("{1}{W}{W}: Target creature gets +1/+1 until end of turn.");
        setPower(1);
        setToughness(1);
        setNeedsTarget(true);
        addEffect(EffectSlot.MANA_ACTIVATED_ABILITY, new BoostTargetCreatureEffect(1, 1));
        setManaActivatedAbilityCost("{1}{W}{W}");
    }
}

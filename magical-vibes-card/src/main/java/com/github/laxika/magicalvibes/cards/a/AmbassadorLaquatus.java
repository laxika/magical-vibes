package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MillTargetPlayerEffect;

import java.util.List;

public class AmbassadorLaquatus extends Card {

    public AmbassadorLaquatus() {
        super("Ambassador Laquatus", CardType.CREATURE, "{1}{U}{U}", CardColor.BLUE);

        setSubtypes(List.of(CardSubtype.MERFOLK, CardSubtype.WIZARD));
        setCardText("{3}: Target player mills three cards.");
        setNeedsTarget(true);
        setPower(1);
        setToughness(3);
        setManaActivatedAbilityCost("{3}");
        addEffect(EffectSlot.MANA_ACTIVATED_ABILITY, new MillTargetPlayerEffect(3));
    }
}

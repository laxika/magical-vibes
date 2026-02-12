package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PreventNextDamageEffect;

import java.util.List;

public class SamiteHealer extends Card {

    public SamiteHealer() {
        super("Samite Healer", CardType.CREATURE, "{1}{W}", CardColor.WHITE);

        setSubtypes(List.of(CardSubtype.HUMAN, CardSubtype.CLERIC));
        setCardText("{T}: Prevent the next 1 damage that would be dealt to any target this turn.");
        setPower(1);
        setToughness(1);
        addEffect(EffectSlot.TAP_ACTIVATED_ABILITY, new PreventNextDamageEffect(1));
    }
}

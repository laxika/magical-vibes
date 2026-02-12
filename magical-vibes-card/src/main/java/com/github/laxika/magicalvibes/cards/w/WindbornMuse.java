package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.RequirePaymentToAttackEffect;

import java.util.List;
import java.util.Set;

public class WindbornMuse extends Card {

    public WindbornMuse() {
        super("Windborn Muse", CardType.CREATURE, "{3}{W}", CardColor.WHITE);

        setSubtypes(List.of(CardSubtype.SPIRIT));
        setCardText("Flying\nCreatures can't attack you unless their controller pays {2} for each creature they control that's attacking you.");
        setKeywords(Set.of(Keyword.FLYING));
        addEffect(EffectSlot.STATIC, new RequirePaymentToAttackEffect(2));
        setPower(2);
        setToughness(3);
    }
}

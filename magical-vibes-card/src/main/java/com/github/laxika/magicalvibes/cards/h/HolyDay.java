package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.PreventAllCombatDamageEffect;

import java.util.List;

public class HolyDay extends Card {

    public HolyDay() {
        super("Holy Day", CardType.INSTANT, "{W}", CardColor.WHITE);

        setCardText("Prevent all combat damage that would be dealt this turn.");
        addEffect(EffectSlot.SPELL, new PreventAllCombatDamageEffect());
    }
}

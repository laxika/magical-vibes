package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.PreventAllCombatDamageEffect;

import java.util.List;

public class HolyDay extends Card {

    public HolyDay() {
        super("Holy Day", CardType.INSTANT, "{W}", CardColor.WHITE);

        setCardText("Prevent all combat damage that would be dealt this turn.");
        setSpellEffects(List.of(new PreventAllCombatDamageEffect()));
    }
}

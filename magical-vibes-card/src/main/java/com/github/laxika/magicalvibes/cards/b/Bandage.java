package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.PreventDamageToTargetEffect;

import java.util.List;

public class Bandage extends Card {

    public Bandage() {
        super("Bandage", CardType.INSTANT, "{W}", CardColor.WHITE);

        setCardText("Prevent the next 1 damage that would be dealt to any target this turn.\nDraw a card.");
        setNeedsTarget(true);
        setSpellEffects(List.of(new PreventDamageToTargetEffect(1), new DrawCardEffect()));
    }
}

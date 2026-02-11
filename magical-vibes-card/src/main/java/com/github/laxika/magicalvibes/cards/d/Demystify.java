package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;

import java.util.List;
import java.util.Set;

public class Demystify extends Card {

    public Demystify() {
        super("Demystify", CardType.INSTANT, "{W}", CardColor.WHITE);

        setCardText("Destroy target enchantment.");
        setNeedsTarget(true);
        setSpellEffects(List.of(new DestroyTargetPermanentEffect(Set.of(CardType.ENCHANTMENT))));
    }
}

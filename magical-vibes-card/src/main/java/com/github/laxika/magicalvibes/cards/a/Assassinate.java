package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.TappedTargetFilter;

import java.util.Set;

public class Assassinate extends Card {

    public Assassinate() {
        super("Assassinate", CardType.SORCERY, "{2}{B}", CardColor.BLACK);

        setCardText("Destroy target tapped creature.");
        setNeedsTarget(true);
        setTargetFilter(new TappedTargetFilter());
        addEffect(EffectSlot.SPELL, new DestroyTargetPermanentEffect(Set.of(CardType.CREATURE)));
    }
}

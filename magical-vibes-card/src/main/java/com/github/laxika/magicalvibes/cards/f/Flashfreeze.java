package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CounterSpellEffect;
import com.github.laxika.magicalvibes.model.filter.SpellColorTargetFilter;

import java.util.Set;

public class Flashfreeze extends Card {

    public Flashfreeze() {
        super("Flashfreeze", CardType.INSTANT, "{1}{U}", CardColor.BLUE);

        setCardText("Counter target red or green spell.");
        setNeedsSpellTarget(true);
        setTargetFilter(new SpellColorTargetFilter(Set.of(CardColor.RED, CardColor.GREEN)));
        addEffect(EffectSlot.SPELL, new CounterSpellEffect());
    }
}

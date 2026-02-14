package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.CounterSpellEffect;
import com.github.laxika.magicalvibes.model.filter.SpellTypeTargetFilter;

import java.util.Set;

public class RemoveSoul extends Card {

    public RemoveSoul() {
        super("Remove Soul", CardType.INSTANT, "{1}{U}", CardColor.BLUE);

        setCardText("Counter target creature spell.");
        setNeedsSpellTarget(true);
        setTargetFilter(new SpellTypeTargetFilter(Set.of(StackEntryType.CREATURE_SPELL)));
        addEffect(EffectSlot.SPELL, new CounterSpellEffect());
    }
}

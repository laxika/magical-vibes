package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CounterSpellEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentCost;
import com.github.laxika.magicalvibes.model.filter.PermanentColorInPredicate;

import java.util.Set;

@CardRegistration(set = "WTH", collectorNumber = "31")
public class Abjure extends Card {

    public Abjure() {
        // As an additional cost to cast this spell, sacrifice a blue permanent.
        addEffect(EffectSlot.SPELL, new SacrificePermanentCost(
                new PermanentColorInPredicate(Set.of(CardColor.BLUE)), "Sacrifice a blue permanent"));

        // Counter target spell.
        addEffect(EffectSlot.SPELL, new CounterSpellEffect());
    }
}

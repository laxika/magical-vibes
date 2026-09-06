package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.TargetSpellNoManaSpentToCast;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CounterSpellEffect;

@CardRegistration(set = "FUT", collectorNumber = "55")
public class Nix extends Card {

    public Nix() {
        addEffect(EffectSlot.SPELL, new ConditionalEffect(
                new TargetSpellNoManaSpentToCast(), new CounterSpellEffect()));
    }
}

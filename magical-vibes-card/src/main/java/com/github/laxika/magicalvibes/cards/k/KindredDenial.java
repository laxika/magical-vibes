package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.TargetSpellManaValue;
import com.github.laxika.magicalvibes.model.effect.CounterSpellEffect;
import com.github.laxika.magicalvibes.model.effect.ManaValueBound;
import com.github.laxika.magicalvibes.model.effect.SeekCardsToHandEffect;

@CardRegistration(set = "YMID", collectorNumber = "18")
public class KindredDenial extends Card {

    public KindredDenial() {
        // Seek first so the target spell's mana value is still available.
        addEffect(EffectSlot.SPELL, new SeekCardsToHandEffect(
                new Fixed(1), null,
                new ManaValueBound(new TargetSpellManaValue(), true, 0)));
        addEffect(EffectSlot.SPELL, new CounterSpellEffect());
    }
}

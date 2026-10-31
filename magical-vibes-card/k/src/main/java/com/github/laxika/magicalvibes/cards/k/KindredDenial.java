package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.TargetSpellManaValue;
import com.github.laxika.magicalvibes.model.effect.CounterSpellEffect;
import com.github.laxika.magicalvibes.model.effect.ManaValueBound;
import com.github.laxika.magicalvibes.model.effect.SeekCardsToHandEffect;

@CardRegistration(set = "YMID", collectorNumber = "18")
public class KindredDenial extends Card {

    public KindredDenial() {
        // Counter first, then seek: the countered spell's mana value is remembered on the resolving entry,
        // and a spell shuffled into its owner's library by a replacement effect can be the card sought.
        addSpellEffect(new CounterSpellEffect());
        addSpellEffect(new SeekCardsToHandEffect(
                new Fixed(1), null,
                new ManaValueBound(new TargetSpellManaValue(), true, 0)));
    }
}

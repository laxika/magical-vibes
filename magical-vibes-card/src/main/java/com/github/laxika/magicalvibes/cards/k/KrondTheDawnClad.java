package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.Enchanted;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "PC2", collectorNumber = "99")
public class KrondTheDawnClad extends Card {

    public KrondTheDawnClad() {
        // Whenever Krond attacks, if it's enchanted, exile target permanent.
        target(TargetFilters.permanent()).addEffect(EffectSlot.ON_ATTACK,
                new ConditionalEffect(new Enchanted(), new ExileTargetPermanentEffect()));
    }
}

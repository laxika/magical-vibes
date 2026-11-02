package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.Enchanted;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "PC2", collectorNumber = "99")
@CardRegistration(set = "PCA", collectorNumber = "99")
public class KrondTheDawnClad extends Card {

    public KrondTheDawnClad() {
        // Whenever Krond attacks, if it's enchanted, exile target permanent.
        target(TargetFilters.permanent()).addTrigger(Triggers.attacks(),
                new ConditionalEffect(new Enchanted(), new ExileTargetPermanentEffect()));
    }
}

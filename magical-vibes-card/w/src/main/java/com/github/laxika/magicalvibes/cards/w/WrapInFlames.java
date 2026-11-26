package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.CantBlockThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "ROE", collectorNumber = "173")
@CardRegistration(set = "THB", collectorNumber = "164")
@CardRegistration(set = "MM2", collectorNumber = "136")
public class WrapInFlames extends Card {

    public WrapInFlames() {
        target(TargetFilters.creature(), 0, 3)
                .addSpellEffect(DealDamageEffect.toEachTarget(new Fixed(1)))
                .addSpellEffect(new CantBlockThisTurnEffect(Subjects.target()));
    }
}

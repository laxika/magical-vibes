package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.amount.Scaled;
import com.github.laxika.magicalvibes.model.amount.Sum;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "OTJ", collectorNumber = "85")
public class DesertsDue extends Card {

    public DesertsDue() {
        PermanentCount deserts = new PermanentCount(
                new PermanentHasSubtypePredicate(CardSubtype.DESERT), CountScope.CONTROLLER);
        target(TargetFilters.creature()).addEffect(EffectSlot.SPELL,
                new BoostTargetCreatureEffect(
                        new Sum(new Fixed(-2), new Scaled(deserts, -1)),
                        new Sum(new Fixed(-2), new Scaled(deserts, -1))));
    }
}

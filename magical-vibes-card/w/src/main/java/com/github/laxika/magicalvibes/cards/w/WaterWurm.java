package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.OpponentControlsPermanent;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

@CardRegistration(set = "DRK", collectorNumber = "38")
public class WaterWurm extends Card {

    public WaterWurm() {
        // This creature gets +0/+1 as long as an opponent controls an Island.
        addEffect(EffectSlot.STATIC, new ConditionalEffect(
                new OpponentControlsPermanent(new PermanentHasSubtypePredicate(CardSubtype.ISLAND)),
                new StaticBoostEffect(0, 1, GrantScope.SELF)));
    }
}

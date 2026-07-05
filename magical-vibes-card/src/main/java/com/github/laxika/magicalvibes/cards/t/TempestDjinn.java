package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSupertypePredicate;

import java.util.List;

@CardRegistration(set = "DOM", collectorNumber = "68")
public class TempestDjinn extends Card {

    public TempestDjinn() {
        // Tempest Djinn gets +1/+0 for each basic Island you control.
        addEffect(EffectSlot.STATIC, new BoostSelfEffect(
                new PermanentCount(new PermanentAllOfPredicate(List.of(
                        new PermanentHasSupertypePredicate(CardSupertype.BASIC),
                        new PermanentHasSubtypePredicate(CardSubtype.ISLAND)
                )), CountScope.CONTROLLER),
                new Fixed(0)));
    }
}

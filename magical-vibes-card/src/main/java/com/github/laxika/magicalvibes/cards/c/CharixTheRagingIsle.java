package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.amount.Scaled;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.IncreaseOpponentCostForTargetingControlledPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourcePermanentPredicate;

import java.util.List;

@CardRegistration(set = "ZNR", collectorNumber = "49")
public class CharixTheRagingIsle extends Card {

    public CharixTheRagingIsle() {
        addEffect(EffectSlot.STATIC, new IncreaseOpponentCostForTargetingControlledPermanentEffect(
                new PermanentIsSourcePermanentPredicate(), 2, false));

        PermanentCount islandsYouControl = new PermanentCount(
                new PermanentHasSubtypePredicate(CardSubtype.ISLAND), CountScope.CONTROLLER);
        addActivatedAbility(new ActivatedAbility(
                false,
                "{3}",
                List.of(new BoostSelfEffect(
                        islandsYouControl,
                        new Scaled(islandsYouControl, -1))),
                "{3}: Charix, the Raging Isle gets +X/-X until end of turn, where X is the number of Islands you control."
        ));
    }
}

package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.AnimatePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.CrewCost;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

import java.util.List;

@CardRegistration(set = "SPM", collectorNumber = "174")
public class SpiderMobile extends Card {

    public SpiderMobile() {
        PermanentCount spidersYouControl = new PermanentCount(
                new PermanentHasSubtypePredicate(CardSubtype.SPIDER), CountScope.CONTROLLER);
        addEffect(EffectSlot.ON_ATTACK, new BoostSelfEffect(spidersYouControl, spidersYouControl));
        addEffect(EffectSlot.ON_BLOCK, new BoostSelfEffect(spidersYouControl, spidersYouControl));

        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(new CrewCost(2), AnimatePermanentsEffect.crew()),
                "Crew 2"
        ));
    }
}

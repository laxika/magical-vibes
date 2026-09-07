package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

@CardRegistration(set = "TMT", collectorNumber = "14")
public class LeonardoBigBrother extends Card {

    public LeonardoBigBrother() {
        addSneak("{W}");

        PermanentCount otherCreaturesYouControl = new PermanentCount(
                new PermanentIsCreaturePredicate(), CountScope.CONTROLLER, true);
        addEffect(EffectSlot.STATIC, new BoostSelfEffect(otherCreaturesYouControl, new Fixed(0)));
    }
}

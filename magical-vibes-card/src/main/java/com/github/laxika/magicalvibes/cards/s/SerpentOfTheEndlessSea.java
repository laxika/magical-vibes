package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.CantAttackUnlessDefenderControlsMatchingPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.SetPowerToughnessToAmountEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

@CardRegistration(set = "M10", collectorNumber = "70")
public class SerpentOfTheEndlessSea extends Card {

    public SerpentOfTheEndlessSea() {
        PermanentCount islandsYouControl =
                new PermanentCount(new PermanentHasSubtypePredicate(CardSubtype.ISLAND), CountScope.CONTROLLER);
        addEffect(EffectSlot.STATIC, new SetPowerToughnessToAmountEffect(islandsYouControl, islandsYouControl));
        addEffect(EffectSlot.STATIC, new CantAttackUnlessDefenderControlsMatchingPermanentEffect(
                new PermanentHasSubtypePredicate(CardSubtype.ISLAND),
                "an Island"
        ));
    }
}

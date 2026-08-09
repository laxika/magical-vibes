package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.SetPowerToughnessToAmountEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

@CardRegistration(set = "NEM", collectorNumber = "103")
public class CoilingWoodworm extends Card {

    public CoilingWoodworm() {
        // Coiling Woodworm's power is equal to the number of Forests on the battlefield.
        PermanentCount forestsOnBattlefield =
                new PermanentCount(new PermanentHasSubtypePredicate(CardSubtype.FOREST), CountScope.ANY_PLAYER);
        addEffect(EffectSlot.STATIC, new SetPowerToughnessToAmountEffect(forestsOnBattlefield, new Fixed(1)));
    }
}

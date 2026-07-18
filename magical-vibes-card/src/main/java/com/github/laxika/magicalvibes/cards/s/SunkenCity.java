package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ForcedCostOrElseEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.PayManaCost;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentColorInPredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "4ED", collectorNumber = "106")
public class SunkenCity extends Card {

    public SunkenCity() {
        // At the beginning of your upkeep, sacrifice this enchantment unless you pay {U}{U}.
        addEffect(EffectSlot.UPKEEP_TRIGGERED,
                new ForcedCostOrElseEffect(
                        new PayManaCost("{U}{U}"),
                        List.of(new SacrificeSelfEffect()),
                        true));
        // Blue creatures get +1/+1.
        addEffect(EffectSlot.STATIC, new StaticBoostEffect(1, 1, GrantScope.ALL_CREATURES,
                new PermanentColorInPredicate(Set.of(CardColor.BLUE))));
    }
}

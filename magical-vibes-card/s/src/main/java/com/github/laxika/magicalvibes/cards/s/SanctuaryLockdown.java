package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.effect.TapMultiplePermanentsCost;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "IKO", collectorNumber = "28")
public class SanctuaryLockdown extends Card {

    public SanctuaryLockdown() {
        var humanFilter = new PermanentHasSubtypePredicate(CardSubtype.HUMAN);

        addEffect(EffectSlot.STATIC, new StaticBoostEffect(1, 1, GrantScope.OWN_CREATURES, humanFilter));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}",
                List.of(
                        new TapMultiplePermanentsCost(2, humanFilter),
                        new TapPermanentsEffect(TapUntapScope.TARGET)),
                "{2}, Tap two untapped Humans you control: Tap target creature an opponent controls.",
                TargetFilters.creatureAnOpponentControls()
        ));
    }
}

package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostEnchantedAndSharingCreaturesUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "ONS", collectorNumber = "134")
public class CrownOfSuspicion extends Card {

    public CrownOfSuspicion() {
        target(TargetFilters.creature())
                .addEffect(EffectSlot.STATIC, new StaticBoostEffect(2, -1, GrantScope.ENCHANTED_CREATURE));

        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(
                        new SacrificeSelfCost(),
                        new BoostEnchantedAndSharingCreaturesUntilEndOfTurnEffect(2, -1)
                ),
                "Sacrifice this Aura: Enchanted creature and other creatures that share a creature type with it get +2/-1 until end of turn."
        ));
    }
}

package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.BoostEquippedCreatureUntilEndOfTurnEffect;

@CardRegistration(set = "M11", collectorNumber = "156")
@CardRegistration(set = "M14", collectorNumber = "153")
@CardRegistration(set = "USG", collectorNumber = "216")
@CardRegistration(set = "DDG", collectorNumber = "74")
public class ShivsEmbrace extends Card {

    public ShivsEmbrace() {
        target(TargetFilters.creature()).addEffect(EffectSlot.STATIC, new StaticBoostEffect(2, 2, GrantScope.ENCHANTED_CREATURE))
                .addEffect(EffectSlot.STATIC, new GrantKeywordEffect(Keyword.FLYING, GrantScope.ENCHANTED_CREATURE));
        addActivatedAbility(new ActivatedAbility(false, "{R}",
                List.of(new BoostEquippedCreatureUntilEndOfTurnEffect(new Fixed(1), new Fixed(0))),
                "{R}: Enchanted creature gets +1/+0 until end of turn."));
    }
}

package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.BoostEquippedCreatureUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "GPT", collectorNumber = "14")
public class ShadowLance extends Card {

    public ShadowLance() {
        target(TargetFilters.creature())
                .addEffect(EffectSlot.STATIC,
                        new GrantKeywordEffect(Keyword.FIRST_STRIKE, GrantScope.ENCHANTED_CREATURE));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}{B}",
                List.of(new BoostEquippedCreatureUntilEndOfTurnEffect(new Fixed(2), new Fixed(2))),
                "{1}{B}: Enchanted creature gets +2/+2 until end of turn."
        ));
    }
}

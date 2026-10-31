package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.AttachedBoostEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.ReturnSourceTransformedFromGraveyardAttachedToTargetPlayerEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "VOW", collectorNumber = "31")
@CardRegistration(set = "DBL", collectorNumber = "298")
public class RadiantGrace extends Card {

    public RadiantGrace() {
        setBackFaceCard(new RadiantRestraints());

        target(TargetFilters.creature())
                .addStaticEffect(new AttachedBoostEffect(
                        new Fixed(1),
                        new Fixed(0),
                        GrantScope.ENCHANTED_CREATURE))
                .addStaticEffect(new GrantKeywordEffect(Keyword.VIGILANCE, GrantScope.ENCHANTED_CREATURE));
        addEffect(EffectSlot.ON_ENCHANTED_PERMANENT_PUT_INTO_GRAVEYARD,
                new ReturnSourceTransformedFromGraveyardAttachedToTargetPlayerEffect());
    }

    @Override
    public String getBackFaceClassName() {
        return "RadiantRestraints";
    }
}

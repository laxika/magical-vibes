package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MustAttackEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnSourceTransformedFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "SOI", collectorNumber = "182")
public class SkinInvasion extends Card {

    public SkinInvasion() {
        setBackFaceCard(new SkinShedder());

        target(TargetFilters.creature())
                .addEffect(EffectSlot.STATIC, new MustAttackEffect())
                .addEffect(EffectSlot.ON_ENCHANTED_PERMANENT_PUT_INTO_GRAVEYARD,
                        new ReturnSourceTransformedFromGraveyardEffect());
    }

    @Override
    public String getBackFaceClassName() {
        return "SkinShedder";
    }
}

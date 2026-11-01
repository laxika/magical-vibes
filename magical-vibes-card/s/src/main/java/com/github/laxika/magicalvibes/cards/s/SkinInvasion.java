package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.MustAttackEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnSourceTransformedFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "SOI", collectorNumber = "182")
public class SkinInvasion extends Card {

    public SkinInvasion() {
        setBackFaceCard(new SkinShedder());

        target(TargetFilters.creature())
                .addStaticEffect(new MustAttackEffect())
                .addTrigger(Triggers.dies(ObjectRelation.ENCHANTED),
                        new ReturnSourceTransformedFromGraveyardEffect());
    }

    @Override
    public String getBackFaceClassName() {
        return "SkinShedder";
    }
}

package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.h.HeartsDesire;
import com.github.laxika.magicalvibes.model.AdventureCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanentCount;
import com.github.laxika.magicalvibes.model.effect.CantAttackUnlessEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPowerAtLeastPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPowerAtMostPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentToughnessAtLeastPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentToughnessAtMostPredicate;

import java.util.List;

@CardRegistration(set = "ELD", collectorNumber = "165")
public class LovestruckBeast extends Card {

    public LovestruckBeast() {
        setBackFaceCard(new HeartsDesire());
        addCastingOption(new AdventureCast("{G}"));
        addEffect(EffectSlot.STATIC, new CantAttackUnlessEffect(
                new ControlsPermanentCount(1, new PermanentAllOfPredicate(List.of(
                        new PermanentIsCreaturePredicate(),
                        new PermanentPowerAtLeastPredicate(1),
                        new PermanentPowerAtMostPredicate(1),
                        new PermanentToughnessAtLeastPredicate(1),
                        new PermanentToughnessAtMostPredicate(1)
                ))),
                "you control a 1/1 creature"
        ));
    }

    @Override
    public String getBackFaceClassName() {
        return "HeartsDesire";
    }
}

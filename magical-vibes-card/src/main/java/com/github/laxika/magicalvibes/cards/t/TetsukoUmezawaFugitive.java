package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ControlledCreaturesMatchingCantBeBlockedEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPowerAtMostPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentToughnessAtMostPredicate;

import java.util.List;

@CardRegistration(set = "DOM", collectorNumber = "69")
public class TetsukoUmezawaFugitive extends Card {

    public TetsukoUmezawaFugitive() {
        addEffect(EffectSlot.STATIC, new ControlledCreaturesMatchingCantBeBlockedEffect(
                new PermanentAnyOfPredicate(List.of(
                        new PermanentPowerAtMostPredicate(1),
                        new PermanentToughnessAtMostPredicate(1)
                ))
        ));
    }
}

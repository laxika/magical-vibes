package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostAllCreaturesEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentColorInPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsAttackingPredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "RAV", collectorNumber = "190")
public class AgrusKosWojekVeteran extends Card {

    public AgrusKosWojekVeteran() {
        addEffect(EffectSlot.ON_ATTACK, new BoostAllCreaturesEffect(2, 0,
                new PermanentAllOfPredicate(List.of(
                        new PermanentIsAttackingPredicate(),
                        new PermanentColorInPredicate(Set.of(CardColor.RED))))));
        addEffect(EffectSlot.ON_ATTACK, new BoostAllCreaturesEffect(0, 2,
                new PermanentAllOfPredicate(List.of(
                        new PermanentIsAttackingPredicate(),
                        new PermanentColorInPredicate(Set.of(CardColor.WHITE))))));
    }
}

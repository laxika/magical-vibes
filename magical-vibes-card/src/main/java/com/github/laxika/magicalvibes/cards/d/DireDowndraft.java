package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PutTargetSpellOrCreatureOnTopOrBottomOfLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.ReduceOwnCastCostIfTargetingPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsAttackingPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTappedPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "BLB", collectorNumber = "46")
public class DireDowndraft extends Card {

    public DireDowndraft() {
        addEffect(EffectSlot.STATIC, new ReduceOwnCastCostIfTargetingPermanentEffect(
                new PermanentAnyOfPredicate(List.of(
                        new PermanentIsAttackingPredicate(),
                        new PermanentIsTappedPredicate()
                )), 1));
        target(TargetFilters.creature())
                .addEffect(EffectSlot.SPELL, new PutTargetSpellOrCreatureOnTopOrBottomOfLibraryEffect());
    }
}

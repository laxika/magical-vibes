package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MatchingCreaturesCanAttackAsThoughNoDefenderEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsModifiedPredicate;

import java.util.List;

@CardRegistration(set = "NEO", collectorNumber = "56")
public class GuardiansOfOboro extends Card {

    public GuardiansOfOboro() {
        addEffect(EffectSlot.STATIC, new MatchingCreaturesCanAttackAsThoughNoDefenderEffect(
                new PermanentAllOfPredicate(List.of(
                        new PermanentIsModifiedPredicate(),
                        new PermanentControlledBySourceControllerPredicate()))));
    }
}

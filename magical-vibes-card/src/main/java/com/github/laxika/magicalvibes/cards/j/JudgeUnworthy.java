package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.RevealTopCardDealManaValueDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsAttackingPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsBlockingPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "FUT", collectorNumber = "9")
public class JudgeUnworthy extends Card {

    public JudgeUnworthy() {
        target(new PermanentPredicateTargetFilter(
                new PermanentAllOfPredicate(List.of(
                        new PermanentIsCreaturePredicate(),
                        new PermanentAnyOfPredicate(List.of(
                                new PermanentIsAttackingPredicate(),
                                new PermanentIsBlockingPredicate())))),
                "Target must be an attacking or blocking creature"))
                .addEffect(EffectSlot.SPELL, new ScryEffect(3))
                .addEffect(EffectSlot.SPELL, new RevealTopCardDealManaValueDamageToAnyTargetEffect());
    }
}

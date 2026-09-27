package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostAllCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.MatchingCreaturesMustAttackEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsAttackingPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.List;

@CardRegistration(set = "C14", collectorNumber = "41")
public class WarmongerHellkite extends Card {

    public WarmongerHellkite() {
        addEffect(EffectSlot.STATIC, new MatchingCreaturesMustAttackEffect(
                new PermanentIsCreaturePredicate()));
        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}{R}",
                List.of(new BoostAllCreaturesEffect(1, 0, new PermanentIsAttackingPredicate())),
                "{1}{R}: Attacking creatures get +1/+0 until end of turn."
        ));
    }
}

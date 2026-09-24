package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.BoostAllOwnCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.BoostOwnCreaturesAttackingPlayersByDefendingPlayerCreatureCountEffect;
import com.github.laxika.magicalvibes.model.effect.MustBeBlockedIfAbleEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsAttackingPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

@CardRegistration(set = "SLD", collectorNumber = "1240")
@CardRegistration(set = "SLX", collectorNumber = "27")
public class JurinLeadingTheCharge extends Card {

    public JurinLeadingTheCharge() {
        addEffect(EffectSlot.STATIC, new MustBeBlockedIfAbleEffect());

        PermanentCount defendingPlayerCreatures =
                new PermanentCount(new PermanentIsCreaturePredicate(), CountScope.DEFENDING_PLAYER);
        addEffect(EffectSlot.ON_ATTACK, new BoostAllOwnCreaturesEffect(
                defendingPlayerCreatures,
                new Fixed(0),
                new PermanentIsAttackingPredicate()));
    }
}

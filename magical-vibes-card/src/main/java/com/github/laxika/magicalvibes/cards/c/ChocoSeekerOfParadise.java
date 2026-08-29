package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.condition.HasAttacker;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsChooseOneToHandThenLandsToBattlefieldTappedEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsAttackingPredicate;

import java.util.List;

@CardRegistration(set = "FIN", collectorNumber = "215")
@CardRegistration(set = "FIN", collectorNumber = "479")
@CardRegistration(set = "FIN", collectorNumber = "569")
public class ChocoSeekerOfParadise extends Card {

    public ChocoSeekerOfParadise() {
        PermanentAllOfPredicate birdAttacker = new PermanentAllOfPredicate(List.of(
                new PermanentIsAttackingPredicate(),
                new PermanentHasSubtypePredicate(CardSubtype.BIRD)));

        addEffect(EffectSlot.ON_ALLY_CREATURES_ATTACK,
                new ConditionalEffect(new HasAttacker(
                        new PermanentHasSubtypePredicate(CardSubtype.BIRD)),
                        new LookAtTopCardsChooseOneToHandThenLandsToBattlefieldTappedEffect(
                                new PermanentCount(birdAttacker, CountScope.CONTROLLER))));
        addEffect(EffectSlot.ON_ALLY_LAND_ENTERS_BATTLEFIELD, new BoostSelfEffect(1, 0));
    }
}

package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.CrewCost;
import com.github.laxika.magicalvibes.model.effect.AnimatePermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

import java.util.List;

@CardRegistration(set = "BLC", collectorNumber = "39")
@CardRegistration(set = "BLC", collectorNumber = "71")
public class RollingHamsphere extends Card {

    public RollingHamsphere() {
        addActivatedAbility(new ActivatedAbility(false, null,
                List.of(new CrewCost(3), AnimatePermanentsEffect.crew()), "Crew 3"));
        PermanentCount hamsters = new PermanentCount(
                new PermanentHasSubtypePredicate(CardSubtype.HAMSTER), CountScope.CONTROLLER);
        addEffect(EffectSlot.STATIC, new BoostSelfEffect(hamsters, hamsters));

        addEffect(EffectSlot.ON_ATTACK, SequenceEffect.of(
                new CreateTokenEffect(3, "Hamster", 1, 1, CardColor.RED,
                        List.of(CardSubtype.HAMSTER), false),
                new DealDamageToAnyTargetEffect(hamsters)));
    }
}

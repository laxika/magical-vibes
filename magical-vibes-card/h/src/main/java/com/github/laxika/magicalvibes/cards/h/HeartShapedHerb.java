package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BecomeMonarchEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PreventDamageFromOpponentSourcesEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnSacrificedPermanentToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentThenEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.List;

@CardRegistration(set = "MSC", collectorNumber = "105")
@CardRegistration(set = "MSC", collectorNumber = "435")
public class HeartShapedHerb extends Card {

    public HeartShapedHerb() {
        addEffect(EffectSlot.STATIC, new PreventDamageFromOpponentSourcesEffect(1));

        addActivatedAbility(new ActivatedAbility(
                true,
                "{2}",
                List.of(
                        new SacrificeSelfCost(),
                        new MayEffect(
                                new SacrificePermanentThenEffect(
                                        new PermanentIsCreaturePredicate(),
                                        SequenceEffect.of(
                                                new ReturnSacrificedPermanentToBattlefieldEffect(
                                                        CounterType.PLUS_ONE_PLUS_ONE, 3),
                                                new BecomeMonarchEffect()),
                                        "a creature",
                                        false,
                                        false),
                                "Sacrifice a creature?")),
                "{2}, {T}, Sacrifice this artifact: You may sacrifice a creature. If you do, return that card to the battlefield under its owner's control with three +1/+1 counters on it and you become the monarch."
        ));
    }
}

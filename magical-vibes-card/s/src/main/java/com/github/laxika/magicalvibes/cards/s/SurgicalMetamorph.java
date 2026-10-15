package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.condition.ControllerIsNotStartingPlayer;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CopyPermanentOnEnterEffect;
import com.github.laxika.magicalvibes.model.effect.ReduceOwnCastCostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentTruePredicate;

import java.util.Set;

@CardRegistration(set = "YONE", collectorNumber = "6")
public class SurgicalMetamorph extends Card {

    public SurgicalMetamorph() {
        // If you weren't the starting player, this spell costs {1} less to cast.
        addEffect(EffectSlot.STATIC, new ConditionalEffect(
                new ControllerIsNotStartingPlayer(), new ReduceOwnCastCostEffect(new Fixed(1))));

        // You may have Surgical Metamorph enter as a copy of any permanent on the battlefield,
        // except it's an artifact in addition to its other types.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new CopyPermanentOnEnterEffect(
                new PermanentTruePredicate(), "permanent", null, null, Set.of(CardType.ARTIFACT)));
    }
}

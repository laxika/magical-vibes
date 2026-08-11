package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.PayLifeCost;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "M20", collectorNumber = "122")
public class VilisBrokerOfBlood extends Card {

    public VilisBrokerOfBlood() {
        // {B}, Pay 2 life: Target creature gets -1/-1 until end of turn.
        addActivatedAbility(new ActivatedAbility(false, "{B}",
                List.of(new PayLifeCost(2), new BoostTargetCreatureEffect(-1, -1)),
                "{B}, Pay 2 life: Target creature gets -1/-1 until end of turn.",
                new PermanentPredicateTargetFilter(new PermanentIsCreaturePredicate(),
                        "Target must be a creature")));

        // Whenever you lose life, draw that many cards.
        addEffect(EffectSlot.ON_CONTROLLER_LOSES_LIFE, new DrawCardEffect(new EventValue()));
    }
}

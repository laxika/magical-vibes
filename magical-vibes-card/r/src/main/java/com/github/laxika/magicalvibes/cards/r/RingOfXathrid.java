package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnReferencedPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.RegenerateEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentColorInPredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "M13", collectorNumber = "215")
public class RingOfXathrid extends Card {

    public RingOfXathrid() {
        // {2}: Regenerate equipped creature.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}",
                List.of(new RegenerateEffect()),
                "{2}: Regenerate equipped creature."
        ));

        // At the beginning of your upkeep, put a +1/+1 counter on equipped creature if it's black.
        addTrigger(Triggers.upkeep(ActivePlayer.YOU), new PutCounterOnReferencedPermanentEffect(
                CounterType.PLUS_ONE_PLUS_ONE, 1, new PermanentColorInPredicate(Set.of(CardColor.BLACK))));

        // Equip {1}
        addActivatedAbility(new EquipActivatedAbility("{1}"));
    }
}

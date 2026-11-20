package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.filter.PermanentColorInPredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "M13", collectorNumber = "211")
public class RingOfEvosIsle extends Card {

    public RingOfEvosIsle() {
        // {2}: Equipped creature gains hexproof until end of turn.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}",
                List.of(new BoostSelfEffect(Subjects.attached(), 0, 0, Keyword.HEXPROOF)),
                "{2}: Equipped creature gains hexproof until end of turn."
        ));

        // At the beginning of your upkeep, put a +1/+1 counter on equipped creature if it's blue.
        addTrigger(Triggers.upkeep(ActivePlayer.YOU), new PutCounterOnTargetPermanentEffect(
                Subjects.attached(), CounterType.PLUS_ONE_PLUS_ONE, new Fixed(1), new PermanentColorInPredicate(Set.of(CardColor.BLUE))));

        // Equip {1}
        addActivatedAbility(new EquipActivatedAbility("{1}"));
    }
}

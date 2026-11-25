package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.amount.CountersOnSource;
import com.github.laxika.magicalvibes.model.effect.PutCountersEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.filter.CardColorPredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import com.github.laxika.magicalvibes.model.effect.Subjects;

@CardRegistration(set = "NPH", collectorNumber = "154")
public class ShrineOfLimitlessPower extends Card {

    public ShrineOfLimitlessPower() {
        // At the beginning of your upkeep, put a charge counter on Shrine of Limitless Power.
        addTrigger(Triggers.upkeep(ActivePlayer.YOU), PutCountersEffect.onSelf(CounterType.CHARGE));

        // Whenever you cast a black spell, put a charge counter on Shrine of Limitless Power.
        addTrigger(Triggers.youCastSpell().withFilter(new CardColorPredicate(CardColor.BLACK)),
                PutCountersEffect.onSelf(CounterType.CHARGE));

        // {4}, {T}, Sacrifice Shrine of Limitless Power: Target player discards a card for each charge counter on Shrine of Limitless Power.
        addActivatedAbility(new ActivatedAbility(
                true,
                "{4}",
                List.of(new SacrificeSelfCost(), new DiscardEffect(new CountersOnSource(CounterType.CHARGE), Subjects.targetPlayer())),
                "{4}, {T}, Sacrifice Shrine of Limitless Power: Target player discards a card for each charge counter on Shrine of Limitless Power."
        ));
    }
}

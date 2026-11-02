package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.amount.CountersOnSource;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.CardColorPredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "NPH", collectorNumber = "152")
public class ShrineOfBoundlessGrowth extends Card {

    public ShrineOfBoundlessGrowth() {
        // At the beginning of your upkeep, put a charge counter on Shrine of Boundless Growth.
        addTrigger(Triggers.upkeep(ActivePlayer.YOU), new PutCountersOnSelfEffect(CounterType.CHARGE));

        // Whenever you cast a green spell, put a charge counter on Shrine of Boundless Growth.
        addTrigger(Triggers.youCastSpell(),
                new SpellCastTriggerEffect(new CardColorPredicate(CardColor.GREEN),
                        List.of(new PutCountersOnSelfEffect(CounterType.CHARGE))));

        // {T}, Sacrifice Shrine of Boundless Growth: Add {C} for each charge counter on Shrine of Boundless Growth.
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new SacrificeSelfCost(),
                        new AwardManaEffect(ManaColor.COLORLESS, new CountersOnSource(CounterType.CHARGE))),
                "{T}, Sacrifice Shrine of Boundless Growth: Add {C} for each charge counter on Shrine of Boundless Growth."
        ));
    }
}

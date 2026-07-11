package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountersOnSource;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsEffect;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.CardColorPredicate;

import java.util.List;

@CardRegistration(set = "NPH", collectorNumber = "156")
public class ShrineOfPiercingVision extends Card {

    public ShrineOfPiercingVision() {
        // At the beginning of your upkeep, put a charge counter on Shrine of Piercing Vision.
        addEffect(EffectSlot.UPKEEP_TRIGGERED, new PutCountersOnSelfEffect(CounterType.CHARGE));

        // Whenever you cast a blue spell, put a charge counter on Shrine of Piercing Vision.
        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL,
                new SpellCastTriggerEffect(new CardColorPredicate(CardColor.BLUE),
                        List.of(new PutCountersOnSelfEffect(CounterType.CHARGE))));

        // {T}, Sacrifice Shrine of Piercing Vision: Look at the top X cards of your library,
        // where X is the number of charge counters on Shrine of Piercing Vision.
        // Put one of those cards into your hand and the rest on the bottom of your library in any order.
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new SacrificeSelfCost(),
                        LookAtTopCardsEffect.chooseOneToHandRestOnBottom(new CountersOnSource(CounterType.CHARGE))),
                "{T}, Sacrifice Shrine of Piercing Vision: Look at the top X cards of your library, where X is the number of charge counters on Shrine of Piercing Vision. Put one of those cards into your hand and the rest on the bottom of your library in any order."
        ));
    }
}

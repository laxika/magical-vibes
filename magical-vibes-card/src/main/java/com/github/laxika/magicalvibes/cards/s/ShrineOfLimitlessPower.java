package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PutChargeCounterOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerDiscardsByChargeCountersEffect;
import com.github.laxika.magicalvibes.model.filter.CardColorPredicate;

import java.util.List;

@CardRegistration(set = "NPH", collectorNumber = "154")
public class ShrineOfLimitlessPower extends Card {

    public ShrineOfLimitlessPower() {
        // At the beginning of your upkeep, put a charge counter on Shrine of Limitless Power.
        addEffect(EffectSlot.UPKEEP_TRIGGERED, new PutChargeCounterOnSelfEffect());

        // Whenever you cast a black spell, put a charge counter on Shrine of Limitless Power.
        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL,
                new SpellCastTriggerEffect(new CardColorPredicate(CardColor.BLACK),
                        List.of(new PutChargeCounterOnSelfEffect())));

        // {4}, {T}, Sacrifice Shrine of Limitless Power: Target player discards a card for each charge counter on Shrine of Limitless Power.
        addActivatedAbility(new ActivatedAbility(
                true,
                "{4}",
                List.of(new SacrificeSelfCost(), new TargetPlayerDiscardsByChargeCountersEffect()),
                "{4}, {T}, Sacrifice Shrine of Limitless Power: Target player discards a card for each charge counter on Shrine of Limitless Power."
        ));
    }
}

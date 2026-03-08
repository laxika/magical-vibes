package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokensEqualToChargeCountersOnSourceEffect;
import com.github.laxika.magicalvibes.model.effect.PutChargeCounterOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.CardColorPredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "NPH", collectorNumber = "155")
public class ShrineOfLoyalLegions extends Card {

    public ShrineOfLoyalLegions() {
        // At the beginning of your upkeep, put a charge counter on Shrine of Loyal Legions.
        addEffect(EffectSlot.UPKEEP_TRIGGERED, new PutChargeCounterOnSelfEffect());

        // Whenever you cast a white spell, put a charge counter on Shrine of Loyal Legions.
        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL,
                new SpellCastTriggerEffect(new CardColorPredicate(CardColor.WHITE),
                        List.of(new PutChargeCounterOnSelfEffect())));

        // {3}, {T}, Sacrifice Shrine of Loyal Legions: Create a 1/1 colorless Phyrexian Myr
        // artifact creature token for each charge counter on Shrine of Loyal Legions.
        addActivatedAbility(new ActivatedAbility(
                true,
                "{3}",
                List.of(
                        new SacrificeSelfCost(),
                        new CreateTokensEqualToChargeCountersOnSourceEffect(
                                "Myr", 1, 1, null,
                                List.of(CardSubtype.PHYREXIAN, CardSubtype.MYR),
                                Set.of(), Set.of(CardType.ARTIFACT))
                ),
                "{3}, {T}, Sacrifice Shrine of Loyal Legions: Create a 1/1 colorless Phyrexian Myr artifact creature token for each charge counter on Shrine of Loyal Legions."
        ));
    }
}

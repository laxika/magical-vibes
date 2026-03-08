package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AddColorlessManaPerChargeCounterOnSourceEffect;
import com.github.laxika.magicalvibes.model.effect.PutChargeCounterOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.CardColorPredicate;

import java.util.List;

@CardRegistration(set = "NPH", collectorNumber = "152")
public class ShrineOfBoundlessGrowth extends Card {

    public ShrineOfBoundlessGrowth() {
        // At the beginning of your upkeep, put a charge counter on Shrine of Boundless Growth.
        addEffect(EffectSlot.UPKEEP_TRIGGERED, new PutChargeCounterOnSelfEffect());

        // Whenever you cast a green spell, put a charge counter on Shrine of Boundless Growth.
        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL,
                new SpellCastTriggerEffect(new CardColorPredicate(CardColor.GREEN),
                        List.of(new PutChargeCounterOnSelfEffect())));

        // {T}, Sacrifice Shrine of Boundless Growth: Add {C} for each charge counter on Shrine of Boundless Growth.
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new SacrificeSelfCost(), new AddColorlessManaPerChargeCounterOnSourceEffect()),
                "{T}, Sacrifice Shrine of Boundless Growth: Add {C} for each charge counter on Shrine of Boundless Growth."
        ));
    }
}

package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardCardTypeCost;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;

import java.util.List;

@CardRegistration(set = "GPT", collectorNumber = "115")
public class GlintEyeNephilim extends Card {

    public GlintEyeNephilim() {
        addEffect(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER, new DrawCardEffect(new EventValue()));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}",
                List.of(
                        new DiscardCardTypeCost(null, null),
                        new BoostSelfEffect(1, 1)
                ),
                "{1}, Discard a card: This creature gets +1/+1 until end of turn."
        ));
    }
}

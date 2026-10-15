package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantProtectionFromCardTypeUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "SOM", collectorNumber = "129")
public class TelJiladDefiance extends Card {

    public TelJiladDefiance() {
        target(TargetFilters.creature()).addEffect(EffectSlot.SPELL, new GrantProtectionFromCardTypeUntilEndOfTurnEffect(CardType.ARTIFACT))
                .addEffect(EffectSlot.SPELL, new DrawCardEffect());
    }
}

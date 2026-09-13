package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.condition.Equipped;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;

@CardRegistration(set = "ZNR", collectorNumber = "139")
public class FirebladeCharger extends Card {

    public FirebladeCharger() {
        addEffect(EffectSlot.STATIC, new ConditionalEffect(
                new Equipped(), new GrantKeywordEffect(Keyword.HASTE, GrantScope.SELF)));
        addEffect(EffectSlot.ON_DEATH, new DealDamageToAnyTargetEffect(new EventValue()));
    }
}

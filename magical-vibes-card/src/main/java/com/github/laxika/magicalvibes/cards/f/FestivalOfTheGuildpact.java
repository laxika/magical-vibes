package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.PreventDamageEffect;
import com.github.laxika.magicalvibes.model.effect.PreventionScope;

@CardRegistration(set = "RAV", collectorNumber = "17")
public class FestivalOfTheGuildpact extends Card {

    public FestivalOfTheGuildpact() {
        addEffect(EffectSlot.SPELL, new PreventDamageEffect(
                PreventionScope.NEXT_TO_CONTROLLER, new XValue(), false, null, null, null));
        addEffect(EffectSlot.SPELL, new DrawCardEffect());
    }
}

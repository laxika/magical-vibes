package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasCountersPredicate;

@CardRegistration(set = "NCC", collectorNumber = "12")
@CardRegistration(set = "NCC", collectorNumber = "113")
@CardRegistration(set = "FDC", collectorNumber = "16")
public class AngelicSleuth extends Card {

    public AngelicSleuth() {
        addEffect(EffectSlot.ON_ALLY_PERMANENT_LEAVES_BATTLEFIELD,
                new TriggeringPermanentConditionalEffect(
                        new PermanentHasCountersPredicate(CounterType.ANY),
                        CreateTokenEffect.ofClueToken(1)));
    }
}

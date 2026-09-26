package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.effect.DealDamageToRandomOpponentOrPlaneswalkerEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToRandomOpponentOrTheirPlaneswalkerEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import java.util.List;

@CardRegistration(set = "SLD", collectorNumber = "1202")
@CardRegistration(set = "FCA", collectorNumber = "59")
public class VialSmasherTheFierce extends Card {

    public VialSmasherTheFierce() {
        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL,
                SpellCastTriggerEffect.nth(1, null,
                        List.of(new DealDamageToRandomOpponentOrTheirPlaneswalkerEffect(new EventValue()))));
    }
}

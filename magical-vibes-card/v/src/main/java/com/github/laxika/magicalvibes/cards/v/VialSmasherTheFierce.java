package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.effect.DealDamageToRandomOpponentOrPlaneswalkerEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToRandomOpponentOrTheirPlaneswalkerEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;
import java.util.List;

@CardRegistration(set = "SLD", collectorNumber = "1202")
@CardRegistration(set = "FCA", collectorNumber = "59")
@CardRegistration(set = "DSC", collectorNumber = "239")
@CardRegistration(set = "C16", collectorNumber = "49")
public class VialSmasherTheFierce extends Card {

    public VialSmasherTheFierce() {
        addTrigger(Triggers.youCastSpell(),
                SpellCastTriggerEffect.nth(1, null,
                        List.of(new DealDamageToRandomOpponentOrTheirPlaneswalkerEffect(new EventValue()))));
    }
}

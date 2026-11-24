package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CopyTriggeringSpellEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterDelayedControllerSpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "TDM", collectorNumber = "185")
public class FlameholdGrappler extends Card {

    public FlameholdGrappler() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), RegisterDelayedControllerSpellCastTriggerEffect.nextThisTurn(
                null, List.of(new CopyTriggeringSpellEffect().permanentSpellCopiesAreTokens())));
    }
}

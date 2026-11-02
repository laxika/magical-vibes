package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.ControllerIsMonarch;
import com.github.laxika.magicalvibes.model.effect.BecomeMonarchEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalReplacementEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "LTC", collectorNumber = "213")
public class CourtOfIre extends Card {

    public CourtOfIre() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), new BecomeMonarchEffect());
        addTrigger(Triggers.upkeep(ActivePlayer.YOU), new ConditionalReplacementEffect(
                new ControllerIsMonarch(),
                new DealDamageToAnyTargetEffect(2),
                new DealDamageToAnyTargetEffect(7)));
    }
}

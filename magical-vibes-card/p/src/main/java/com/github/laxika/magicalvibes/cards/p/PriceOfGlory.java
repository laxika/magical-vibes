package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.condition.TargetPlayerTurn;
import com.github.laxika.magicalvibes.model.effect.DestroyReferencedPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.PermanentReference;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ODY", collectorNumber = "214")
@CardRegistration(set = "SLD", collectorNumber = "1525")
public class PriceOfGlory extends Card {

    public PriceOfGlory() {
        addTrigger(Triggers.playerTapsLandForMana(),
                TriggerOptions.DEFAULT.withInterveningIf(new NotCondition(new TargetPlayerTurn())),
                new DestroyReferencedPermanentEffect(PermanentReference.TRIGGERING));
    }
}

package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.Raid;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "FDN", collectorNumber = "60")
public class GutlessPlunderer extends Card {

    public GutlessPlunderer() {
        // Raid — When this creature enters, if you attacked this turn, look at the top three cards
        // of your library. You may put one back on top, then put the rest into your graveyard.
        addTrigger(Triggers.enters(ObjectRelation.THIS),
                TriggerOptions.DEFAULT.withInterveningIf(new Raid()),
                LookAtTopCardsEffect.mayPutOneOnTopRestToGraveyard(3));
    }
}

package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.SkipKind;
import com.github.laxika.magicalvibes.model.effect.SkipNextEffect;
import com.github.laxika.magicalvibes.model.effect.SkipRecipient;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "TSP", collectorNumber = "50")
@CardRegistration(set = "DD2", collectorNumber = "18")
@CardRegistration(set = "JVC", collectorNumber = "18")
@CardRegistration(set = "A25", collectorNumber = "47")
@CardRegistration(set = "TSR", collectorNumber = "55")
@CardRegistration(set = "C14", collectorNumber = "99")
public class BrineElemental extends Card {

    public BrineElemental() {
        addMorph("{5}{U}{U}");
        addTrigger(Triggers.turnedFaceUp(null, ObjectRelation.THIS),
                new SkipNextEffect(SkipKind.UNTAP_STEP, SkipRecipient.EACH_OPPONENT));
    }
}

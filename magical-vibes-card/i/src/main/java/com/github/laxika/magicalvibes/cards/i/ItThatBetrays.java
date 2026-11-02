package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ReturnSacrificedCardUnderControlEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeRecipient;
import com.github.laxika.magicalvibes.model.filter.PermanentTruePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ROE", collectorNumber = "7")
@CardRegistration(set = "DDP", collectorNumber = "43")
@CardRegistration(set = "SLD", collectorNumber = "1176")
public class ItThatBetrays extends Card {

    public ItThatBetrays() {
        // Annihilator 2.
        addTrigger(Triggers.attacks(), new SacrificePermanentsEffect(
                2, new PermanentTruePredicate(), SacrificeRecipient.DEFENDING_PLAYER));
        addTrigger(Triggers.sacrificed(Triggers.NONTOKEN, ObjectRelation.OPPONENT_CONTROLS),
                new ReturnSacrificedCardUnderControlEffect());
    }
}

package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeRecipient;
import com.github.laxika.magicalvibes.model.filter.PermanentTruePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "SOK", collectorNumber = "19")
@CardRegistration(set = "SLD", collectorNumber = "259")
public class MichikoKondaTruthSeeker extends Card {

    public MichikoKondaTruthSeeker() {
        addTrigger(Triggers.youAreDealtDamageBySourceOpponentControls(),
                new SacrificePermanentsEffect(1, new PermanentTruePredicate(), SacrificeRecipient.TARGET_PLAYER));
    }
}

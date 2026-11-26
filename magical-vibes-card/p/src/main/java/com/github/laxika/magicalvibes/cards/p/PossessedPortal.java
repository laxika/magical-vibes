package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DiscardCardTypeCost;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.UnlessPaysEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentTruePredicate;
import com.github.laxika.magicalvibes.model.effect.SkipDrawsEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "5DN", collectorNumber = "144")
public class PossessedPortal extends Card {

    public PossessedPortal() {
        addStaticEffect(new SkipDrawsEffect(Subjects.eachPlayer()));
        // At the beginning of each end step, each player sacrifices a permanent of their choice unless they discard a
        // card: every player chooses in APNAP order, then those who didn't discard sacrifice together (CR 101.4).
        addTrigger(Triggers.endStep(ActivePlayer.ANY), UnlessPaysEffect.eachUnless(Subjects.eachPlayer(),
                new DiscardCardTypeCost(null, null),
                players -> new SacrificePermanentsEffect(1, new PermanentTruePredicate(), players)));
    }
}

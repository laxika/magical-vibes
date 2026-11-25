package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.m.MaliciousInvader;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentCost;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.TransformSelfEffect;
import com.github.laxika.magicalvibes.model.effect.UnlessPaysEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "VOW", collectorNumber = "121")
@CardRegistration(set = "DBL", collectorNumber = "388")
public class InnocentTraveler extends Card {

    public InnocentTraveler() {
        setBackFaceCard(new MaliciousInvader());

        addTrigger(Triggers.upkeep(ActivePlayer.YOU), UnlessPaysEffect.unlessPays(Subjects.eachOpponent(),
                new SacrificePermanentCost(new PermanentIsCreaturePredicate(), "creature", false),
                new TransformSelfEffect())
                .withPrompt("Sacrifice a creature? If no one does, it transforms.")
                .payingTogether());
    }

    @Override
    public String getBackFaceClassName() {
        return "MaliciousInvader";
    }
}

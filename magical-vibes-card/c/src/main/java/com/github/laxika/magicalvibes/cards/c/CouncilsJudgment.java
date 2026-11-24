package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.VoteEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

@CardRegistration(set = "VMA", collectorNumber = "20")
@CardRegistration(set = "SLD", collectorNumber = "1635")
@CardRegistration(set = "2XM", collectorNumber = "11")
@CardRegistration(set = "2XM", collectorNumber = "336")
@CardRegistration(set = "OTC", collectorNumber = "79")
public class CouncilsJudgment extends Card {

    public CouncilsJudgment() {
        addSpellEffect(VoteEffect.forPermanents(Subjects.controlledBy(Subjects.eachOpponent(),
                new PermanentNotPredicate(new PermanentIsLandPredicate()))));
        addSpellEffect(new ExileTargetPermanentEffect(Subjects.mostVoted()));
    }
}

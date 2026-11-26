package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PermanentIsAttackingPredicate;

@CardRegistration(set = "MBS", collectorNumber = "73")
public class RallyTheForces extends Card {

    public RallyTheForces() {
        // Attacking creatures get +1/+0 and gain first strike until end of turn.
        addSpellEffect(BoostEffect.allCreatures(1, 0, new PermanentIsAttackingPredicate()));
        addSpellEffect(new GrantKeywordEffect(Keyword.FIRST_STRIKE, Subjects.allCreatures(), new PermanentIsAttackingPredicate()));
    }
}

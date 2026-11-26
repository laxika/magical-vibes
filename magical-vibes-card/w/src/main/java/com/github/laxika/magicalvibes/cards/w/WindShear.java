package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasKeywordPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsAttackingPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

import java.util.List;

@CardRegistration(set = "VIS", collectorNumber = "125")
public class WindShear extends Card {

    public WindShear() {
        // Attacking creatures with flying get -2/-2 and lose flying until end of turn.
        PermanentPredicate attackingWithFlying = new PermanentAllOfPredicate(List.of(
                new PermanentIsAttackingPredicate(),
                new PermanentHasKeywordPredicate(Keyword.FLYING)));
        addSpellEffect(BoostEffect.allCreatures(-2, -2, attackingWithFlying));
        addSpellEffect(new RemoveKeywordEffect(Keyword.FLYING, Subjects.allCreatures(), attackingWithFlying));
    }
}

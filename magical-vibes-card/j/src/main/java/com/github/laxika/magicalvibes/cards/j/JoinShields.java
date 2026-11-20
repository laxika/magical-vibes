package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.Set;

@CardRegistration(set = "GRN", collectorNumber = "181")
public class JoinShields extends Card {

    public JoinShields() {
        // Untap all creatures you control.
        addSpellEffect(new UntapPermanentsEffect(Subjects.controlledBy(Subjects.you(),
                new PermanentIsCreaturePredicate())));

        // Creatures you control gain hexproof and indestructible until end of turn.
        addSpellEffect(new GrantKeywordEffect(
                Set.of(Keyword.HEXPROOF, Keyword.INDESTRUCTIBLE), Subjects.otherCreaturesYouControl()));
    }
}

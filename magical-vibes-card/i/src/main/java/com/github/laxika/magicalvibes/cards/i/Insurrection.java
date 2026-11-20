package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.model.effect.GainControlOfAllPermanentsMatchingEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import java.util.List;

@CardRegistration(set = "ONS", collectorNumber = "213")
@CardRegistration(set = "HOP", collectorNumber = "57")
@CardRegistration(set = "TLE", collectorNumber = "31")
@CardRegistration(set = "CMD", collectorNumber = "126")
@CardRegistration(set = "CMM", collectorNumber = "236")
@CardRegistration(set = "CMM", collectorNumber = "542")
@CardRegistration(set = "CMM", collectorNumber = "699")
public class Insurrection extends Card {

    public Insurrection() {
        PermanentIsCreaturePredicate creature = new PermanentIsCreaturePredicate();

        // Untap all creatures and gain control of them until end of turn.
        addSpellEffect(new UntapPermanentsEffect(Subjects.controlledBy(Subjects.eachPlayer(),
                new PermanentAllOfPredicate(List.of(new PermanentIsCreaturePredicate(), creature)))));
        addSpellEffect(new GainControlOfAllPermanentsMatchingEffect(creature, Duration.UNTIL_END_OF_TURN));

        // They gain haste until end of turn.
        addSpellEffect(new GrantKeywordEffect(Keyword.HASTE, Subjects.otherCreaturesYouControl()));
    }
}

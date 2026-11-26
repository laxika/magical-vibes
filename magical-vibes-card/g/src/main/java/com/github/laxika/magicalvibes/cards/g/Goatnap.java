package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.TargetPermanentMatches;
import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.model.effect.GainControlOfTargetEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "ECL", collectorNumber = "142")
@CardRegistration(set = "MH1", collectorNumber = "126")
public class Goatnap extends Card {

    public Goatnap() {
        target(TargetFilters.creature())
                .addSpellEffect(new GainControlOfTargetEffect(Duration.UNTIL_END_OF_TURN))
                .addSpellEffect(new UntapPermanentsEffect(Subjects.target()))
                .addSpellEffect(new GrantKeywordEffect(Keyword.HASTE, Subjects.target()))
                .addSpellEffect(new ConditionalEffect(
                        new TargetPermanentMatches(new PermanentHasSubtypePredicate(CardSubtype.GOAT)),
                        BoostEffect.target(3, 0)));
    }
}

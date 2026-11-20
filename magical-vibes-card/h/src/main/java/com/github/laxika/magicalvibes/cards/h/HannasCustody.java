package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;

@CardRegistration(set = "TMP", collectorNumber = "22")
public class HannasCustody extends Card {

    public HannasCustody() {
        // All artifacts have shroud — every artifact on any battlefield, creature or not.
        addStaticEffect(new GrantKeywordEffect(Keyword.SHROUD, Subjects.otherPermanents(),
                new PermanentIsArtifactPredicate()));
        // ALL_PERMANENTS skips the source, so the Custody itself gets shroud through this grant if it is an artifact.
        addStaticEffect(new GrantKeywordEffect(Keyword.SHROUD, Subjects.source(),
                new PermanentIsArtifactPredicate()));
    }
}

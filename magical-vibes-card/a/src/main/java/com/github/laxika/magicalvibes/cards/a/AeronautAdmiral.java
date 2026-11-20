package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

@CardRegistration(set = "AER", collectorNumber = "2")
@CardRegistration(set = "KLR", collectorNumber = "2")
@CardRegistration(set = "NEC", collectorNumber = "79")
public class AeronautAdmiral extends Card {

    public AeronautAdmiral() {
        addStaticEffect(new GrantKeywordEffect(
                Keyword.FLYING,
                Subjects.otherPermanentsYouControl(),
                new PermanentHasSubtypePredicate(CardSubtype.VEHICLE)));
    }
}

package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

public class NezumiRoadCaptain extends Card {

    public NezumiRoadCaptain() {
        addStaticEffect(new GrantKeywordEffect(
                Keyword.MENACE,
                Subjects.otherPermanentsYouControl(),
                new PermanentHasSubtypePredicate(CardSubtype.VEHICLE)));
    }
}

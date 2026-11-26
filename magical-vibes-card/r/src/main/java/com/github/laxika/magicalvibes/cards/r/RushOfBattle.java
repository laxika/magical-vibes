package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

@CardRegistration(set = "KTK", collectorNumber = "19")
public class RushOfBattle extends Card {

    public RushOfBattle() {
        addSpellEffect(BoostEffect.creaturesYouControl(2, 1));
        addSpellEffect(new GrantKeywordEffect(
                Keyword.LIFELINK,
                Subjects.otherCreaturesYouControl(),
                new PermanentHasSubtypePredicate(CardSubtype.WARRIOR)
        ));
    }
}

package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

@CardRegistration(set = "MSC", collectorNumber = "719")
public class DevilDinosaur extends Card {

    public DevilDinosaur() {
        PermanentHasSubtypePredicate dinosaur = new PermanentHasSubtypePredicate(CardSubtype.DINOSAUR);
        addStaticEffect(new StaticBoostEffect(1, 1, GrantScope.OWN_CREATURES, dinosaur));
        addStaticEffect(new GrantKeywordEffect(Keyword.HEXPROOF, GrantScope.OWN_CREATURES, dinosaur));
    }
}

package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.effect.GrantCardTypeEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;

public class AwakenedSkyclave extends Card {

    public AwakenedSkyclave() {
        addStaticEffect(new GrantCardTypeEffect(CardType.LAND, Subjects.source()));
        addActivatedAbility(ManaAbilities.tapForAnyColor());
    }
}

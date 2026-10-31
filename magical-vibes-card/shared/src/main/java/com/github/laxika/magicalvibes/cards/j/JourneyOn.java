package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.OpponentsControllingPermanentMatching;
import com.github.laxika.magicalvibes.model.amount.Sum;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;

public class JourneyOn extends Card {

    public JourneyOn() {
        addSpellEffect(CreateTokenEffect.ofMapToken(new Sum(
                new Fixed(1),
                new OpponentsControllingPermanentMatching(new PermanentIsArtifactPredicate()))));
    }
}

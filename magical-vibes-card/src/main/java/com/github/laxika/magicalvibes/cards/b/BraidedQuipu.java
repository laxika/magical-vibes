package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.PutSourcePermanentIntoLibraryNFromTopEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;

import java.util.List;

public class BraidedQuipu extends Card {

    public BraidedQuipu() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{3}{U}",
                List.of(
                        new DrawCardEffect(new PermanentCount(
                                new PermanentIsArtifactPredicate(), CountScope.CONTROLLER)),
                        new PutSourcePermanentIntoLibraryNFromTopEffect(2)
                ),
                "{3}{U}, {T}: Draw a card for each artifact you control, then put Braided Quipu "
                        + "into its owner's library third from the top."));
    }
}

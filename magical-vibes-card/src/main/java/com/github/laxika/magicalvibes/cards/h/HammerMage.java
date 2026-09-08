package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DestroyAllPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardCardTypeCost;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentManaValueAtMostXPredicate;

import java.util.List;

@CardRegistration(set = "MMQ", collectorNumber = "193")
public class HammerMage extends Card {

    public HammerMage() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{X}{R}",
                List.of(
                        new DiscardCardTypeCost(null, null),
                        new DestroyAllPermanentsEffect(new PermanentAllOfPredicate(List.of(
                                new PermanentIsArtifactPredicate(),
                                new PermanentManaValueAtMostXPredicate())))
                ),
                "{X}{R}, {T}, Discard a card: Destroy all artifacts with mana value X or less."
        ));
    }
}

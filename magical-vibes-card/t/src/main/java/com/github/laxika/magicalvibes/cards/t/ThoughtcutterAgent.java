package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.RevealTargetHandEffect;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

import java.util.List;

@CardRegistration(set = "ALA", collectorNumber = "201")
public class ThoughtcutterAgent extends Card {

    public ThoughtcutterAgent() {
        // {U}{B}, {T}: Target player loses 1 life and reveals their hand.
        addActivatedAbility(new ActivatedAbility(
                true, "{U}{B}",
                List.of(
                        new LoseLifeEffect(1, LoseLifeRecipient.TARGET_PLAYER),
                        new RevealTargetHandEffect()
                ),
                "{U}{B}, {T}: Target player loses 1 life and reveals their hand.",
                new PlayerPredicateTargetFilter(
                        new PlayerRelationPredicate(PlayerRelation.ANY),
                        "Target must be a player"
                )
        ));
    }
}

package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentAndTrackWithSourceEffect;
import com.github.laxika.magicalvibes.model.effect.KarnRestartGameEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerExilesFromHandEffect;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

import java.util.List;

@CardRegistration(set = "NPH", collectorNumber = "1")
public class KarnLiberated extends Card {

    public KarnLiberated() {
        // +4: Target player exiles a card from their hand.
        addActivatedAbility(new ActivatedAbility(
                +4,
                List.of(new TargetPlayerExilesFromHandEffect(1)),
                "+4: Target player exiles a card from their hand.",
                new PlayerPredicateTargetFilter(
                        new PlayerRelationPredicate(PlayerRelation.ANY),
                        "Must target a player"
                )
        ));

        // −3: Exile target permanent.
        addActivatedAbility(new ActivatedAbility(
                -3,
                List.of(new ExileTargetPermanentAndTrackWithSourceEffect()),
                "\u22123: Exile target permanent."
        ));

        // −14: Restart the game, leaving in exile all non-Aura permanent cards exiled
        // with Karn Liberated. Then put those cards onto the battlefield under your control.
        addActivatedAbility(new ActivatedAbility(
                -14,
                List.of(new KarnRestartGameEffect()),
                "\u221214: Restart the game, leaving in exile all non-Aura permanent cards exiled with Karn Liberated. Then put those cards onto the battlefield under your control."
        ));
    }
}

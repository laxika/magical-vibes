package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardForTargetPlayerEffect;
import com.github.laxika.magicalvibes.model.effect.ExileCardFromHandAndCreateTokenCopyEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTokensCreatedWithSourceEffect;
import com.github.laxika.magicalvibes.model.filter.CardTruePredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "KHC", collectorNumber = "36")
public class ArcaneArtisan extends Card {

    public ArcaneArtisan() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{2}{U}",
                List.of(
                        new DrawCardForTargetPlayerEffect(1),
                        new ExileCardFromHandAndCreateTokenCopyEffect(
                                new CardTruePredicate(),
                                CreateTokenCopyOfTargetPermanentEffect.trackedForTargetController(),
                                new CardTypePredicate(CardType.CREATURE),
                                true)
                ),
                "{2}{U}, {T}: Target player draws a card, then exiles a card from their hand. "
                        + "If a creature card is exiled this way, that player creates a token that's a copy of that card.",
                new PlayerPredicateTargetFilter(
                        new PlayerRelationPredicate(PlayerRelation.ANY),
                        "Target must be a player"
                )
        ));
        addTrigger(Triggers.leavesBattlefield(ObjectRelation.THIS), new ExileTokensCreatedWithSourceEffect(true));
    }
}

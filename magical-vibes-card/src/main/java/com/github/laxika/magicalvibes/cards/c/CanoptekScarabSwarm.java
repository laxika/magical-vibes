package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.ExileGraveyardCardsEffect;
import com.github.laxika.magicalvibes.model.effect.GraveyardExileScope;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

import java.util.List;
import java.util.Map;
import java.util.Set;

@CardRegistration(set = "40K", collectorNumber = "150")
public class CanoptekScarabSwarm extends Card {

    public CanoptekScarabSwarm() {
        CardAnyOfPredicate artifactOrLand = new CardAnyOfPredicate(List.of(
                new CardTypePredicate(CardType.ARTIFACT),
                new CardTypePredicate(CardType.LAND)));
        CreateTokenEffect scarab = new CreateTokenEffect(
                CardType.CREATURE, new EventValue(), "Insect", 1, 1, null, null,
                List.of(CardSubtype.INSECT), Set.of(Keyword.FLYING), Set.of(CardType.ARTIFACT),
                false, false, Map.of(), List.of(), false, false, false, 0, Set.of());

        target(new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.ANY),
                "Target must be a player"
        )).addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, SequenceEffect.of(
                new ExileGraveyardCardsEffect(0, GraveyardExileScope.TARGET_PLAYER_ALL_MATCHING,
                        null, null, false, false, false, artifactOrLand),
                scarab));
    }
}

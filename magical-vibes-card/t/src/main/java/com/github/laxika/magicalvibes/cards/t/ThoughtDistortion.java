package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.CantBeCounteredEffect;
import com.github.laxika.magicalvibes.model.effect.ExileGraveyardCardsEffect;
import com.github.laxika.magicalvibes.model.effect.ExileMatchingCardsFromTargetPlayerHandEffect;
import com.github.laxika.magicalvibes.model.effect.GraveyardExileScope;
import com.github.laxika.magicalvibes.model.effect.RevealTargetHandEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

import java.util.List;

@CardRegistration(set = "M20", collectorNumber = "117")
public class ThoughtDistortion extends Card {

    public ThoughtDistortion() {
        var noncreatureNonland = new CardAllOfPredicate(List.of(
                new CardNotPredicate(new CardTypePredicate(CardType.CREATURE)),
                new CardNotPredicate(new CardTypePredicate(CardType.LAND))));

        addStaticEffect(new CantBeCounteredEffect());
        target(new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.OPPONENT),
                "Target must be an opponent"
        )).addSpellEffect(new RevealTargetHandEffect())
                .addSpellEffect(new ExileMatchingCardsFromTargetPlayerHandEffect(noncreatureNonland))
                .addSpellEffect(new ExileGraveyardCardsEffect(
                        0, GraveyardExileScope.TARGET_PLAYER_ALL_MATCHING, noncreatureNonland));
    }
}

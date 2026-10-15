package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.GraveyardCardThreshold;
import com.github.laxika.magicalvibes.model.effect.RevealTopCardsOfTargetPlayerAndCastInstantOrSorceryEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;
import java.util.List;

@CardRegistration(set = "ORI", collectorNumber = "78")
public class TalentOfTheTelepath extends Card {

    public TalentOfTheTelepath() {
        // Target opponent reveals the top seven cards of their library. You may cast an instant or
        // sorcery spell from among them without paying its mana cost. Then that player puts the rest
        // into their graveyard.
        // Spell mastery — If there are two or more instant and/or sorcery cards in your graveyard,
        // you may cast up to two instant and/or sorcery spells from among the revealed cards instead
        // of one.
        target(new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.OPPONENT),
                "Target must be an opponent"
        )).addEffect(EffectSlot.SPELL, new RevealTopCardsOfTargetPlayerAndCastInstantOrSorceryEffect(
                7,
                new GraveyardCardThreshold(2, new CardAnyOfPredicate(List.of(
                        new CardTypePredicate(CardType.INSTANT),
                        new CardTypePredicate(CardType.SORCERY))))));
    }
}

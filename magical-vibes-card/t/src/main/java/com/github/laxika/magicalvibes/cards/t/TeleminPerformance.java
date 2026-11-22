package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.PutCardsOntoBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.PutExiledCardsIntoOwnersGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.RevealCardsFromTopUntilEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

@CardRegistration(set = "CON", collectorNumber = "35")
public class TeleminPerformance extends Card {

    public TeleminPerformance() {
        var creatureCard = new CardTypePredicate(CardType.CREATURE);
        target(new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.OPPONENT),
                "You must target an opponent."
        ))
                .addSpellEffect(new RevealCardsFromTopUntilEffect(Subjects.targetPlayer(), creatureCard))
                .addSpellEffect(new PutExiledCardsIntoOwnersGraveyardEffect(Subjects.revealedThisWay(new CardNotPredicate(creatureCard))))
                .addSpellEffect(new PutCardsOntoBattlefieldEffect(Subjects.revealedThisWay(creatureCard)).underControlOf(Subjects.you()));
    }
}

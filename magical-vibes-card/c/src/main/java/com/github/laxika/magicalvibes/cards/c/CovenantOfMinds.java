package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.MayChoicePlayer;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardsIntoHandEffect;
import com.github.laxika.magicalvibes.model.effect.PutExiledCardsIntoOwnersGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.RevealTopCardOfLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

@CardRegistration(set = "ALA", collectorNumber = "38")
public class CovenantOfMinds extends Card {

    public CovenantOfMinds() {
        target(new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.OPPONENT),
                "You must target an opponent."
        ))
                .addSpellEffect(RevealTopCardOfLibraryEffect.topCards(Subjects.you(), new Fixed(3)))
                .addSpellEffect(new MayEffect(
                        new PutCardsIntoHandEffect(Subjects.revealedThisWay()),
                        "Put the revealed cards into your opponent's hand? If not, they go to their graveyard"
                                + " and your opponent draws five cards.",
                        SequenceEffect.of(
                                new PutExiledCardsIntoOwnersGraveyardEffect(Subjects.cardsThisWay(StepKind.REVEAL)),
                                new DrawCardEffect(5)),
                        MayChoicePlayer.TARGET_PLAYER));
    }
}

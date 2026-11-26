package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.condition.SourceCardInGraveyard;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.ExileGraveyardCardsEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardsOntoBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.PutExiledCardsIntoOwnersGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.RevealCardsFromTopUntilEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "UDS", collectorNumber = "106")
@CardRegistration(set = "DMR", collectorNumber = "162")
@CardRegistration(set = "C16", collectorNumber = "151")
public class Gamekeeper extends Card {

    public Gamekeeper() {
        var creatureCard = new CardTypePredicate(CardType.CREATURE);
        addTrigger(Triggers.dies(ObjectRelation.THIS), new ConditionalEffect(
                new SourceCardInGraveyard(),
                new MayEffect(
                        SequenceEffect.of(
                                ExileGraveyardCardsEffect.cards(Subjects.sourceCard()),
                                new RevealCardsFromTopUntilEffect(creatureCard),
                                new PutCardsOntoBattlefieldEffect(Subjects.revealedThisWay(creatureCard)),
                                new PutExiledCardsIntoOwnersGraveyardEffect(Subjects.restRevealedThisWay(creatureCard))),
                        "Exile Gamekeeper and reveal cards from the top of your library?")));
    }
}

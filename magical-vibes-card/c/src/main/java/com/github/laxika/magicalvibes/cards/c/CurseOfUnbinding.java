package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.PutCardsOntoBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.PutExiledCardsIntoOwnersGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.RevealCardsFromTopUntilEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MIC", collectorNumber = "12")
@CardRegistration(set = "MIC", collectorNumber = "50")
public class CurseOfUnbinding extends Card {

    public CurseOfUnbinding() {
        var creatureCard = new CardTypePredicate(CardType.CREATURE);
        addTrigger(Triggers.upkeep(ActivePlayer.ENCHANTED_PLAYER),
                new RevealCardsFromTopUntilEffect(Subjects.enchantedPlayer(), creatureCard),
                new PutCardsOntoBattlefieldEffect(Subjects.revealedThisWay(creatureCard)).underControlOf(Subjects.you()),
                new PutExiledCardsIntoOwnersGraveyardEffect(Subjects.restRevealedThisWay(creatureCard)));
    }
}

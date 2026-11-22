package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.amount.AmountThisWay;
import com.github.laxika.magicalvibes.model.condition.HappenedThisWay;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardsIntoHandEffect;
import com.github.laxika.magicalvibes.model.effect.RevealTopCardOfLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "EVE", collectorNumber = "128")
@CardRegistration(set = "C21", collectorNumber = "228")
public class SaplingOfColfenor extends Card {

    public SaplingOfColfenor() {
        var creatureCard = new CardTypePredicate(CardType.CREATURE);
        // Whenever this creature attacks, reveal the top card of your library. If it's a creature
        // card, you gain life equal to that card's toughness, lose life equal to its power, then
        // put it into your hand. (Indestructible is auto-loaded from Scryfall.)
        addTrigger(Triggers.attacks(), new RevealTopCardOfLibraryEffect(Subjects.you()),
                new ConditionalEffect(HappenedThisWay.toACard(StepKind.REVEAL, creatureCard),
                        SequenceEffect.of(new GainLifeEffect(AmountThisWay.revealedToughness(creatureCard)),
                                new LoseLifeEffect(AmountThisWay.revealedPower(creatureCard), Subjects.you()),
                                new PutCardsIntoHandEffect(Subjects.revealedThisWay(creatureCard)))));
    }
}

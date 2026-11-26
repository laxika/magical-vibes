package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.effect.UnlessPaysEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.condition.HappenedThisWay;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.PutCardsOntoBattlefieldEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "INV", collectorNumber = "227")
public class AetherRift extends Card {

    public AetherRift() {
        // At the beginning of your upkeep, discard a card at random. If you discard a creature card
        // this way, return it from your graveyard to the battlefield unless any player pays 5 life.
        addTrigger(Triggers.upkeep(ActivePlayer.YOU),
                SequenceEffect.of(
                        new DiscardEffect(1, Subjects.you(), true),
                        new ConditionalEffect(HappenedThisWay.toACard(StepKind.DISCARD, new CardTypePredicate(CardType.CREATURE)),
                                UnlessPaysEffect.unlessPaysLife(Subjects.eachPlayer(), 5,
                                        new PutCardsOntoBattlefieldEffect(
                                                Subjects.inYourGraveyard(Subjects.cardsThisWay(StepKind.DISCARD)))))));
    }
}

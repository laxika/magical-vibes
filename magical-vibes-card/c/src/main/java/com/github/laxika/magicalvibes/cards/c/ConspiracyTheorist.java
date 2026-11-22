package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.effect.AllowPlayCardsFromExileEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseCardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardAndDrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileGraveyardCardsEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "STX", collectorNumber = "94")
@CardRegistration(set = "SOC", collectorNumber = "240")
public class ConspiracyTheorist extends Card {

    public ConspiracyTheorist() {
        addTrigger(Triggers.attacks(), new MayPayManaEffect(
                "{1}", new DiscardAndDrawCardEffect(), "Pay {1} and discard a card to draw a card?"));
        addTrigger(Triggers.youDiscardOneOrMoreCards(), new MayEffect(
                SequenceEffect.of(
                        ChooseCardEffect.oneOf(
                                Subjects.triggeringCards(new CardNotPredicate(new CardTypePredicate(CardType.LAND))),
                                "one of the discarded nonland cards to exile"),
                        ExileGraveyardCardsEffect.cardsWithSource(Subjects.cardsThisWay(StepKind.CHOOSE)),
                        AllowPlayCardsFromExileEffect.thisTurn(Subjects.cardsThisWay(StepKind.EXILE))),
                "Exile one of the discarded nonland cards?"));
    }
}

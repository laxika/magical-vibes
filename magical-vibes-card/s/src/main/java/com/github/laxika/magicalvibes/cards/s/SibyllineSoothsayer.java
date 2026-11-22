package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.effect.ExileGraveyardCardsEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardsIntoLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.PutTimeCountersAndGainSuspendEffect;
import com.github.laxika.magicalvibes.model.effect.RevealCardsFromTopUntilEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardMinManaValuePredicate;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;
import java.util.List;

@CardRegistration(set = "WHO", collectorNumber = "95")
@CardRegistration(set = "WHO", collectorNumber = "700")
public class SibyllineSoothsayer extends Card {

    public SibyllineSoothsayer() {
        var match = new CardAllOfPredicate(List.of(
                new CardNotPredicate(new CardTypePredicate(CardType.LAND)), new CardMinManaValuePredicate(3)));
        addTrigger(Triggers.enters(ObjectRelation.THIS),
                new RevealCardsFromTopUntilEffect(match),
                ExileGraveyardCardsEffect.cards(Subjects.revealedThisWay(match)),
                new PutTimeCountersAndGainSuspendEffect(Subjects.cardsThisWay(StepKind.EXILE), 3),
                PutCardsIntoLibraryEffect.onBottomInRandomOrder(Subjects.restRevealedThisWay(match)));
    }
}

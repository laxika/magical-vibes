package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.condition.HappenedThisWay;
import com.github.laxika.magicalvibes.model.effect.ChooseCardEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardMayPlayThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardsIntoLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "STX", collectorNumber = "92")
public class ArdentDustspeaker extends Card {

    public ArdentDustspeaker() {
        addTrigger(Triggers.attacks(),
                ChooseCardEffect.upTo(
                        Subjects.cardsInYourGraveyard(new CardAnyOfPredicate(List.of(
                                new CardTypePredicate(CardType.INSTANT),
                                new CardTypePredicate(CardType.SORCERY)))),
                        1, "an instant or sorcery card from your graveyard to put on the bottom of your library"),
                PutCardsIntoLibraryEffect.onBottom(Subjects.cardsThisWay(StepKind.CHOOSE)),
                new ConditionalEffect(new HappenedThisWay(StepKind.PUT_INTO_LIBRARY),
                        new ExileTopCardMayPlayThisTurnEffect(2, false)));
    }
}

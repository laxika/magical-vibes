package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.MayChoicePlayer;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.effect.ChooseCardEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOpponentGainsControlOfSourceEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PlayerSubject;
import com.github.laxika.magicalvibes.model.effect.PutCardToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardsOntoBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "C21", collectorNumber = "256")
@CardRegistration(set = "C19", collectorNumber = "56")
public class PendantOfProsperity extends Card {

    public PendantOfProsperity() {
        addStaticEffect(new ChooseOpponentGainsControlOfSourceEffect());

        PlayerSubject owner = Subjects.ownerOf(Subjects.source());
        CardTypePredicate land = new CardTypePredicate(CardType.LAND);
        addActivatedAbility(new ActivatedAbility(
                true,
                "{2}",
                List.of(
                        new DrawCardEffect(1),
                        new MayEffect(
                                new PutCardToBattlefieldEffect(land, "land"),
                                "Put a land card from your hand onto the battlefield?"),
                        new DrawCardEffect(1, Subjects.ownerOf(Subjects.source())),
                        new MayEffect(
                                SequenceEffect.of(
                                        ChooseCardEffect.upTo(Subjects.cardsInHands(owner, land), 1,
                                                        "a land card from your hand to put onto the battlefield")
                                                .by(owner),
                                        new PutCardsOntoBattlefieldEffect(Subjects.cardsThisWay(StepKind.CHOOSE))),
                                "Put a land card from your hand onto the battlefield?",
                                null,
                                MayChoicePlayer.SOURCE_OWNER)
                ),
                "{2}, {T}: Draw a card, then you may put a land card from your hand onto the battlefield. "
                        + "This artifact's owner draws a card, then that player may put a land card from their hand "
                        + "onto the battlefield."
        ));
    }
}

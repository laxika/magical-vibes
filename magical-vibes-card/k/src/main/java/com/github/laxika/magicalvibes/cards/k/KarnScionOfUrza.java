package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.StaticAbility;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseCardEffect;
import com.github.laxika.magicalvibes.model.effect.ChoosePlayerEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.ExileGraveyardCardsEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardsIntoHandEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnExiledCardsEffect;
import com.github.laxika.magicalvibes.model.effect.RevealTopCardOfLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.CardHasCounterInExilePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "DOM", collectorNumber = "1")
@CardRegistration(set = "SLD", collectorNumber = "1419")
public class KarnScionOfUrza extends Card {

    public KarnScionOfUrza() {
        // +1: Reveal the top two cards of your library. An opponent chooses one of them.
        // Put that card into your hand and exile the other with a silver counter on it.
        addActivatedAbility(new ActivatedAbility(
                +1,
                List.of(RevealTopCardOfLibraryEffect.topCards(Subjects.you(), new Fixed(2)),
                        ChoosePlayerEffect.opponent(),
                        ChooseCardEffect.oneOf(Subjects.revealedThisWay(), "a card to put into its owner's hand")
                                .by(Subjects.playersThisWay(StepKind.CHOOSE)),
                        new PutCardsIntoHandEffect(Subjects.cardsThisWay(StepKind.CHOOSE)),
                        ExileGraveyardCardsEffect.cards(Subjects.revealedThisWay()),
                        PutCounterOnExiledCardsEffect.onCardsExiledThisWay(CounterType.SILVER)),
                "+1: Reveal the top two cards of your library. An opponent chooses one of them. Put that card into your hand and exile the other with a silver counter on it."
        ));

        // −1: Put a card you own with a silver counter on it from exile into your hand.
        addActivatedAbility(new ActivatedAbility(
                -1,
                List.of(ChooseCardEffect.oneOf(Subjects.exiledCards(Subjects.you(),
                                        new CardHasCounterInExilePredicate(CounterType.SILVER)),
                                "a card you own in exile with a silver counter on it"),
                        new PutCardsIntoHandEffect(Subjects.cardsThisWay(StepKind.CHOOSE))),
                "−1: Put a card you own with a silver counter on it from exile into your hand."
        ));

        // −2: Create a 0/0 colorless Construct artifact creature token with
        // "This creature gets +1/+1 for each artifact you control."
        PermanentCount artifactsYouControl =
                new PermanentCount(new PermanentIsArtifactPredicate(), CountScope.CONTROLLER);
        addActivatedAbility(new ActivatedAbility(
                -2,
                List.of(new CreateTokenEffect(
                        1, "Construct", 0, 0,
                        null, List.of(CardSubtype.CONSTRUCT),
                        Set.of(), Set.of(CardType.ARTIFACT),
                        List.of(StaticAbility.of(BoostEffect.self(artifactsYouControl, artifactsYouControl)))
                )),
                "−2: Create a 0/0 colorless Construct artifact creature token with \"This creature gets +1/+1 for each artifact you control.\""
        ));
    }
}

package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseCardEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardsIntoLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.SourceControllerDrawsCost;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.UnlessPaysEffect;
import java.util.List;
import java.util.Set;

/** Research // Development, a split spell with one mode for each half. */
@CardRegistration(set = "DIS", collectorNumber = "155")
public class ResearchDevelopment extends Card {

    public ResearchDevelopment() {
        CreateTokenEffect elemental = new CreateTokenEffect(
                1, "Elemental", 3, 1, CardColor.RED, List.of(CardSubtype.ELEMENTAL), Set.of(), Set.of());
        CardEffect elementalUnlessYouDraw = UnlessPaysEffect.unlessPays(Subjects.eachOpponent(),
                new SourceControllerDrawsCost(1), elemental)
                .withPrompt("Have its controller draw a card? If no opponent does, they create a 3/1 red Elemental"
                        + " creature token.")
                .payingTogether();
        List<CardEffect> development = List.of(elementalUnlessYouDraw, elementalUnlessYouDraw, elementalUnlessYouDraw);

        addSpellEffect(new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Research - Shuffle up to four cards you own from outside the game into your library",
                        List.of(
                                ChooseCardEffect.upTo(Subjects.cardsOutsideTheGame(Subjects.you(), null), 4,
                                        "cards you own from outside the game to shuffle into your library"),
                                PutCardsIntoLibraryEffect.shuffledIn(Subjects.cardsThisWay(StepKind.CHOOSE))
                                        .alwaysShuffling(Subjects.you()))
                ).withManaCost("{G}{U}"),
                new ChooseOneEffect.ChooseOneOption(
                        "Development - Create a 3/1 red Elemental creature token unless an opponent has you draw a card, repeated three times",
                        development
                ).withManaCost("{3}{U}{R}")
        )));
    }
}

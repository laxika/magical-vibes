package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.m.MistralCharger;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CourtHussar.class, MistralCharger.class})
class CourtHussarTest extends BaseCardTest {

    @Test
    @DisplayName("Both enter abilities trigger separately even when white mana was spent")
    void enterAbilitiesUseSeparateStackEntries() {
        harness.setLibrary(player1, List.of());
        harness.setHand(player1, List.of(new CourtHussar()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).hasSize(2);
    }

    @Test
    @DisplayName("White mana prevents sacrifice even with an empty library")
    void whiteManaPreventsSacrificeWithEmptyLibrary() {
        harness.setLibrary(player1, List.of());
        harness.setHand(player1, List.of(new CourtHussar()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Court Hussar");
        harness.assertNotInGraveyard(player1, "Court Hussar");
    }

    @Test
    @DisplayName("Entry without casting still puts the only library card into hand and sacrifices Hussar")
    void entryWithoutCastingStillGetsCardAndSacrifices() {
        Card onlyCard = new MistralCharger();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(onlyCard));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.enterBattlefieldAndReturn(player1, new CourtHussar());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(onlyCard);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        harness.assertNotOnBattlefield(player1, "Court Hussar");
        harness.assertInGraveyard(player1, "Court Hussar");
    }

    @Test
    @DisplayName("Unchosen cards go below the untouched library in the chosen order without white mana")
    void unchosenCardsGoToBottomInChosenOrderWithoutWhiteMana() {
        Card top = new MistralCharger();
        Card middle = new MistralCharger();
        Card third = new MistralCharger();
        Card untouched = new MistralCharger();
        harness.setLibrary(player1, List.of(top, middle, third, untouched));
        harness.castFromHand(player1, new CourtHussar(), "{2}{U}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.handleMultipleCardsChosen(player1, List.of(middle.getId()));
        harness.getGameService().handleInteractionAnswer(gd, player1,
                new InteractionAnswer.CardOrder(List.of(1, 0)));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(middle);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(untouched, third, top);
    }

    @Test
    @DisplayName("Looks at the top three cards and keeps one when white mana was spent")
    void looksAtTopThreeAndKeepsOneWhenWhiteManaWasSpent() {
        harness.setHand(player1, List.of(new CourtHussar()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 2);

        Card top = new MistralCharger();
        Card middle = new MistralCharger();
        Card bottom = new MistralCharger();
        harness.setLibrary(player1, List.of(top, middle, bottom));

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.handleMultipleCardsChosen(player1, List.of(middle.getId()));
        harness.getGameService().handleInteractionAnswer(gd, player1, new InteractionAnswer.CardOrder(List.of(0, 1)));

        assertThat(gd.playerHands.get(player1.getId())).contains(middle);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(top, bottom);
        harness.assertOnBattlefield(player1, "Court Hussar");
    }

    @Test
    @DisplayName("Looks at all available cards when fewer than three are in the library")
    void looksAtAllAvailableCardsWhenFewerThanThreeAreInLibrary() {
        harness.setHand(player1, List.of(new CourtHussar()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 2);

        Card top = new MistralCharger();
        Card bottom = new MistralCharger();
        harness.setLibrary(player1, List.of(top, bottom));

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.handleMultipleCardsChosen(player1, List.of(bottom.getId()));

        assertThat(gd.playerHands.get(player1.getId())).contains(bottom);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(top);
        harness.assertOnBattlefield(player1, "Court Hussar");
    }

    @Test
    @DisplayName("Sacrifices itself when white mana was not spent")
    void sacrificesItselfWhenWhiteManaWasNotSpent() {
        harness.castFromHand(player1, new CourtHussar(), "{2}{U}");
        harness.setLibrary(player1, List.of());
        harness.passBothPriorities();
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Court Hussar");
        harness.assertInGraveyard(player1, "Court Hussar");
    }
}

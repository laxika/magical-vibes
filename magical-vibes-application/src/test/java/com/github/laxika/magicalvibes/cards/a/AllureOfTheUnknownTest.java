package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AllureOfTheUnknown.class, Forest.class, GrizzlyBears.class})
class AllureOfTheUnknownTest extends BaseCardTest {

    @Test
    @DisplayName("An opponent exiles a revealed nonland, the rest go to hand, and they may cast it free")
    void opponentExilesNonlandAndMayCastIt() {
        Forest forest1 = new Forest();
        GrizzlyBears firstBear = new GrizzlyBears();
        Forest forest2 = new Forest();
        GrizzlyBears chosenBear = new GrizzlyBears();
        Forest forest3 = new Forest();
        Forest forest4 = new Forest();
        GrizzlyBears untouched = new GrizzlyBears();
        setLibrary(forest1, firstBear, forest2, chosenBear, forest3, forest4, untouched);

        castAndResolve();

        PendingInteraction.LibraryRevealChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(choice.playerId()).isEqualTo(player2.getId());
        assertThat(choice.validCardIds()).containsExactly(firstBear.getId(), chosenBear.getId());

        harness.handleMultipleCardsChosen(player2, List.of(chosenBear.getId()));

        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(chosenBear);
        assertThat(gd.playerHands.get(player1.getId()))
                .containsExactlyInAnyOrder(forest1, firstBear, forest2, forest3, forest4);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(untouched);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.handleMayAbilityChosen(player2, true);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Allure of the Unknown");
    }

    @Test
    @DisplayName("If the revealed cards are all lands, they all go to the controller's hand")
    void allLandsGoToHandWithoutAnOpponentChoice() {
        Forest forest1 = new Forest();
        Forest forest2 = new Forest();
        Forest forest3 = new Forest();
        Forest forest4 = new Forest();
        Forest forest5 = new Forest();
        Forest forest6 = new Forest();
        GrizzlyBears untouched = new GrizzlyBears();
        setLibrary(forest1, forest2, forest3, forest4, forest5, forest6, untouched);

        castAndResolve();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player1.getId()))
                .containsExactlyInAnyOrder(forest1, forest2, forest3, forest4, forest5, forest6);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(untouched);
    }

    private void castAndResolve() {
        harness.setHand(player1, List.of(new AllureOfTheUnknown()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
    }

    private void setLibrary(Card... cards) {
        harness.setLibrary(player1, List.of(cards));
    }
}

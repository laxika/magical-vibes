package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed(CandyTrail.class)
class CandyTrailTest extends BaseCardTest {

    @Test
    void enteringBattlefieldScriesTwo() {
        List<Card> library = List.of(new CandyTrail(), new CandyTrail(), new CandyTrail());
        harness.setLibrary(player1, library);
        harness.castFromHand(player1, new CandyTrail(), "{1}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards())
                .containsExactly(library.get(0), library.get(1));

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(1), List.of(0)));

        assertThat(gd.playerDecks.get(player1.getId()))
                .containsExactly(library.get(1), library.get(2), library.get(0));
    }

    @Test
    void sacrificesToGainLifeAndDrawCard() {
        Permanent candyTrail = harness.addToBattlefieldAndReturn(player1, new CandyTrail());
        CandyTrail card = new CandyTrail();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(card));
        harness.setLife(player1, 20);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(candyTrail);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(candyTrail.getCard());

        harness.passBothPriorities();

        harness.assertLife(player1, 23);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(card);
    }

    @Test
    void scryWithOneCardCanKeepThatCardOnTop() {
        CandyTrail card = new CandyTrail();
        harness.setLibrary(player1, List.of(card));
        harness.castFromHand(player1, new CandyTrail(), "{1}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards())
                .containsExactly(card);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(0), List.of()));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(card);
        harness.assertLife(player1, 20);
    }

    @Test
    void scryWithEmptyLibraryDoesNotRequireAChoice() {
        harness.setLibrary(player1, List.of());
        harness.castFromHand(player1, new CandyTrail(), "{1}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        harness.assertOnBattlefield(player1, "Candy Trail");
        harness.assertLife(player1, 20);
    }

    @Test
    void cannotActivateWhenTapped() {
        Permanent candyTrail = harness.addToBattlefieldAndReturn(player1, new CandyTrail());
        candyTrail.tap();
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(candyTrail);
        harness.assertNotInGraveyard(player1, "Candy Trail");
        harness.assertLife(player1, 20);
    }

    @Test
    void cannotActivateWithOnlyOneMana() {
        Permanent candyTrail = harness.addToBattlefieldAndReturn(player1, new CandyTrail());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(candyTrail);
        assertThat(candyTrail.isTapped()).isFalse();
        harness.assertNotInGraveyard(player1, "Candy Trail");
        harness.assertLife(player1, 20);
    }

    @Test
    void newlyEnteredArtifactCanActivateAndBenefitsItsControllerOnlyOnResolution() {
        Permanent candyTrail = harness.addToBattlefieldAndReturn(player2, new CandyTrail());
        candyTrail.setSummoningSick(true);
        CandyTrail card = new CandyTrail();
        harness.setLibrary(player2, List.of(card));
        harness.setHand(player2, List.of());
        harness.setLife(player1, 17);
        harness.setLife(player2, 12);
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        harness.activateAbility(player2, 0, 0, null, null);

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(candyTrail);
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(candyTrail.getCard());
        harness.assertLife(player2, 12);
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();

        harness.passBothPriorities();

        harness.assertLife(player1, 17);
        harness.assertLife(player2, 15);
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(card);
    }
}

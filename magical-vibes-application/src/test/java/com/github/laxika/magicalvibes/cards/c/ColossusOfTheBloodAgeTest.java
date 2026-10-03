package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.w.WrathOfGod;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ColossusOfTheBloodAge.class, GrizzlyBears.class, WrathOfGod.class})
class ColossusOfTheBloodAgeTest extends BaseCardTest {

    @Test
    @DisplayName("ETB deals 3 damage to each opponent and controller gains 3 life")
    void etbDamagesOpponentsAndGainsLife() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        castColossus();
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB trigger

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(23);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("When Colossus dies, controller may discard 2 and draw 3")
    void deathTriggerDiscardTwoDrawThree() {
        harness.addToBattlefield(player1, new ColossusOfTheBloodAge());
        harness.setHand(player1, List.of(
                new WrathOfGod(),
                new GrizzlyBears(),
                new GrizzlyBears(),
                new GrizzlyBears(),
                new GrizzlyBears()
        ));

        killColossusWithWrath(player1);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.XValueChoice.class) != null).isTrue();

        harness.handleXValueChosen(player1, 2);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class) != null).isTrue();
        harness.handleCardChosen(player1, 0);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class) != null).isTrue();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(5);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .filteredOn(c -> c.getName().equals("Grizzly Bears"))
                .hasSize(2);
        harness.assertInGraveyard(player1, "Colossus of the Blood Age");
    }

    @Test
    @DisplayName("When Colossus dies, controller may discard 0 and draw 1")
    void deathTriggerDiscardZeroDrawOne() {
        harness.addToBattlefield(player1, new ColossusOfTheBloodAge());
        Card loneCard = new GrizzlyBears();
        harness.setHand(player1, List.of(new WrathOfGod(), loneCard));

        killColossusWithWrath(player1);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.XValueChoice.class) != null).isTrue();

        harness.handleXValueChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .filteredOn(c -> c.getName().equals("Grizzly Bears"))
                .isEmpty();
    }

    @Test
    @DisplayName("When Colossus dies with empty hand, controller draws 1")
    void deathTriggerEmptyHandDrawsOne() {
        harness.addToBattlefield(player1, new ColossusOfTheBloodAge());
        harness.setHand(player1, List.of());

        harness.setHand(player2, List.of(new WrathOfGod()));
        harness.addMana(player2, ManaColor.WHITE, 4);
        setupPlayer2Active();

        harness.castAndResolveSorcery(player2, 0, 0);
        harness.passBothPriorities(); // Death trigger resolves and draws 1

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        harness.assertInGraveyard(player1, "Colossus of the Blood Age");
    }

    @Test
    @DisplayName("ETB uses the opponent's controller for damage and life gain")
    void opponentControlledEtbDamagesOnlyTheirOpponent() {
        setupPlayer2Active();
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.castFromHand(player2, new ColossusOfTheBloodAge(), "{4}{R}{W}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player1, 17);
        harness.assertLife(player2, 23);
    }

    @Test
    @DisplayName("Discarding the entire hand draws one more card after all discards")
    void deathTriggerCanDiscardEntireHandBeforeDrawing() {
        harness.addToBattlefield(player1, new ColossusOfTheBloodAge());
        Card firstDiscard = new ColossusOfTheBloodAge();
        Card secondDiscard = new ColossusOfTheBloodAge();
        Card firstDraw = new ColossusOfTheBloodAge();
        Card secondDraw = new ColossusOfTheBloodAge();
        Card thirdDraw = new ColossusOfTheBloodAge();
        harness.setHand(player1, List.of(new WrathOfGod(), firstDiscard, secondDiscard));
        harness.setLibrary(player1, List.of(firstDraw, secondDraw, thirdDraw));

        killColossusWithWrath(player1);
        harness.handleXValueChosen(player1, 2);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(secondDiscard);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(3);

        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId()))
                .containsExactlyInAnyOrder(firstDraw, secondDraw, thirdDraw);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(firstDiscard, secondDiscard);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("An opponent's death trigger discards and draws only for that opponent")
    void deathTriggerUsesDyingCreaturesController() {
        harness.addToBattlefield(player2, new ColossusOfTheBloodAge());
        Card discarded = new ColossusOfTheBloodAge();
        Card firstDraw = new ColossusOfTheBloodAge();
        Card secondDraw = new ColossusOfTheBloodAge();
        harness.setHand(player2, List.of(discarded));
        harness.setLibrary(player2, List.of(firstDraw, secondDraw));
        harness.setHand(player1, List.of(new WrathOfGod()));

        killColossusWithWrath(player1);
        harness.handleXValueChosen(player2, 1);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).containsExactlyInAnyOrder(firstDraw, secondDraw);
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(discarded);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    private void castColossus() {
        harness.castFromHand(player1, new ColossusOfTheBloodAge(), "{4}{R}{W}");
    }

    private void killColossusWithWrath(com.github.laxika.magicalvibes.model.Player controller) {
        harness.addMana(controller, ManaColor.WHITE, 4);
        harness.castAndResolveSorcery(controller, 0, 0);
        harness.passBothPriorities(); // Death trigger begins resolving
    }

    private void setupPlayer2Active() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}

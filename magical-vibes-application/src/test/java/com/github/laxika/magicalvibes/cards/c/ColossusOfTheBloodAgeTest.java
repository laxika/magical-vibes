package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.w.WrathOfGod;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ColossusOfTheBloodAgeTest extends BaseCardTest {

    // ===== ETB =====

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

    // ===== Death trigger =====

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
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Colossus of the Blood Age"));
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

        harness.castSorcery(player2, 0, 0);
        harness.passBothPriorities(); // Wrath resolves, Colossus dies
        harness.passBothPriorities(); // Death trigger resolves and draws 1

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Colossus of the Blood Age"));
    }

    // ===== Helpers =====

    private void castColossus() {
        harness.setHand(player1, List.of(new ColossusOfTheBloodAge()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castCreature(player1, 0);
    }

    private void killColossusWithWrath(com.github.laxika.magicalvibes.model.Player controller) {
        harness.addMana(controller, ManaColor.WHITE, 4);
        harness.castSorcery(controller, 0, 0);
        harness.passBothPriorities(); // Wrath resolves, Colossus dies, death trigger on stack
        harness.passBothPriorities(); // Death trigger begins resolving
    }

    private void setupPlayer2Active() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}

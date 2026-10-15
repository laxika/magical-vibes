package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.c.CoastalPeak;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TyphoidMaryFractured.class, CoastalPeak.class, Shock.class})
class TyphoidMaryFracturedTest extends BaseCardTest {

    private static final String MARY_MODE = "Mary — Create a Treasure token";
    private static final String TYPHOID_MARY_MODE = "Typhoid Mary — Draw a card";
    private static final String BLOODY_MARY_MODE =
            "Bloody Mary — Each opponent loses 2 life and you gain 2 life";

    @Test
    @DisplayName("Lets you choose the Treasure mode after discarding this turn")
    void choosesTreasureModeAfterDiscarding() {
        discardThisTurn();
        attackMary();

        harness.handleListChoice(player1, MARY_MODE);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Treasure")).hasSize(1);
    }

    @Test
    @DisplayName("Lets you choose the draw mode after discarding this turn")
    void choosesDrawModeAfterDiscarding() {
        discardThisTurn();
        attackMary();

        harness.handleListChoice(player1, TYPHOID_MARY_MODE);
        resolveAllTriggers();

        harness.assertInHand(player1, "Shock");
    }

    @Test
    @DisplayName("Lets you choose the life-drain mode after discarding this turn")
    void choosesLifeDrainModeAfterDiscarding() {
        harness.setLife(player1, 10);
        harness.setLife(player2, 20);
        discardThisTurn();
        attackMary();

        harness.handleListChoice(player1, BLOODY_MARY_MODE);
        resolveAllTriggers();

        harness.assertLife(player1, 12);
        harness.assertLife(player2, 15);
    }

    @Test
    @DisplayName("Chooses one mode at random without a discard")
    void choosesModeAtRandomWithoutDiscarding() {
        harness.setLibrary(player1, List.of(new CoastalPeak(), new Shock()));
        attackMary();
        resolveAllTriggers();

        boolean treasure = findPermanents(player1, "Treasure").size() == 1;
        boolean draw = gd.playerHands.get(player1.getId()).stream()
                .anyMatch(card -> card.getName().equals("Coastal Peak")
                        || card.getName().equals("Shock"));
        boolean drain = gd.playerLifeTotals.get(player1.getId()) == 22
                && gd.playerLifeTotals.get(player2.getId()) == 15;
        assertThat(treasure || draw || drain).isTrue();
    }

    @Test
    @DisplayName("Chooses the mode before players can respond to the attack trigger")
    void choosesModeWhenTriggerGoesOnStack() {
        discardThisTurn();
        addCreatureReady(player1, new TyphoidMaryFractured());

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS,
                () -> declareAttackers(List.of(0)));

        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS,
                () -> harness.handleListChoice(player1, MARY_MODE));
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).hasSize(1);
        assertThat(findPermanents(player1, "Treasure")).isEmpty();

        resolveAllTriggers();
        assertThat(findPermanents(player1, "Treasure")).hasSize(1);
    }

    @Test
    @DisplayName("Discarding in response cannot replace the already randomly chosen mode")
    void discardAfterTriggerDoesNotEnableChoosingMode() {
        harness.setHand(player1, List.of(new CoastalPeak()));
        harness.setLibrary(player1, List.of(new Shock(), new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        addCreatureReady(player1, new TyphoidMaryFractured());

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(List.of(0));
            assertThat(gd.interaction.isAwaitingInput()).isFalse();
            assertThat(gd.stack).hasSize(1);
            harness.activateHandAbility(player1, 0, null);
            harness.passBothPriorities();
            resolveAllTriggers();
        });

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        int treasure = findPermanents(player1, "Treasure").size();
        int extraDraw = gd.playerHands.get(player1.getId()).size() - 1;
        int drain = gd.playerLifeTotals.get(player1.getId()) == 22 ? 1 : 0;
        assertThat(treasure + extraDraw + drain).isEqualTo(1);
    }

    @Test
    @DisplayName("An opponent's discard does not let the controller choose a mode")
    void opponentDiscardDoesNotEnableChoosingMode() {
        harness.setHand(player2, List.of(new CoastalPeak()));
        harness.setLibrary(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.withAutoStop(TurnStep.PRECOMBAT_MAIN, () -> {
            harness.activateHandAbility(player2, 0, null);
            harness.passBothPriorities();
        });
        harness.setLibrary(player1, List.of(new Shock(), new Shock()));

        attackMary();
        resolveAllTriggers();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        int treasure = findPermanents(player1, "Treasure").size();
        int draw = gd.playerHands.get(player1.getId()).size();
        int drain = gd.playerLifeTotals.get(player1.getId()) == 22 ? 1 : 0;
        assertThat(treasure + draw + drain).isEqualTo(1);
    }

    @Test
    @DisplayName("The Mary mode creates an untapped Treasure that can make mana")
    void treasureCanBeSacrificedForMana() {
        discardThisTurn();
        attackMary();
        harness.handleListChoice(player1, MARY_MODE);
        resolveAllTriggers();

        var treasure = findPermanent(player1, "Treasure");
        assertThat(treasure.isTapped()).isFalse();
        int index = gd.playerBattlefields.get(player1.getId()).indexOf(treasure);
        harness.activateAbility(player1, index, null, null);
        harness.handleListChoice(player1, ManaColor.RED.name());

        harness.assertNotOnBattlefield(player1, "Treasure");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
    }

    private void discardThisTurn() {
        harness.setHand(player1, List.of(new CoastalPeak()));
        harness.setLibrary(player1, List.of(new CoastalPeak(), new Shock()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();
    }

    private void attackMary() {
        addCreatureReady(player1, new TyphoidMaryFractured());
        declareAttackers(List.of(0));
        harness.passBothPriorities();
    }
}

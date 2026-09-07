package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ClackbridgeTroll.class, GrizzlyBears.class})
class ClackbridgeTrollTest extends BaseCardTest {

    @Test
    @DisplayName("Enters and gives the targeted opponent three Goat tokens")
    void entersAndCreatesGoatsForTargetOpponent() {
        harness.setHand(player1, new ArrayList<>(List.of(new ClackbridgeTroll())));
        harness.addMana(player1, com.github.laxika.magicalvibes.model.ManaColor.BLACK, 2);
        harness.addMana(player1, com.github.laxika.magicalvibes.model.ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0, 0, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        List<Permanent> goats = gd.playerBattlefields.get(player2.getId()).stream()
                .filter(permanent -> permanent.getCard().getName().equals("Goat"))
                .toList();
        assertThat(goats).hasSize(3);
        assertThat(goats).allSatisfy(goat -> {
            assertThat(goat.getCard().getPower()).isEqualTo(0);
            assertThat(goat.getCard().getToughness()).isEqualTo(1);
        });
    }

    @Test
    @DisplayName("Declining the combat choice leaves the Troll and opponent creature unchanged")
    void decliningDoesNothing() {
        Permanent troll = harness.addToBattlefieldAndReturn(player1, new ClackbridgeTroll());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setLife(player1, 20);
        harness.setHand(player1, new ArrayList<>());

        resolveBeginningOfCombat(player1);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player2, false);

        assertThat(troll.isTapped()).isFalse();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Accepting sacrifices a creature, taps the Troll, gains life, and draws")
    void acceptingSacrificesAndRewardsController() {
        Permanent troll = harness.addToBattlefieldAndReturn(player1, new ClackbridgeTroll());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setLife(player1, 20);
        harness.setHand(player1, new ArrayList<>());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));

        resolveBeginningOfCombat(player1);
        harness.handleMayAbilityChosen(player2, true);

        assertThat(troll.isTapped()).isTrue();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(23);
        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertInHand(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Accepting with several creatures asks the opponent which one to sacrifice")
    void acceptingWithSeveralCreaturesAsksWhichOne() {
        Permanent troll = harness.addToBattlefieldAndReturn(player1, new ClackbridgeTroll());
        harness.addToBattlefield(player2, new GrizzlyBears());
        Permanent chosen = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setLife(player1, 20);
        harness.setHand(player1, new ArrayList<>());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));

        resolveBeginningOfCombat(player1);
        harness.handleMayAbilityChosen(player2, true);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player2, chosen.getId());

        assertThat(gd.playerBattlefields.get(player2.getId())).hasSize(1);
        assertThat(troll.isTapped()).isTrue();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(23);
        harness.assertInHand(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Triggers only at the beginning of combat on its controller's turn")
    void doesNotTriggerOnOpponentTurn() {
        harness.addToBattlefieldAndReturn(player1, new ClackbridgeTroll());
        harness.addToBattlefield(player2, new GrizzlyBears());

        resolveBeginningOfCombat(player2);

        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private void resolveBeginningOfCombat(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}

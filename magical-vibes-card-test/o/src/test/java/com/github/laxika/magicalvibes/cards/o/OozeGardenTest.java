package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.n.NecroticOoze;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({OozeGarden.class, GrizzlyBears.class, Ornithopter.class, NecroticOoze.class})
class OozeGardenTest extends BaseCardTest {

    private long oozeTokenCount(Player player) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Ooze") && p.getCard().isToken())
                .count();
    }

    private void activateAsSorcery() {
        harness.addMana(player1, ManaColor.GREEN, 2); // pays {1}{G}
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.activateAbility(player1, 0, null, null); // Ooze Garden is index 0
    }

    @Test
    @DisplayName("Sacrificing a 2/2 creature creates an Ooze token whose power and toughness are 2")
    void sacrificingCreatesXXOoze() {
        harness.addToBattlefield(player1, new OozeGarden());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears()); // 2/2

        activateAsSorcery();
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities(); // resolve ability

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Grizzly Bears");

        var oozes = findPermanents(player1, "Ooze");
        assertThat(oozes).hasSize(1);
        assertThat(oozes.getFirst().getCard().getPower()).isEqualTo(2);
        assertThat(oozes.getFirst().getCard().getToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Sacrificing a 0-power creature makes a 0/0 Ooze that dies immediately")
    void zeroPowerTokenDies() {
        harness.addToBattlefield(player1, new OozeGarden());
        Permanent ornithopter = addCreatureReady(player1, new Ornithopter()); // 0/2

        activateAsSorcery();
        harness.handlePermanentChosen(player1, ornithopter.getId());
        harness.passBothPriorities(); // resolve ability; the 0/0 dies to state-based actions

        harness.assertNotOnBattlefield(player1, "Ornithopter");
        assertThat(oozeTokenCount(player1)).isZero();
    }

    @Test
    @DisplayName("The creature is sacrificed as a cost and the token is created when the ability resolves")
    void sacrificeIsPaidBeforeResolution() {
        harness.addToBattlefield(player1, new OozeGarden());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, bears.getId());

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        assertThat(oozeTokenCount(player1)).isZero();
        harness.passBothPriorities();
        assertThat(oozeTokenCount(player1)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The ability cannot be activated outside a main phase")
    void cannotActivateDuringCombat() {
        harness.addToBattlefield(player1, new OozeGarden());
        addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("The ability cannot be activated on another player's turn")
    void cannotActivateOnOpponentsTurn() {
        harness.addToBattlefield(player1, new OozeGarden());
        addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.ensurePriority(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("A non-Ooze creature is required — an Ooze creature can't be sacrificed")
    void oozeCreatureCannotBeSacrificed() {
        harness.addToBattlefield(player1, new OozeGarden());
        addCreatureReady(player1, new NecroticOoze()); // an Ooze creature

        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertOnBattlefield(player1, "Necrotic Ooze");
        assertThat(oozeTokenCount(player1)).isZero();
    }
}

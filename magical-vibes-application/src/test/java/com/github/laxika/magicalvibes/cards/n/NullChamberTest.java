package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.a.AdarkarWastes;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class NullChamberTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving Null Chamber asks the controller, then the opponent, for a card name")
    void bothPlayersNameACard() {
        harness.setHand(player1, List.of(new NullChamber(), new GrizzlyBears()));
        harness.setHand(player2, List.of(new AdarkarWastes()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Null Chamber");
        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class).playerId())
                .isEqualTo(player1.getId());

        harness.handleListChoice(player1, "Adarkar Wastes");

        harness.assertNotOnBattlefield(player1, "Null Chamber");
        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class).playerId())
                .isEqualTo(player2.getId());

        harness.handleListChoice(player2, "Grizzly Bears");

        Permanent perm = findPermanent(player1, "Null Chamber");
        assertThat(perm.getChosenName()).isEqualTo("Adarkar Wastes");
        assertThat(perm.getSecondChosenName()).isEqualTo("Grizzly Bears");
    }

    @Test
    @DisplayName("Basic land names are not offered as choices")
    void basicLandNamesAreNotOffered() {
        harness.setHand(player1, List.of(new NullChamber(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();

        List<String> options = gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class).options();
        assertThat(options).contains("Grizzly Bears");
        assertThat(options).doesNotContain("Plains", "Island", "Swamp", "Mountain", "Forest");
    }

    @Test
    @DisplayName("Neither player can cast a spell with either chosen name")
    void neitherPlayerCanCastEitherChosenName() {
        addReadyNullChamber(player1, "Grizzly Bears", "Naturalize");

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.addMana(player2, ManaColor.GREEN, 2);

        assertThatThrownBy(() -> harness.castCreature(player2, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");

        harness.forceActivePlayer(player1);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new Naturalize()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("A land with a chosen name can't be played")
    void landWithChosenNameCantBePlayed() {
        addReadyNullChamber(player1, "Adarkar Wastes", "Grizzly Bears");

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new AdarkarWastes()));

        assertThatThrownBy(() -> harness.playLand(player2, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Lands and spells with other names are unaffected")
    void otherNamesAreUnaffected() {
        addReadyNullChamber(player1, "Grizzly Bears", "Naturalize");

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new AdarkarWastes()));

        harness.playLand(player2, 0);

        harness.assertOnBattlefield(player2, "Adarkar Wastes");
    }

    @Test
    @DisplayName("The restrictions lift when Null Chamber leaves the battlefield")
    void restrictionsLiftWhenChamberLeaves() {
        Permanent chamber = addReadyNullChamber(player1, "Grizzly Bears", "Adarkar Wastes");

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Naturalize()));
        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.castInstant(player2, 0, chamber.getId());
        harness.passBothPriorities();

        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.addMana(player2, ManaColor.GREEN, 2);

        harness.castCreature(player2, 0);

        assertThat(gd.stack).hasSize(1);
    }

    /**
     * CR 704.5k — the world rule: the older of two world permanents is put into its owner's
     * graveyard, no matter who controls them.
     */
    @Test
    @DisplayName("A second Null Chamber puts the first one into the graveyard (world rule)")
    void worldRulePutsOlderChamberIntoGraveyard() {
        Permanent first = addReadyNullChamber(player1, "Grizzly Bears", "Naturalize");
        Permanent second = addReadyNullChamber(player2, "Adarkar Wastes", "Naturalize");
        first.setTimestamp(1);
        second.setTimestamp(2);

        harness.runStateBasedActions();

        harness.assertNotOnBattlefield(player1, "Null Chamber");
        harness.assertOnBattlefield(player2, "Null Chamber");
    }

    private Permanent addReadyNullChamber(Player player, String firstName, String secondName) {
        Permanent perm = new Permanent(new NullChamber());
        perm.setChosenName(firstName);
        perm.setSecondChosenName(secondName);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}

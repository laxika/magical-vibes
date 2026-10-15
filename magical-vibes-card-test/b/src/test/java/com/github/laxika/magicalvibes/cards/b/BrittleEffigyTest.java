package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.r.RuneclawBear;
import com.github.laxika.magicalvibes.cards.j.Juggernaut;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BrittleEffigy.class, RuneclawBear.class, Juggernaut.class})
class BrittleEffigyTest extends BaseCardTest {

    @Test
    @DisplayName("Activating ability exiles Brittle Effigy as cost and puts ability on stack")
    void activatingExilesSelfAndPutsOnStack() {
        addReadyEffigy(player1);
        Permanent target = addCreatureReady(player2, new RuneclawBear());
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.activateAbility(player1, 0, null, target.getId());

        GameData gd = harness.getGameData();
        // Effigy is exiled as cost
        harness.assertNotOnBattlefield(player1, "Brittle Effigy");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Brittle Effigy"));
        // Ability is on the stack
        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        assertThat(entry.getTargetId()).isEqualTo(target.getId());
    }

    @Test
    @DisplayName("Resolving ability exiles target creature")
    void resolvingExilesTargetCreature() {
        addReadyEffigy(player1);
        Permanent target = addCreatureReady(player2, new RuneclawBear());
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        harness.assertNotOnBattlefield(player2, "Runeclaw Bear");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(c -> c.getName().equals("Runeclaw Bear"));
        harness.assertNotInGraveyard(player2, "Runeclaw Bear");
    }

    @Test
    @DisplayName("Brittle Effigy goes to exile, not graveyard, as cost")
    void effigyGoesToExileNotGraveyard() {
        addReadyEffigy(player1);
        Permanent target = addCreatureReady(player2, new RuneclawBear());
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.activateAbility(player1, 0, null, target.getId());

        GameData gd = harness.getGameData();
        harness.assertNotInGraveyard(player1, "Brittle Effigy");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Brittle Effigy"));
    }

    @Test
    @DisplayName("Cannot activate without enough mana")
    void cannotActivateWithoutEnoughMana() {
        addReadyEffigy(player1);
        Permanent target = addCreatureReady(player2, new RuneclawBear());
        harness.addMana(player1, ManaColor.WHITE, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("Consumes 4 mana when activating")
    void consumesMana() {
        addReadyEffigy(player1);
        Permanent target = addCreatureReady(player2, new RuneclawBear());
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.activateAbility(player1, 0, null, target.getId());

        GameData gd = harness.getGameData();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot activate when tapped")
    void cannotActivateWhenTapped() {
        Permanent effigy = addReadyEffigy(player1);
        effigy.tap();
        Permanent target = addCreatureReady(player2, new RuneclawBear());
        harness.addMana(player1, ManaColor.WHITE, 4);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can activate the turn it enters while it is not a creature")
    void canActivateTurnItEnters() {
        harness.addToBattlefield(player1, new BrittleEffigy());
        Permanent target = addCreatureReady(player2, new RuneclawBear());
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.activateAbility(player1, 0, null, target.getId());

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Ability fizzles if target creature is removed before resolution")
    void fizzlesIfTargetRemoved() {
        addReadyEffigy(player1);
        Permanent target = addCreatureReady(player2, new RuneclawBear());
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.activateAbility(player1, 0, null, target.getId());

        // Remove target before resolution
        harness.getGameData().playerBattlefields.get(player2.getId()).clear();

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
        // Effigy is still exiled (cost was already paid)
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Brittle Effigy"));
    }

    @Test
    @DisplayName("Can exile a creature its controller owns")
    void canExileOwnCreature() {
        addReadyEffigy(player1);
        Permanent target = addCreatureReady(player1, new RuneclawBear());
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Runeclaw Bear");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .extracting(c -> c.getName())
                .containsExactlyInAnyOrder("Brittle Effigy", "Runeclaw Bear");
    }

    @Test
    @DisplayName("Can exile an artifact creature")
    void canExileArtifactCreature() {
        addReadyEffigy(player1);
        Permanent target = addCreatureReady(player2, new Juggernaut());
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Juggernaut");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(c -> c.getName().equals("Juggernaut"));
    }

    @Test
    @DisplayName("Rejects a noncreature artifact without paying costs")
    void cannotTargetNoncreatureArtifact() {
        Permanent effigy = addReadyEffigy(player1);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new BrittleEffigy());
        harness.addMana(player1, ManaColor.WHITE, 4);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(effigy.isTapped()).isFalse();
        harness.assertOnBattlefield(player1, "Brittle Effigy");
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(4);
        assertThat(gd.stack).isEmpty();
    }

    private Permanent addReadyEffigy(Player player) {
        return addCreatureReady(player, new BrittleEffigy());
    }
}

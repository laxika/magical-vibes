package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.t.Terminate;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({VedalkenGhoul.class, Terminate.class})
class VedalkenGhoulTest extends BaseCardTest {

    @Test
    @DisplayName("Becoming blocked makes the defending player lose 4 life")
    void blockedDrainsDefendingPlayer() {
        addAttackingGhoul(player1, player2);
        addCreatureReady(player2, new VedalkenGhoul());
        int startingLife = gd.getLife(player2.getId());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(startingLife - 4);
    }

    @Test
    @DisplayName("Unblocked Vedalken Ghoul does not fire the becomes-blocked drain (only combat damage)")
    void unblockedDoesNotDrain() {
        addAttackingGhoul(player1, player2);
        int startingLife = gd.getLife(player2.getId());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of());
        harness.passBothPriorities();

        // Only the 1 combat damage lands; the 4-life becomes-blocked drain never triggers.
        assertThat(gd.getLife(player2.getId())).isEqualTo(startingLife - 1);
    }

    @Test
    @DisplayName("Multiple blockers cause only one loss of 4 life")
    void multipleBlockersTriggerOnlyOnce() {
        addAttackingGhoul(player1, player2);
        addCreatureReady(player2, new VedalkenGhoul());
        addCreatureReady(player2, new VedalkenGhoul());
        int startingLife = gd.getLife(player2.getId());
        int attackerLife = gd.getLife(player1.getId());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0), new BlockerAssignment(1, 0)));
        resolveAllTriggers();

        assertThat(gd.getLife(player2.getId())).isEqualTo(startingLife - 4);
        assertThat(gd.getLife(player1.getId())).isEqualTo(attackerLife);
    }

    @Test
    @DisplayName("The defending player loses life when player two attacks")
    void reversedPlayersLoseLifeCorrectly() {
        addAttackingGhoul(player2, player1);
        addCreatureReady(player1, new VedalkenGhoul());
        int startingLife = gd.getLife(player1.getId());
        int attackerLife = gd.getLife(player2.getId());

        prepareDeclareBlockers(player2);
        gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(0, 0)));
        resolveAllTriggers();

        assertThat(gd.getLife(player1.getId())).isEqualTo(startingLife - 4);
        assertThat(gd.getLife(player2.getId())).isEqualTo(attackerLife);
    }

    @Test
    @DisplayName("The life-loss trigger resolves even after the Ghoul is destroyed")
    void triggerSurvivesSourceRemoval() {
        Permanent ghoul = addAttackingGhoul(player1, player2);
        addCreatureReady(player2, new VedalkenGhoul());
        harness.setHand(player2, List.of(new Terminate()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.RED, 1);
        int startingLife = gd.getLife(player2.getId());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        assertThat(gd.getLife(player2.getId())).isEqualTo(startingLife);
        harness.castInstant(player2, 0, ghoul.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Vedalken Ghoul");
        harness.assertInGraveyard(player1, "Vedalken Ghoul");
        resolveAllTriggers();
        assertThat(gd.getLife(player2.getId())).isEqualTo(startingLife - 4);
    }

    private Permanent addAttackingGhoul(Player attacker, Player defender) {
        Permanent perm = addCreatureReady(attacker, new VedalkenGhoul());
        perm.setAttacking(true);
        perm.setAttackTarget(defender.getId());
        return perm;
    }
}

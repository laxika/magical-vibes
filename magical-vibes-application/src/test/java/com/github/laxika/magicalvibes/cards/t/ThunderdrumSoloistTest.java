package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.Hurricane;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ThunderdrumSoloistTest extends BaseCardTest {

    private void addSoloist(Player player) {
        Permanent perm = new Permanent(new ThunderdrumSoloist());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
    }

    private void setUpMainPhase(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
    }

    

    @Test
    @DisplayName("Casting a cheap instant deals 1 damage to each opponent")
    void cheapSpellDealsOne() {
        addSoloist(player1);
        setUpMainPhase(player1);
        int startingLife = gd.getLife(player2.getId());

        harness.addMana(player1, ManaColor.RED, 1);
        harness.setHand(player1, List.of(new Shock()));
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities(); // resolve the Opus trigger
        harness.passBothPriorities(); // resolve Shock itself

        // Shock also deals 2 to player2; the trigger adds 1 more.
        assertThat(gd.getLife(player2.getId())).isEqualTo(startingLife - 2 - 1);
    }

    @Test
    @DisplayName("Casting a four-mana spell deals 1 damage to each opponent (below threshold)")
    void fourManaSpellDealsOne() {
        addSoloist(player1);
        setUpMainPhase(player1);
        int startingLife = gd.getLife(player2.getId());

        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.setHand(player1, List.of(new Hurricane()));
        harness.castSorcery(player1, 0, 3);
        harness.passBothPriorities(); // resolve the Opus trigger
        harness.passBothPriorities(); // resolve Hurricane itself

        // Hurricane deals X=3 to each player; the trigger adds 1 more to the opponent.
        assertThat(gd.getLife(player2.getId())).isEqualTo(startingLife - 3 - 1);
    }

    @Test
    @DisplayName("Casting a five-mana spell deals 3 damage to each opponent instead")
    void fiveManaSpellDealsThree() {
        addSoloist(player1);
        setUpMainPhase(player1);
        int startingLife = gd.getLife(player2.getId());

        harness.addMana(player1, ManaColor.GREEN, 5);
        harness.setHand(player1, List.of(new Hurricane()));
        harness.castSorcery(player1, 0, 4);
        harness.passBothPriorities(); // resolve the Opus trigger
        harness.passBothPriorities(); // resolve Hurricane itself

        // Hurricane deals X=4 to each player; the trigger adds 3 more to the opponent.
        assertThat(gd.getLife(player2.getId())).isEqualTo(startingLife - 4 - 3);
    }

    @Test
    @DisplayName("Casting a creature spell does not trigger the ability")
    void creatureSpellDoesNotTrigger() {
        addSoloist(player1);
        setUpMainPhase(player1);
        int startingLife = gd.getLife(player2.getId());

        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(startingLife);
    }
}

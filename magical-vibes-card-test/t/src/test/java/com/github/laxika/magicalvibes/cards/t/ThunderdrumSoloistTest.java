package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.Hurricane;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ThunderdrumSoloist.class, Shock.class, Hurricane.class, GrizzlyBears.class})
class ThunderdrumSoloistTest extends BaseCardTest {

    private void addSoloist(Player player) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new ThunderdrumSoloist());
        perm.setSummoningSick(false);
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

    @Test
    @DisplayName("An opponent's instant does not trigger Opus")
    void opponentSpellDoesNotTrigger() {
        addSoloist(player1);
        setUpMainPhase(player2);
        harness.addMana(player2, ManaColor.RED, 1);
        harness.setHand(player2, List.of(new Shock()));

        harness.castAndResolveInstant(player2, 0, player1.getId());

        harness.assertLife(player1, 18);
        harness.assertLife(player2, 20);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Extra mana in the pool does not upgrade the damage")
    void unspentManaDoesNotCount() {
        addSoloist(player1);
        setUpMainPhase(player1);
        harness.addMana(player1, ManaColor.RED, 5);
        harness.setHand(player1, List.of(new Shock()));

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 19);
        harness.passBothPriorities();
        harness.assertLife(player2, 17);
    }

    @Test
    @DisplayName("Opus still deals damage when its source dies before resolution")
    void triggerSurvivesSourceRemoval() {
        Permanent soloist = harness.addToBattlefieldAndReturn(player1, new ThunderdrumSoloist());
        setUpMainPhase(player1);
        harness.addMana(player1, ManaColor.GREEN, 5);
        harness.setHand(player1, List.of(new Hurricane()));
        harness.castSorcery(player1, 0, 4);

        harness.addMana(player2, ManaColor.RED, 2);
        harness.setHand(player2, List.of(new Shock(), new Shock()));
        harness.castAndResolveInstant(player2, 0, soloist.getId());
        harness.castAndResolveInstant(player2, 0, soloist.getId());
        harness.assertInGraveyard(player1, "Thunderdrum Soloist");

        harness.passBothPriorities();
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 17);
        harness.passBothPriorities();
        harness.assertLife(player1, 16);
        harness.assertLife(player2, 13);
    }
}

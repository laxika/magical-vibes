package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CaptivatingGlanceTest extends BaseCardTest {

    /** Attaches Captivating Glance (controlled by {@code auraController}) to {@code creature}. */
    private void attachGlance(Player auraController, Permanent creature) {
        Permanent glance = new Permanent(new CaptivatingGlance());
        glance.setAttachedTo(creature.getId());
        gd.playerBattlefields.get(auraController.getId()).add(glance);
    }

    /** Runs player1 through their end step so the controller-end-step clash trigger resolves. */
    private void runPlayer1EndStep() {
        harness.forceActivePlayer(player1);
        harness.setHand(player1, java.util.List.of());
        harness.setHand(player2, java.util.List.of());
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        gs.advanceStep(gd); // POSTCOMBAT_MAIN -> END_STEP, clash trigger onto stack
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // resolve clash + control change
    }

    private Permanent addCreature(Player owner) {
        Permanent perm = new Permanent(new GrizzlyBears());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(owner.getId()).add(perm);
        return perm;
    }

    // ===== Won clash — controller gains control =====

    @Test
    @DisplayName("Winning the clash gives the controller control of the enchanted creature")
    void wonClashGainsControl() {
        Permanent creature = addCreature(player2);
        attachGlance(player1, creature);

        // player1 reveals higher mana value (Grizzly Bears MV 2 > Forest MV 0) → player1 wins.
        gd.playerDecks.get(player1.getId()).addFirst(new GrizzlyBears());
        gd.playerDecks.get(player2.getId()).addFirst(new Forest());

        runPlayer1EndStep();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(creature);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(creature);
    }

    // ===== Lost clash — the clash opponent gains control =====

    @Test
    @DisplayName("Losing the clash gives the opponent control of the enchanted creature")
    void lostClashOpponentGainsControl() {
        Permanent creature = addCreature(player1);
        attachGlance(player1, creature);

        // player1 reveals lower mana value (Forest MV 0 < Grizzly Bears MV 2) → player1 loses.
        gd.playerDecks.get(player1.getId()).addFirst(new Forest());
        gd.playerDecks.get(player2.getId()).addFirst(new GrizzlyBears());

        runPlayer1EndStep();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(creature);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(creature);
    }

    // ===== Tie — not a win, so the opponent gains control =====

    @Test
    @DisplayName("A tie counts as 'otherwise', so the opponent gains control")
    void tiedClashOpponentGainsControl() {
        Permanent creature = addCreature(player1);
        attachGlance(player1, creature);

        // Equal mana values (both Grizzly Bears MV 2) → no one wins the clash (CR 701.29c).
        gd.playerDecks.get(player1.getId()).addFirst(new GrizzlyBears());
        gd.playerDecks.get(player2.getId()).addFirst(new GrizzlyBears());

        runPlayer1EndStep();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(creature);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(creature);
    }

    // ===== Only triggers on the aura controller's end step =====

    @Test
    @DisplayName("Does not clash on the opponent's end step")
    void doesNotTriggerOnOpponentEndStep() {
        Permanent creature = addCreature(player2);
        attachGlance(player1, creature);

        // Even though player1 would win, it is player2's end step, so nothing happens.
        gd.playerDecks.get(player1.getId()).addFirst(new GrizzlyBears());
        gd.playerDecks.get(player2.getId()).addFirst(new Forest());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        gs.advanceStep(gd);

        assertThat(gd.currentStep).isEqualTo(TurnStep.END_STEP);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(creature);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(creature);
    }
}

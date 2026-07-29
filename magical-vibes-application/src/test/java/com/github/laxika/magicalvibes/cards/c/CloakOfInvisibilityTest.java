package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.a.AngelicWall;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CloakOfInvisibilityTest extends BaseCardTest {

    @Test
    @DisplayName("Enchanted creature can't be blocked by a non-Wall creature")
    void cannotBeBlockedByNonWall() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setAttacking(true);
        enchant(attacker);

        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());

        beginDeclareBlockers();

        int blockerIdx = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIdx = gd.playerBattlefields.get(player1.getId()).indexOf(attacker);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(blockerIdx, attackerIdx))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Enchanted creature can be blocked by a Wall")
    void canBeBlockedByWall() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setAttacking(true);
        enchant(attacker);

        Permanent blocker = addCreatureReady(player2, new AngelicWall());

        beginDeclareBlockers();

        int blockerIdx = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIdx = gd.playerBattlefields.get(player1.getId()).indexOf(attacker);

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIdx, attackerIdx)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Enchanted creature phases out during its controller's untap step, taking the Cloak with it")
    void phasesOutDuringControllersUntapStep() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        Permanent cloak = enchant(bears);

        harness.forceActivePlayer(player1);
        advanceTurn(); // opponent's untap step — nothing of player1's phases
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(bears, cloak);

        advanceTurn(); // player1's untap step

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(bears, cloak);
        assertThat(gd.phasedOutPermanents.get(player1.getId())).contains(bears, cloak);
        assertThat(cloak.isPhasedOutIndirectly()).isTrue();
        assertThat(bears.isPhasedOutIndirectly()).isFalse();
    }

    @Test
    @DisplayName("Phased-out creature phases back in on its controller's next untap step")
    void phasesBackInOnNextUntapStep() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        Permanent cloak = enchant(bears);

        harness.forceActivePlayer(player1);
        advanceTurn();
        advanceTurn(); // phases out
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(bears);

        advanceTurn();
        advanceTurn(); // player1's next untap step

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(bears, cloak);
        assertThat(gd.phasedOutPermanents.get(player1.getId())).isEmpty();
        assertThat(cloak.isPhasedOutIndirectly()).isFalse();
        assertThat(cloak.getAttachedTo()).isEqualTo(bears.getId());
    }

    @Test
    @DisplayName("A phased-out creature is treated as though it doesn't exist")
    void phasedOutCreatureDoesNotExist() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        enchant(bears);

        harness.forceActivePlayer(player1);
        advanceTurn();
        advanceTurn();

        assertThat(gqs.findPermanentById(gd, bears.getId())).isNull();
        assertThat(gd.anyPermanentMatches(permanent -> permanent.getId().equals(bears.getId()))).isFalse();
    }

    private Permanent enchant(Permanent host) {
        Permanent cloak = new Permanent(new CloakOfInvisibility());
        cloak.setAttachedTo(host.getId());
        gd.playerBattlefields.get(player1.getId()).add(cloak);
        return cloak;
    }

    private void advanceTurn() {
        harness.forceStep(TurnStep.CLEANUP);
        harness.passBothPriorities();
    }

    private void beginDeclareBlockers() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();
    }
}

package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.t.ThrabenValiant;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HuntedGhoul.class, ThrabenValiant.class})
class HuntedGhoulTest extends BaseCardTest {

    @Test
    @DisplayName("Hunted Ghoul can block a non-Human attacker")
    void canBlockNonHuman() {
        Permanent ghoul = harness.addToBattlefieldAndReturn(player2, new HuntedGhoul());
        ghoul.setSummoningSick(false);

        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new HuntedGhoul());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(ghoul.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Hunted Ghoul cannot block a Human attacker")
    void cannotBlockHuman() {
        Permanent ghoul = harness.addToBattlefieldAndReturn(player2, new HuntedGhoul());
        ghoul.setSummoningSick(false);

        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new ThrabenValiant());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can only block creatures that aren't Humans");
    }

    @Test
    @DisplayName("A newly entered Hunted Ghoul can block a non-Human")
    void summoningSickGhoulCanBlockNonHuman() {
        Permanent ghoul = harness.addToBattlefieldAndReturn(player2, new HuntedGhoul());
        ghoul.setSummoningSick(true);
        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new HuntedGhoul());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(ghoul.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("A Human can block an attacking Hunted Ghoul")
    void humanCanBlockGhoul() {
        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new ThrabenValiant());
        Permanent ghoul = harness.addToBattlefieldAndReturn(player1, new HuntedGhoul());
        ghoul.setSummoningSick(false);
        ghoul.setAttacking(true);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }
}

package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.u.Unsummon;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ScoriaWurm.class, Unsummon.class})
class ScoriaWurmTest extends BaseCardTest {

    @Test
    @DisplayName("Triggers during controller upkeep")
    void triggersDuringControllerUpkeep() {
        harness.addToBattlefield(player1, new ScoriaWurm());

        advanceToUpkeep(player1);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getDescription()).contains("Scoria Wurm's upkeep ability");
    }

    @Test
    @DisplayName("Does not trigger during opponent upkeep")
    void doesNotTriggerDuringOpponentUpkeep() {
        harness.addToBattlefield(player1, new ScoriaWurm());

        advanceToUpkeep(player2);

        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Triggers during the current controller's upkeep even when the owner is another player")
    void triggersDuringControllerUpkeepWhenOwnerDiffers() {
        ScoriaWurm wurm = new ScoriaWurm();
        wurm.setOwnerId(player1.getId());
        harness.addToBattlefield(player2, wurm);

        advanceToUpkeep(player2);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getDescription()).contains("Scoria Wurm's upkeep ability");
    }

    @Test
    @DisplayName("Upkeep resolution flips a coin and Scoria Wurm ends in exactly one legal zone")
    void upkeepResolutionFlipsCoinAndMovesOrStays() {
        harness.addToBattlefield(player1, new ScoriaWurm());
        int handBefore = gd.playerHands.get(player1.getId()).size();

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        boolean won = gameLogContains("wins the coin flip for Scoria Wurm");
        boolean lost = gameLogContains("loses the coin flip for Scoria Wurm");
        assertThat(won).isNotEqualTo(lost);

        if (lost) {
            harness.assertNotOnBattlefield(player1, "Scoria Wurm");
            harness.assertInHand(player1, "Scoria Wurm");
            assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
        } else {
            harness.assertOnBattlefield(player1, "Scoria Wurm");
            harness.assertNotInHand(player1, "Scoria Wurm");
            assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);
        }

        assertThat(gameLogContains("coin flip for Scoria Wurm")).isTrue();
    }
    @Test
    @DisplayName("A lost flip returns Scoria Wurm to its owner rather than its controller")
    void resolutionUsesOwnersHand() {
        ScoriaWurm wurm = new ScoriaWurm();
        wurm.setOwnerId(player1.getId());
        harness.addToBattlefield(player2, wurm);
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        boolean lost = gameLogContains("loses the coin flip for Scoria Wurm");
        boolean won = gameLogContains("wins the coin flip for Scoria Wurm");
        assertThat(won).isNotEqualTo(lost);
        if (lost) {
            harness.assertNotOnBattlefield(player2, "Scoria Wurm");
            assertThat(gd.playerHands.get(player1.getId())).containsExactly(wurm);
        } else {
            harness.assertOnBattlefield(player2, "Scoria Wurm");
            assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        }
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("The upkeep ability still flips a coin after Scoria Wurm leaves the battlefield")
    void flipsAfterSourceLeavesBattlefield() {
        ScoriaWurm wurm = new ScoriaWurm();
        harness.addToBattlefield(player1, wurm);
        harness.setHand(player1, List.of(new Unsummon()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        advanceToUpkeep(player1);
        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player1, "Scoria Wurm"));

        assertThat(gd.stack).hasSize(1);
        assertThat(gameLogContains("coin flip for Scoria Wurm")).isFalse();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(wurm);

        harness.passBothPriorities();

        boolean won = gameLogContains("wins the coin flip for Scoria Wurm");
        boolean lost = gameLogContains("loses the coin flip for Scoria Wurm");
        assertThat(won).isNotEqualTo(lost);
        harness.assertNotOnBattlefield(player1, "Scoria Wurm");
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(wurm);
    }

    @Test
    @DisplayName("Scoria Wurm entering after upkeep begins does not trigger retroactively")
    void enteringDuringUpkeepDoesNotTrigger() {
        advanceToUpkeep(player1);

        harness.enterBattlefieldAndReturn(player1, new ScoriaWurm());

        assertThat(gd.stack).isEmpty();
        assertThat(gameLogContains("coin flip for Scoria Wurm")).isFalse();
        harness.assertOnBattlefield(player1, "Scoria Wurm");
    }
}


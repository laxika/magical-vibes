package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.u.Unsummon;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SleeperAgent.class, Unsummon.class})
class SleeperAgentTest extends BaseCardTest {

    private void castSleeperAgent(java.util.UUID targetPlayerId) {
        harness.setHand(player1, List.of(new SleeperAgent()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castCreature(player1, 0, targetPlayerId);
    }

    @Test
    @DisplayName("ETB trigger gives control to target opponent")
    void etbGivesControlToTargetOpponent() {
        castSleeperAgent(player2.getId());

        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Sleeper Agent");
        harness.assertOnBattlefield(player2, "Sleeper Agent");
        assertThat(gameLogContains("gains control of Sleeper Agent")).isTrue();
    }

    @Test
    @DisplayName("Sleeper Agent deals 2 damage to its current controller during that player's upkeep")
    void upkeepDamagesCurrentController() {
        castSleeperAgent(player2.getId());
        resolveAllTriggers();

        int p1LifeBefore = gd.playerLifeTotals.get(player1.getId());
        int p2LifeBefore = gd.playerLifeTotals.get(player2.getId());

        advanceToUpkeep(player2);
        harness.passBothPriorities(); // resolve upkeep trigger

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(p2LifeBefore - 2);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(p1LifeBefore);
    }

    @Test
    @DisplayName("Sleeper Agent does not trigger during non-controller upkeep")
    void doesNotTriggerDuringNonControllerUpkeep() {
        castSleeperAgent(player2.getId());
        resolveAllTriggers();

        int p2LifeBefore = gd.playerLifeTotals.get(player2.getId());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(p2LifeBefore);
    }

    @Test
    @DisplayName("Cannot cast Sleeper Agent by targeting yourself")
    void cannotTargetSelf() {
        harness.setHand(player1, List.of(new SleeperAgent()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an opponent");
    }

    @Test
    @DisplayName("Returning Sleeper Agent before its ETB trigger resolves prevents the control handoff")
    void returningSourceBeforeEtbResolutionPreventsControlHandoff() {
        castSleeperAgent(player2.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Sleeper Agent");
        assertThat(gd.stack).hasSize(1);

        harness.setHand(player1, List.of(new Unsummon()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castInstant(player1, 0, harness.getPermanentId(player1, "Sleeper Agent"));
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Sleeper Agent");
        harness.assertNotOnBattlefield(player2, "Sleeper Agent");
        harness.assertInHand(player1, "Sleeper Agent");
        harness.assertNotInHand(player2, "Sleeper Agent");
    }

    @Test
    @DisplayName("Upkeep damage still resolves after Sleeper Agent returns to its owner's hand")
    void upkeepDamageResolvesAfterSourceLeavesBattlefield() {
        castSleeperAgent(player2.getId());
        resolveAllTriggers();
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        advanceToUpkeep(player2);
        assertThat(gd.stack).hasSize(1);

        harness.setHand(player2, List.of(new Unsummon()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.castInstant(player2, 0, harness.getPermanentId(player2, "Sleeper Agent"));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Sleeper Agent");
        harness.assertInHand(player1, "Sleeper Agent");
        harness.assertLife(player2, 20);

        resolveAllTriggers();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 18);
    }
}

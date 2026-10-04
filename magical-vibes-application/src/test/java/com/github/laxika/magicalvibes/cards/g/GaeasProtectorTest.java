package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.b.BalothGorger;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GaeasProtector.class, BalothGorger.class})
class GaeasProtectorTest extends BaseCardTest {

    @Test
    @DisplayName("At least one creature must block Gaea's Protector if able")
    void mustBeBlockedByAtLeastOne() {
        Permanent protector = addCreatureReady(player1, new GaeasProtector());
        protector.setAttacking(true);

        addCreatureReady(player2, new BalothGorger());
        addCreatureReady(player2, new BalothGorger());

        prepareDeclareBlockers();

        // No blockers assigned — should fail because at least one must block
        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must be blocked if able");
    }

    @Test
    @DisplayName("Blocking with one creature satisfies the requirement")
    void oneBlockerSuffices() {
        Permanent protector = addCreatureReady(player1, new GaeasProtector());
        protector.setAttacking(true);

        addCreatureReady(player2, new BalothGorger());
        addCreatureReady(player2, new BalothGorger());

        prepareDeclareBlockers();

        // One blocker assigned — should succeed (unlike Lure which requires all)
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(gd.playerBattlefields.get(player2.getId()).get(0).isBlocking()).isTrue();
        assertThat(gd.playerBattlefields.get(player2.getId()).get(1).isBlocking()).isFalse();
    }

    @Test
    @DisplayName("Tapped creatures are not forced to block Gaea's Protector")
    void tappedCreaturesNotForcedToBlock() {
        Permanent protector = addCreatureReady(player1, new GaeasProtector());
        protector.setAttacking(true);

        Permanent tapped = addCreatureReady(player2, new BalothGorger());
        tapped.tap();

        prepareDeclareBlockers();

        // Only blocker is tapped, so no block is required
        gs.declareBlockers(gd, player2, List.of());
    }

    @Test
    @DisplayName("Defender can choose which creature blocks Gaea's Protector")
    void defenderChoosesWhichCreatureBlocks() {
        Permanent protector = addCreatureReady(player1, new GaeasProtector());
        protector.setAttacking(true);

        addCreatureReady(player2, new BalothGorger());
        addCreatureReady(player2, new BalothGorger());

        prepareDeclareBlockers();

        // Block with second creature instead of first — should succeed
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(1, 0)));

        assertThat(gd.playerBattlefields.get(player2.getId()).get(0).isBlocking()).isFalse();
        assertThat(gd.playerBattlefields.get(player2.getId()).get(1).isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Two available blockers must block both attacking Protectors")
    void cannotDoubleBlockOneProtectorAndLeaveAnotherUnblocked() {
        addCreatureReady(player1, new GaeasProtector()).setAttacking(true);
        addCreatureReady(player1, new GaeasProtector()).setAttacking(true);
        addCreatureReady(player2, new BalothGorger());
        addCreatureReady(player2, new BalothGorger());
        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0), new BlockerAssignment(1, 0))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("One blocker may choose either of two attacking Protectors")
    void oneBlockerCanSatisfyOnlyOneProtectorRequirement() {
        addCreatureReady(player1, new GaeasProtector()).setAttacking(true);
        addCreatureReady(player1, new GaeasProtector()).setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new BalothGorger());
        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 1)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("An available blocker cannot block another attacker instead of Protector")
    void cannotDivertBlockerToOrdinaryAttacker() {
        addCreatureReady(player1, new GaeasProtector()).setAttacking(true);
        addCreatureReady(player1, new BalothGorger()).setAttacking(true);
        addCreatureReady(player2, new BalothGorger());
        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 1))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must be blocked if able");
    }

    @Test
    @DisplayName("Protector may attack unblocked when the defender controls no creatures")
    void noBlockRequiredWithoutCreatures() {
        addCreatureReady(player1, new GaeasProtector()).setAttacking(true);
        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of());
    }
}

package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.s.StonybrookSchoolmaster;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CennsTactician.class, StonybrookSchoolmaster.class})
class CennsTacticianTest extends BaseCardTest {

    // ===== Activated ability =====

    @Test
    @DisplayName("{W}, {T}: Put a +1/+1 counter on a target Soldier creature")
    void abilityPutsCounterOnSoldier() {
        // Cenn's Tactician is itself a Kithkin Soldier, so it is a legal target.
        Permanent tactician = addCreatureReady(player1, new CennsTactician());
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, tactician.getId());
        harness.passBothPriorities();

        assertThat(tactician.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot target a non-Soldier creature")
    void cannotTargetNonSoldier() {
        addCreatureReady(player1, new CennsTactician());
        Permanent nonSoldier = addCreatureReady(player1, new StonybrookSchoolmaster());
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, nonSoldier.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can target a Soldier creature an opponent controls")
    void canTargetOpponentsSoldier() {
        addCreatureReady(player1, new CennsTactician());
        Permanent opponentSoldier = addCreatureReady(player2, new CennsTactician());
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, opponentSoldier.getId());
        harness.passBothPriorities();

        assertThat(opponentSoldier.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot activate without paying {W}")
    void cannotActivateWithoutWhiteMana() {
        Permanent tactician = addCreatureReady(player1, new CennsTactician());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, tactician.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot activate when tapped")
    void cannotActivateWhenTapped() {
        Permanent tactician = addCreatureReady(player1, new CennsTactician());
        tactician.tap();
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, tactician.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    // ===== Static: additional block for creatures with a +1/+1 counter =====

    @Test
    @DisplayName("A creature you control with a +1/+1 counter can block an additional creature")
    void counteredCreatureCanBlockTwo() {
        addCreatureReady(player2, new CennsTactician());
        Permanent blocker = addCreatureReady(player2, new StonybrookSchoolmaster());
        blocker.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        addAttackers(2);

        prepareDeclareBlockers();
        int blockerIdx = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);

        assertThatCode(() -> gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(blockerIdx, 0),
                new BlockerAssignment(blockerIdx, 1)
        ))).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("A creature you control without a +1/+1 counter cannot block an additional creature")
    void uncounteredCreatureCannotBlockTwo() {
        addCreatureReady(player2, new CennsTactician());
        Permanent blocker = addCreatureReady(player2, new StonybrookSchoolmaster());
        addAttackers(2);

        prepareDeclareBlockers();
        int blockerIdx = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(blockerIdx, 0),
                new BlockerAssignment(blockerIdx, 1)
        )))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("too many times");
    }

    @Test
    @DisplayName("A creature with a different counter cannot block an additional creature")
    void otherCounterDoesNotGrantAdditionalBlock() {
        addCreatureReady(player2, new CennsTactician());
        Permanent blocker = addCreatureReady(player2, new StonybrookSchoolmaster());
        blocker.setCounterCount(CounterType.STUN, 1);
        addAttackers(2);

        prepareDeclareBlockers();
        int blockerIdx = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(blockerIdx, 0),
                new BlockerAssignment(blockerIdx, 1)
        )))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("too many times");
    }

    // ===== Helpers =====

    private void addAttackers(int count) {
        for (int i = 0; i < count; i++) {
            Permanent atk = new Permanent(new StonybrookSchoolmaster());
            atk.setSummoningSick(false);
            atk.setAttacking(true);
            gd.playerBattlefields.get(player1.getId()).add(atk);
        }
    }

}

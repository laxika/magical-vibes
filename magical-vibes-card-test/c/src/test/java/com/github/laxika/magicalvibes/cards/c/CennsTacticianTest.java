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

    @Test
    void activationPaysWhiteManaAndTapsSource() {
        Permanent tactician = addCreatureReady(player1, new CennsTactician());
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, tactician.getId());

        assertThat(tactician.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isZero();
        harness.passBothPriorities();
        assertThat(tactician.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void summoningSickTacticianCannotActivate() {
        Permanent tactician = addCreatureReady(player1, new CennsTactician());
        tactician.setSummoningSick(true);
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, tactician.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void multipleCountersStillAllowOnlyOneAdditionalBlock() {
        addCreatureReady(player2, new CennsTactician());
        Permanent blocker = addCreatureReady(player2, new StonybrookSchoolmaster());
        blocker.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);
        addAttackers(3);
        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(1, 0),
                new BlockerAssignment(1, 1),
                new BlockerAssignment(1, 2))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("too many times");
    }

    @Test
    void multipleTacticiansEachGrantAnAdditionalBlock() {
        addCreatureReady(player2, new CennsTactician());
        addCreatureReady(player2, new CennsTactician());
        Permanent blocker = addCreatureReady(player2, new StonybrookSchoolmaster());
        blocker.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        addAttackers(3);
        prepareDeclareBlockers();

        assertThatCode(() -> gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(2, 0),
                new BlockerAssignment(2, 1),
                new BlockerAssignment(2, 2)))).doesNotThrowAnyException();
    }

    @Test
    void tacticianCanBenefitFromItsOwnStaticAbility() {
        Permanent blocker = addCreatureReady(player2, new CennsTactician());
        blocker.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        addAttackers(2);
        prepareDeclareBlockers();

        assertThatCode(() -> gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(0, 1)))).doesNotThrowAnyException();
    }

    @Test
    void opponentsTacticianDoesNotGrantAdditionalBlocks() {
        addCreatureReady(player1, new CennsTactician());
        Permanent blocker = addCreatureReady(player2, new StonybrookSchoolmaster());
        blocker.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        addAttackers(2);
        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 1),
                new BlockerAssignment(0, 2))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("too many times");
    }

    @Test
    void activatedAbilityResolvesAfterSourceLeaves() {
        Permanent source = addCreatureReady(player1, new CennsTactician());
        Permanent target = addCreatureReady(player2, new CennsTactician());
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.activateAbility(player1, 0, null, target.getId());
        gd.playerBattlefields.get(player1.getId()).remove(source);
        gd.playerGraveyards.get(player1.getId()).add(source.getCard());

        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void removingLastCounterEndsAdditionalBlockPermission() {
        addCreatureReady(player2, new CennsTactician());
        Permanent blocker = addCreatureReady(player2, new StonybrookSchoolmaster());
        blocker.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        addAttackers(2);
        blocker.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 0);
        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(1, 0),
                new BlockerAssignment(1, 1))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("too many times");
    }

    @Test
    void additionalBlockPermissionEndsWhenTacticianLeaves() {
        Permanent tactician = addCreatureReady(player2, new CennsTactician());
        Permanent blocker = addCreatureReady(player2, new StonybrookSchoolmaster());
        blocker.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        addAttackers(2);
        gd.playerBattlefields.get(player2.getId()).remove(tactician);
        gd.playerGraveyards.get(player2.getId()).add(tactician.getCard());
        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(0, 1))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("too many times");
    }

    private void addAttackers(int count) {
        for (int i = 0; i < count; i++) {
            Permanent atk = addCreatureReady(player1, new StonybrookSchoolmaster());
            atk.setAttacking(true);
        }
    }

}

package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.w.WalkingCorpse;
import com.github.laxika.magicalvibes.cards.w.WindDrake;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PredatoryRampage.class, WalkingCorpse.class, WindDrake.class})
class PredatoryRampageTest extends BaseCardTest {

    @Test
    @DisplayName("Boosts creatures you control +3/+3 and leaves opponent's creatures alone")
    void boostsOwnCreaturesOnly() {
        Permanent own = addCreatureReady(player1, new WalkingCorpse());
        Permanent theirs = addCreatureReady(player2, new WalkingCorpse());
        castRampage();

        assertThat(own.getEffectivePower()).isEqualTo(5);
        assertThat(own.getEffectiveToughness()).isEqualTo(5);
        assertThat(theirs.getEffectivePower()).isEqualTo(2);
        assertThat(theirs.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Opponent's creatures must block if able")
    void opponentCreaturesMustBlock() {
        Permanent attacker = addCreatureReady(player1, new WalkingCorpse());
        Permanent blocker = addCreatureReady(player2, new WalkingCorpse());
        castRampage();

        assertThat(blocker.isMustBlockThisTurnIfAble()).isTrue();

        beginCombat(attacker);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must block");
    }

    @Test
    @DisplayName("Declaring the forced block is legal")
    void forcedBlockCanBeSatisfied() {
        Permanent attacker = addCreatureReady(player1, new WalkingCorpse());
        addCreatureReady(player2, new WalkingCorpse());
        castRampage();

        beginCombat(attacker);

        assertThatCode(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Does not force the caster's own creatures to block")
    void doesNotForceOwnCreatures() {
        Permanent own = addCreatureReady(player1, new WalkingCorpse());
        castRampage();

        assertThat(own.isMustBlockThisTurnIfAble()).isFalse();
    }

    @Test
    @DisplayName("Boost and block requirement wear off at end of turn")
    void wearsOffAtEndOfTurn() {
        Permanent own = addCreatureReady(player1, new WalkingCorpse());
        Permanent theirs = addCreatureReady(player2, new WalkingCorpse());
        castRampage();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(own.getEffectivePower()).isEqualTo(2);
        assertThat(theirs.isMustBlockThisTurnIfAble()).isFalse();
    }

    @Test
    @DisplayName("Resolves with no creatures on the battlefield")
    void resolvesWithEmptyBattlefield() {
        castRampage();

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Predatory Rampage");
    }

    @Test
    @DisplayName("Creatures entering under your control after resolution do not receive the boost")
    void laterOwnCreatureIsNotBoosted() {
        castRampage();

        Permanent later = addCreatureReady(player1, new WalkingCorpse());

        assertThat(later.getEffectivePower()).isEqualTo(2);
        assertThat(later.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Opponent creatures entering after resolution must still block")
    void laterOpponentCreatureMustBlock() {
        Permanent attacker = addCreatureReady(player1, new WalkingCorpse());
        castRampage();
        harness.addToBattlefield(player2, new WalkingCorpse());

        beginCombat(attacker);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must block");
        assertThatCode(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Tapped opponent creatures are not required to block")
    void tappedCreatureNeedNotBlock() {
        Permanent attacker = addCreatureReady(player1, new WalkingCorpse());
        Permanent tapped = addCreatureReady(player2, new WalkingCorpse());
        tapped.setTapped(true);
        castRampage();

        beginCombat(attacker);

        assertThatCode(() -> gs.declareBlockers(gd, player2, List.of()))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Ground creatures need not block a flying attacker")
    void creatureUnableToBlockFlyingNeedNotBlock() {
        Permanent attacker = addCreatureReady(player1, new WindDrake());
        addCreatureReady(player2, new WalkingCorpse());
        castRampage();

        beginCombat(attacker);

        assertThatCode(() -> gs.declareBlockers(gd, player2, List.of()))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Every able opponent creature must block, even when one already blocks")
    void eachAbleCreatureMustBlock() {
        Permanent attacker = addCreatureReady(player1, new WalkingCorpse());
        addCreatureReady(player2, new WalkingCorpse());
        addCreatureReady(player2, new WalkingCorpse());
        castRampage();

        beginCombat(attacker);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must block");
        assertThatCode(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0), new BlockerAssignment(1, 0))))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("The turn-wide requirement no longer affects later creatures after cleanup")
    void laterCreatureNeedNotBlockAfterCleanup() {
        castRampage();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        Permanent attacker = addCreatureReady(player1, new WalkingCorpse());
        addCreatureReady(player2, new WalkingCorpse());

        beginCombat(attacker);

        assertThatCode(() -> gs.declareBlockers(gd, player2, List.of()))
                .doesNotThrowAnyException();
    }

    private void castRampage() {
        harness.setHand(player1, List.of(new PredatoryRampage()));
        harness.addMana(player1, ManaColor.GREEN, 5);
        harness.castAndResolveSorcery(player1, 0, 0);
    }

    private void beginCombat(Permanent attacker) {
        attacker.setAttacking(true);
        prepareDeclareBlockers();
    }
}

package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.a.ActOfHeroism;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Pacifism;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ValorMadeReal.class, GrizzlyBears.class, Pacifism.class, ActOfHeroism.class})
class ValorMadeRealTest extends BaseCardTest {

    @Test
    @DisplayName("Target creature can block any number of attackers")
    void targetCreatureCanBlockAnyNumberOfAttackers() {
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        int blockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        addAttacker();
        addAttacker();
        addAttacker();

        castValorMadeReal(blocker);
        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(blockerIndex, 0),
                new BlockerAssignment(blockerIndex, 1),
                new BlockerAssignment(blockerIndex, 2)
        ));

        assertThat(blocker.getBlockingTargets()).containsExactlyInAnyOrder(0, 1, 2);
    }

    @Test
    @DisplayName("Valor Made Real's effect expires at end of turn")
    void effectExpiresAtEndOfTurn() {
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        castValorMadeReal(blocker);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(blocker.getAdditionalBlocksUntilEndOfTurn()).isZero();
    }

    @Test
    @DisplayName("Valor Made Real cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        Permanent noncreature = harness.addToBattlefieldAndReturn(player2, new Pacifism());
        harness.setHand(player1, List.of(new ValorMadeReal()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, noncreature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("A later additional-block grant preserves unlimited blocking")
    void laterAdditionalBlockGrantPreservesUnlimitedBlocking() {
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        addAttacker();
        addAttacker();
        addAttacker();

        castValorMadeReal(blocker);
        harness.setHand(player1, List.of(new ActOfHeroism()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castAndResolveInstant(player1, 0, blocker.getId());
        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(0, 1),
                new BlockerAssignment(0, 2)
        ));

        assertThat(blocker.getBlockingTargets()).containsExactlyInAnyOrder(0, 1, 2);
    }

    @Test
    @DisplayName("Untargeted creatures retain their normal blocking limit")
    void untargetedCreatureRetainsNormalBlockingLimit() {
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());
        addAttacker();
        addAttacker();

        castValorMadeReal(target);
        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(1, 0),
                new BlockerAssignment(1, 1)
        ))).isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("assigned too many times");
    }

    @Test
    @DisplayName("Unlimited blocking does not let a tapped creature block")
    void tappedCreatureStillCannotBlock() {
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        blocker.tap();
        addAttacker();

        castValorMadeReal(blocker);
        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid blocker index");
    }

    @Test
    @DisplayName("Unlimited blocking does not override Pacifism")
    void cannotBlockRestrictionStillApplies() {
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        Permanent pacifism = harness.addToBattlefieldAndReturn(player2, new Pacifism());
        pacifism.setAttachedTo(blocker.getId());
        addAttacker();

        castValorMadeReal(blocker);
        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid blocker index");
    }

    private void castValorMadeReal(Permanent target) {
        harness.setHand(player1, List.of(new ValorMadeReal()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castAndResolveInstant(player1, 0, target.getId());
    }

    private void addAttacker() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setAttacking(true);
        attacker.setAttackTarget(player2.getId());
    }
}

package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.k.KraulWarrior;
import com.github.laxika.magicalvibes.cards.p.PaladinEnVec;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.BlockerAssignment;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ArmedDangerous.class, KraulWarrior.class, PaladinEnVec.class})
class ArmedDangerousTest extends BaseCardTest {

    private static final int ARMED = 0;
    private static final int DANGEROUS = 1;
    private static final int FUSE = 2;

    @Test
    @DisplayName("Armed boosts the target and grants double strike")
    void armedBoostsAndGrantsDoubleStrike() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new KraulWarrior());

        harness.setHand(player1, List.of(new ArmedDangerous()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveSorcery(player1, 0, ARMED, bears.getId());

        assertThat(bears.getEffectivePower()).isEqualTo(3);
        assertThat(bears.getEffectiveToughness()).isEqualTo(3);
        assertThat(bears.hasKeyword(Keyword.DOUBLE_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("Dangerous makes the target require all able blockers")
    void dangerousMakesTargetRequireAllBlockers() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new KraulWarrior());

        harness.setHand(player1, List.of(new ArmedDangerous()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castAndResolveSorcery(player1, 0, DANGEROUS, bears.getId());

        assertThat(bears.isMustBeBlockedByAllThisTurn()).isTrue();
    }

    @Test
    @DisplayName("Fuse resolves Armed and Dangerous on independent targets")
    void fuseUsesIndependentTargets() {
        Permanent armedTarget = harness.addToBattlefieldAndReturn(player1, new KraulWarrior());
        Permanent dangerousTarget = harness.addToBattlefieldAndReturn(player2, new KraulWarrior());

        harness.setHand(player1, List.of(new ArmedDangerous()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castModalSorcery(player1, 0, FUSE, List.of(armedTarget.getId(), dangerousTarget.getId()));
        harness.passBothPriorities();

        assertThat(armedTarget.getEffectivePower()).isEqualTo(3);
        assertThat(armedTarget.getEffectiveToughness()).isEqualTo(3);
        assertThat(armedTarget.hasKeyword(Keyword.DOUBLE_STRIKE)).isTrue();
        assertThat(armedTarget.isMustBeBlockedByAllThisTurn()).isFalse();
        assertThat(dangerousTarget.isMustBeBlockedByAllThisTurn()).isTrue();
    }

    @Test
    @DisplayName("Fuse allows both halves to target the same creature")
    void fuseAllowsSharedTarget() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new KraulWarrior());

        harness.setHand(player1, List.of(new ArmedDangerous()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castModalSorcery(player1, 0, FUSE, List.of(bears.getId(), bears.getId()));
        harness.passBothPriorities();

        assertThat(bears.getEffectivePower()).isEqualTo(3);
        assertThat(bears.getEffectiveToughness()).isEqualTo(3);
        assertThat(bears.hasKeyword(Keyword.DOUBLE_STRIKE)).isTrue();
        assertThat(bears.isMustBeBlockedByAllThisTurn()).isTrue();
    }

    @Test
    @DisplayName("Fuse requires the combined cost")
    void fuseRequiresCombinedCost() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new KraulWarrior());

        harness.setHand(player1, List.of(new ArmedDangerous()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castModalSorcery(player1, 0, FUSE,
                List.of(bears.getId(), bears.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void fusedEffectsExpireAtEndOfTurn() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new KraulWarrior());
        harness.setHand(player1, List.of(new ArmedDangerous()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castModalSorcery(player1, 0, FUSE, List.of(target.getId(), target.getId()));
        harness.passBothPriorities();
        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);

        assertThat(target.getEffectivePower()).isEqualTo(2);
        assertThat(target.getEffectiveToughness()).isEqualTo(2);
        assertThat(target.hasKeyword(Keyword.DOUBLE_STRIKE)).isFalse();
        assertThat(target.isMustBeBlockedByAllThisTurn()).isFalse();
    }

    @Test
    void dangerousRequiresEveryAbleBlockerButNotTappedCreatures() {
        Permanent attacker = addCreatureReady(player1, new KraulWarrior());
        Permanent firstBlocker = addCreatureReady(player2, new KraulWarrior());
        Permanent secondBlocker = addCreatureReady(player2, new KraulWarrior());
        Permanent tappedBlocker = addCreatureReady(player2, new KraulWarrior());
        tappedBlocker.tap();
        harness.setHand(player1, List.of(new ArmedDangerous()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castAndResolveSorcery(player1, 0, DANGEROUS, attacker.getId());

        attacker.setAttacking(true);
        prepareDeclareBlockers();
        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0), new BlockerAssignment(1, 0)));

        assertThat(firstBlocker.getBlockingTargetIds()).containsExactly(attacker.getId());
        assertThat(secondBlocker.getBlockingTargetIds()).containsExactly(attacker.getId());
        assertThat(tappedBlocker.isBlocking()).isFalse();
    }

    @Test
    void fuseStillResolvesDangerousWhenArmedTargetLeavesBattlefield() {
        Permanent armedTarget = harness.addToBattlefieldAndReturn(player1, new KraulWarrior());
        Permanent dangerousTarget = harness.addToBattlefieldAndReturn(player2, new KraulWarrior());
        harness.setHand(player1, List.of(new ArmedDangerous()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castModalSorcery(player1, 0, FUSE, List.of(armedTarget.getId(), dangerousTarget.getId()));

        gd.playerBattlefields.get(player1.getId()).remove(armedTarget);
        gd.playerGraveyards.get(player1.getId()).add(armedTarget.getCard());
        harness.passBothPriorities();

        assertThat(dangerousTarget.isMustBeBlockedByAllThisTurn()).isTrue();
        assertThat(dangerousTarget.getEffectivePower()).isEqualTo(2);
        assertThat(dangerousTarget.hasKeyword(Keyword.DOUBLE_STRIKE)).isFalse();
    }

    @Test
    void fuseStillResolvesArmedWhenDangerousTargetLeavesBattlefield() {
        Permanent armedTarget = harness.addToBattlefieldAndReturn(player1, new KraulWarrior());
        Permanent dangerousTarget = harness.addToBattlefieldAndReturn(player2, new KraulWarrior());
        harness.setHand(player1, List.of(new ArmedDangerous()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castModalSorcery(player1, 0, FUSE, List.of(armedTarget.getId(), dangerousTarget.getId()));

        gd.playerBattlefields.get(player2.getId()).remove(dangerousTarget);
        gd.playerGraveyards.get(player2.getId()).add(dangerousTarget.getCard());
        harness.passBothPriorities();

        assertThat(armedTarget.getEffectivePower()).isEqualTo(3);
        assertThat(armedTarget.getEffectiveToughness()).isEqualTo(3);
        assertThat(armedTarget.hasKeyword(Keyword.DOUBLE_STRIKE)).isTrue();
        assertThat(armedTarget.isMustBeBlockedByAllThisTurn()).isFalse();
    }

    @Test
    void dangerousCanTargetCreatureWithProtectionFromRed() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new PaladinEnVec());
        harness.setHand(player1, List.of(new ArmedDangerous()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castAndResolveSorcery(player1, 0, DANGEROUS, target.getId());

        assertThat(target.isMustBeBlockedByAllThisTurn()).isTrue();
    }
}

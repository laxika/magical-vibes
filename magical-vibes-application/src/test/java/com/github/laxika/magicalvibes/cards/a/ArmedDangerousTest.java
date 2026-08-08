package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ArmedDangerousTest extends BaseCardTest {

    private static final int ARMED = 0;
    private static final int DANGEROUS = 1;
    private static final int FUSE = 2;

    @Test
    @DisplayName("Armed boosts the target and grants double strike")
    void armedBoostsAndGrantsDoubleStrike() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.setHand(player1, List.of(new ArmedDangerous()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castSorcery(player1, 0, ARMED, bears.getId());
        harness.passBothPriorities();

        assertThat(bears.getEffectivePower()).isEqualTo(3);
        assertThat(bears.getEffectiveToughness()).isEqualTo(3);
        assertThat(bears.hasKeyword(Keyword.DOUBLE_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("Dangerous makes the target require all able blockers")
    void dangerousMakesTargetRequireAllBlockers() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new ArmedDangerous()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castSorcery(player1, 0, DANGEROUS, bears.getId());
        harness.passBothPriorities();

        assertThat(bears.isMustBeBlockedByAllThisTurn()).isTrue();
    }

    @Test
    @DisplayName("Fuse resolves Armed and Dangerous on independent targets")
    void fuseUsesIndependentTargets() {
        Permanent armedTarget = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent dangerousTarget = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

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
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

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
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.setHand(player1, List.of(new ArmedDangerous()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castModalSorcery(player1, 0, FUSE,
                List.of(bears.getId(), bears.getId())))
                .isInstanceOf(IllegalStateException.class);
    }
}

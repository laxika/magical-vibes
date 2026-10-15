package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SparkmagesGambit.class, GrizzlyBears.class, Forest.class})
class SparkmagesGambitTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 1 damage to and stops up to two target creatures from blocking")
    void damagesAndStopsTwoCreaturesFromBlocking() {
        Permanent firstTarget = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent secondTarget = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new SparkmagesGambit()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveSorcery(player1, 0, List.of(firstTarget.getId(), secondTarget.getId()));

        assertThat(firstTarget.getMarkedDamage()).isEqualTo(1);
        assertThat(secondTarget.getMarkedDamage()).isEqualTo(1);
        assertThat(firstTarget.isCantBlockThisTurn()).isTrue();
        assertThat(secondTarget.isCantBlockThisTurn()).isTrue();
    }

    @Test
    @DisplayName("Can target only one creature")
    void canTargetOnlyOneCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent untargeted = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new SparkmagesGambit()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveSorcery(player1, 0, List.of(target.getId()));

        assertThat(target.getMarkedDamage()).isEqualTo(1);
        assertThat(target.isCantBlockThisTurn()).isTrue();
        assertThat(untargeted.getMarkedDamage()).isZero();
        assertThat(untargeted.isCantBlockThisTurn()).isFalse();
    }

    @Test
    void canResolveWithNoTargets() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new SparkmagesGambit()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castAndResolveSorcery(player1, 0, List.of());

        assertThat(creature.getMarkedDamage()).isZero();
        assertThat(creature.isCantBlockThisTurn()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotTargetThreeCreatures() {
        Permanent first = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent third = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new SparkmagesGambit()));
        harness.addMana(player1, ManaColor.RED, 2);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0,
                List.of(first.getId(), second.getId(), third.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void canTargetCreaturesControlledByDifferentPlayers() {
        Permanent own = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opposing = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new SparkmagesGambit()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castAndResolveSorcery(player1, 0, List.of(own.getId(), opposing.getId()));

        assertThat(own.getMarkedDamage()).isEqualTo(1);
        assertThat(opposing.getMarkedDamage()).isEqualTo(1);
        assertThat(own.isCantBlockThisTurn()).isTrue();
        assertThat(opposing.isCantBlockThisTurn()).isTrue();
    }

    @Test
    void cannotTargetTheSameCreatureTwice() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new SparkmagesGambit()));
        harness.addMana(player1, ManaColor.RED, 2);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0,
                List.of(target.getId(), target.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreaturePermanent() {
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setHand(player1, List.of(new SparkmagesGambit()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, List.of(forest.getId())))
                .isInstanceOf(IllegalStateException.class);
    }
}

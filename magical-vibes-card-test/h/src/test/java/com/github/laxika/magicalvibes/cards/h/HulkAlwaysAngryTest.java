package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HulkAlwaysAngry.class, GrizzlyBears.class, Ornithopter.class})
class HulkAlwaysAngryTest extends BaseCardTest {

    @Test
    @DisplayName("Hulk destroys all artifacts when it enters")
    void etbDestroysAllArtifacts() {
        harness.addToBattlefield(player1, new Ornithopter());
        harness.addToBattlefield(player2, new Ornithopter());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.castFromHand(player1, new HulkAlwaysAngry(), "{5}{R}{R}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Ornithopter");
        harness.assertInGraveyard(player2, "Ornithopter");
        harness.assertOnBattlefield(player1, "Hulk, Always Angry");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Hulk must attack each combat when able")
    void mustAttackWhenAble() {
        addCreatureReady(player1, new HulkAlwaysAngry());

        assertThatThrownBy(() -> declareAttackers(List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must attack");
    }

    @Test
    void doesNotHaveToAttackWithSummoningSickness() {
        Permanent hulk = harness.addToBattlefieldAndReturn(player1, new HulkAlwaysAngry());

        declareAttackers(List.of());

        assertThat(hulk.isAttacking()).isFalse();
    }

    @Test
    void doesNotHaveToAttackWhenTapped() {
        Permanent hulk = addCreatureReady(player1, new HulkAlwaysAngry());
        hulk.tap();

        declareAttackers(List.of());

        assertThat(hulk.isAttacking()).isFalse();
    }

    @Test
    void entersWithNoArtifacts() {
        harness.castFromHand(player1, new HulkAlwaysAngry(), "{5}{R}{R}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Hulk, Always Angry");
        assertThat(gd.stack).isEmpty();
    }
}

package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Explosive Getaway")
class ExplosiveGetawayTest extends BaseCardTest {

    @Test
    @DisplayName("exiles a targeted creature, damages the other creatures, and returns the target at the next end step")
    void exilesTargetAndReturnsAtNextEndStep() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new HillGiant());
        castGetaway(target.getId());

        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Grizzly Bears")).isZero();
        assertThat(countPermanents(player1, "Hill Giant")).isZero();

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Grizzly Bears")).isEqualTo(1);
    }

    @Test
    @DisplayName("can resolve without choosing the optional target")
    void canResolveWithoutTarget() {
        harness.addToBattlefield(player1, new HillGiant());
        harness.setHand(player1, List.of(new ExplosiveGetaway()));
        addGetawayMana();

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Hill Giant")).isZero();
    }

    @Test
    @DisplayName("rejects a land as the optional target")
    void rejectsLandTarget() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.setHand(player1, List.of(new ExplosiveGetaway()));
        addGetawayMana();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 0, land.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void castGetaway(java.util.UUID targetId) {
        harness.setHand(player1, List.of(new ExplosiveGetaway()));
        addGetawayMana();
        harness.castSorcery(player1, 0, 0, targetId);
    }

    private void addGetawayMana() {
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
    }
}

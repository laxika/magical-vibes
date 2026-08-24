package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ArrestersZealTest extends BaseCardTest {

    @Test
    @DisplayName("During your main phase, target creature gets +2/+2 and flying")
    void grantsFlyingDuringMainPhase() {
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new ArrestersZeal()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        UUID targetId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();

        Permanent bears = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(bears.getPowerModifier()).isEqualTo(2);
        assertThat(bears.getToughnessModifier()).isEqualTo(2);
        assertThat(bears.hasKeyword(Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Outside your main phase, target creature gets +2/+2 without flying")
    void doesNotGrantFlyingOutsideMainPhase() {
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new ArrestersZeal()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        UUID targetId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();

        Permanent bears = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(bears.getPowerModifier()).isEqualTo(2);
        assertThat(bears.getToughnessModifier()).isEqualTo(2);
        assertThat(bears.hasKeyword(Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("The pump and flying wear off at end of turn")
    void effectsWearOffAtEndOfTurn() {
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new ArrestersZeal()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        UUID targetId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent bears = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(bears.getPowerModifier()).isZero();
        assertThat(bears.getToughnessModifier()).isZero();
        assertThat(bears.hasKeyword(Keyword.FLYING)).isFalse();
    }
}

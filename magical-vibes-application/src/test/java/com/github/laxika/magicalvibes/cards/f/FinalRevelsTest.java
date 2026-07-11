package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FinalRevelsTest extends BaseCardTest {

    @Test
    @DisplayName("Mode 0 gives all creatures +2/+0 until end of turn")
    void plusTwoPowerMode() {
        Permanent mine = new Permanent(new GrizzlyBears()); // 2/2
        Permanent theirs = new Permanent(new GrizzlyBears()); // 2/2
        gd.playerBattlefields.get(player1.getId()).add(mine);
        gd.playerBattlefields.get(player2.getId()).add(theirs);

        harness.setHand(player1, List.of(new FinalRevels()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(mine.getPowerModifier()).isEqualTo(2);
        assertThat(mine.getToughnessModifier()).isEqualTo(0);
        assertThat(theirs.getPowerModifier()).isEqualTo(2);
        assertThat(theirs.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("The +2/+0 boost wears off at end of turn")
    void plusTwoWearsOff() {
        Permanent mine = new Permanent(new GrizzlyBears()); // 2/2
        gd.playerBattlefields.get(player1.getId()).add(mine);

        harness.setHand(player1, List.of(new FinalRevels()));
        harness.addMana(player1, ManaColor.BLACK, 5);
        harness.forceActivePlayer(player1);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
        assertThat(mine.getPowerModifier()).isEqualTo(2);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(mine.getPowerModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Mode 1 gives all creatures -0/-2, killing the 2/2 and sparing the 3/3")
    void minusTwoToughnessMode() {
        Permanent bears = new Permanent(new GrizzlyBears()); // 2/2
        Permanent giant = new Permanent(new HillGiant()); // 3/3
        gd.playerBattlefields.get(player2.getId()).add(bears);
        gd.playerBattlefields.get(player2.getId()).add(giant);

        harness.setHand(player1, List.of(new FinalRevels()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castSorcery(player1, 0, 1);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Hill Giant"));
        assertThat(giant.getPowerModifier()).isEqualTo(0);
        assertThat(giant.getToughnessModifier()).isEqualTo(-2);
    }

    @Test
    @DisplayName("Choosing an invalid mode is rejected at cast time")
    void invalidModeIsRejected() {
        harness.setHand(player1, List.of(new FinalRevels()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 99))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid mode index");
    }
}

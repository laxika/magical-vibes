package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.b.Bandage;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Murder;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DrossHarvesterTest extends BaseCardTest {

    @Test
    @DisplayName("Gains 2 life when another creature dies")
    void gainsLifeWhenAnotherCreatureDies() {
        harness.addToBattlefield(player1, new DrossHarvester());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setLife(player1, 10);

        harness.setHand(player1, List.of(new DoomBlade()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.castSorcery(player1, 0, bears.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(12);
    }

    @Test
    @DisplayName("Gains 2 life when Dross Harvester dies")
    void gainsLifeWhenItDies() {
        Permanent harvester = harness.addToBattlefieldAndReturn(player1, new DrossHarvester());
        harness.setLife(player1, 10);

        harness.setHand(player2, List.of(new Murder()));
        harness.addMana(player2, ManaColor.BLACK, 3);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castInstant(player2, 0, harvester.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(12);
    }

    @Test
    @DisplayName("Loses 4 life at the beginning of its controller's end step")
    void losesLifeAtEndStep() {
        harness.addToBattlefield(player1, new DrossHarvester());
        harness.setLife(player1, 20);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(16);
    }

    @Test
    @DisplayName("Cannot be targeted by a white spell")
    void cannotBeTargetedByWhiteSpell() {
        Permanent harvester = harness.addToBattlefieldAndReturn(player1, new DrossHarvester());
        harness.setHand(player1, List.of(new Bandage()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, harvester.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection from white");
    }
}

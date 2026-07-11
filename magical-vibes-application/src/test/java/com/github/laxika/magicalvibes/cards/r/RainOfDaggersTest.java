package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.ManorGargoyle;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RainOfDaggersTest extends BaseCardTest {

    private static final int STARTING_LIFE = 20;

    @Test
    @DisplayName("Destroys all creatures the target opponent controls and controller loses 2 life each")
    void destroysOpponentCreaturesAndLosesLife() {
        Permanent theirs1 = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent theirs2 = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent mine = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.setHand(player1, List.of(new RainOfDaggers()));
        harness.addMana(player1, ManaColor.BLACK, 6);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getId().equals(theirs1.getId()) || p.getId().equals(theirs2.getId()));
        // Controller's own creature is untouched
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getId().equals(mine.getId()));
        // 2 life lost per creature destroyed
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(STARTING_LIFE - 4);
    }

    @Test
    @DisplayName("Loses no life when the target opponent controls no creatures")
    void losesNoLifeWithNoCreatures() {
        harness.addToBattlefield(player1, new GrizzlyBears());

        harness.setHand(player1, List.of(new RainOfDaggers()));
        harness.addMana(player1, ManaColor.BLACK, 6);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(STARTING_LIFE);
    }

    @Test
    @DisplayName("Indestructible creatures are not destroyed and do not count toward life loss")
    void indestructibleNotDestroyedNotCounted() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        // Manor Gargoyle has defender, so its static ability makes it indestructible.
        Permanent gargoyle = harness.addToBattlefieldAndReturn(player2, new ManorGargoyle());

        harness.setHand(player1, List.of(new RainOfDaggers()));
        harness.addMana(player1, ManaColor.BLACK, 6);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getId().equals(bears.getId()));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getId().equals(gargoyle.getId()));
        // Only the destroyed Grizzly Bears counts: 2 life lost, not 4
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(STARTING_LIFE - 2);
    }
}

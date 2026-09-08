package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AncientOoze.class, GrizzlyBears.class, LlanowarElves.class})
class AncientOozeTest extends BaseCardTest {

    @Test
    @DisplayName("Counts the total mana value of other creatures you control")
    void countsOtherCreaturesYouControl() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new LlanowarElves());
        harness.addToBattlefield(player2, new GrizzlyBears());
        Permanent ooze = harness.addToBattlefieldAndReturn(player1, new AncientOoze());

        assertThat(gqs.getEffectivePower(gd, ooze)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, ooze)).isEqualTo(3);
    }

    @Test
    @DisplayName("Updates as your other creatures enter and leave")
    void updatesDynamically() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent ooze = harness.addToBattlefieldAndReturn(player1, new AncientOoze());

        assertStats(ooze, 2, 2);

        Permanent elves = harness.addToBattlefieldAndReturn(player1, new LlanowarElves());
        assertStats(ooze, 3, 3);

        gd.playerBattlefields.get(player1.getId()).remove(elves);
        assertStats(ooze, 2, 2);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(bears, ooze);
    }

    private void assertStats(Permanent ooze, int power, int toughness) {
        assertThat(gqs.getEffectivePower(gd, ooze)).isEqualTo(power);
        assertThat(gqs.getEffectiveToughness(gd, ooze)).isEqualTo(toughness);
    }
}

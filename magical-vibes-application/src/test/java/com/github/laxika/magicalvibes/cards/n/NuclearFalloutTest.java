package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({NuclearFallout.class, GrizzlyBears.class})
class NuclearFalloutTest extends BaseCardTest {

    @Test
    @DisplayName("Gives each player X rad counters and each creature -2X/-2X")
    void givesRadCountersAndWeakensAllCreatures() {
        Permanent ownBear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opposingBear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new NuclearFallout()));
        harness.addMana(player1, ManaColor.BLACK, 4); // X=2: {2}{B}{B}

        harness.castSorcery(player1, 0, 2);
        harness.passBothPriorities();

        assertThat(ownBear.getPowerModifier()).isEqualTo(-4);
        assertThat(ownBear.getToughnessModifier()).isEqualTo(-4);
        assertThat(opposingBear.getPowerModifier()).isEqualTo(-4);
        assertThat(opposingBear.getToughnessModifier()).isEqualTo(-4);
        assertThat(gd.playerRadCounters.get(player1.getId())).isEqualTo(2);
        assertThat(gd.playerRadCounters.get(player2.getId())).isEqualTo(2);
    }
}

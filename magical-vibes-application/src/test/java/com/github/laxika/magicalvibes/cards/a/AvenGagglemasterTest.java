package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.cards.s.SnappingDrake;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class AvenGagglemasterTest extends BaseCardTest {

    @Test
    @DisplayName("Gains 2 life for each flying creature you control")
    void gainsTwoLifePerControlledFlyingCreature() {
        harness.addToBattlefield(player1, new SerraAngel());
        harness.addToBattlefield(player1, new SnappingDrake());
        harness.setHand(player1, List.of(new AvenGagglemaster()));
        harness.addMana(player1, ManaColor.WHITE, 5);
        harness.setLife(player1, 20);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(26);
    }

    @Test
    @DisplayName("Counts itself but not non-flying or opposing creatures")
    void countsOnlyControlledFlyingCreaturesIncludingItself() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new SerraAngel());
        harness.setHand(player1, List.of(new AvenGagglemaster()));
        harness.addMana(player1, ManaColor.WHITE, 5);
        harness.setLife(player1, 20);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(22);
    }
}

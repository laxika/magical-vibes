package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.ManorGargoyle;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({VillainousWrath.class, GrizzlyBears.class, ManorGargoyle.class})
class VillainousWrathTest extends BaseCardTest {

    @Test
    @DisplayName("Target opponent loses life for their creatures, then all creatures are destroyed")
    void losesLifeThenDestroysAllCreatures() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new VillainousWrath()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 18);
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Counts indestructible creatures for life loss but leaves them on the battlefield")
    void countsIndestructibleCreaturesBeforeDestroying() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new ManorGargoyle());
        harness.setHand(player1, List.of(new VillainousWrath()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 18);
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Manor Gargoyle");
    }

    @Test
    @DisplayName("Cannot target yourself")
    void cannotTargetYourself() {
        harness.setHand(player1, List.of(new VillainousWrath()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}

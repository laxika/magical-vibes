package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.c.Cancel;
import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class VolcanicFalloutTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 2 damage to each creature and each player")
    void dealsTwoToEachCreatureAndPlayer() {
        harness.addToBattlefield(player1, new GrizzlyBears()); // 2/2
        harness.addToBattlefield(player2, new GrizzlyBears()); // 2/2
        harness.setHand(player1, List.of(new VolcanicFallout()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        // Both 2/2 creatures die to 2 damage.
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        // Both players take 2 damage.
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Creatures with toughness greater than 2 survive")
    void toughCreaturesSurvive() {
        harness.addToBattlefield(player2, new GiantSpider()); // 2/4
        harness.setHand(player1, List.of(new VolcanicFallout()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Giant Spider");
    }

    @Test
    @DisplayName("Can't be countered — Cancel resolves but damage is still dealt")
    void cannotBeCountered() {
        VolcanicFallout fallout = new VolcanicFallout();
        harness.setHand(player1, List.of(fallout));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.setHand(player2, List.of(new Cancel()));
        harness.addMana(player2, ManaColor.BLUE, 3);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.castInstant(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, fallout.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        // Cancel resolved but couldn't counter — Volcanic Fallout still dealt 2 to each player.
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Cancel"));
    }
}

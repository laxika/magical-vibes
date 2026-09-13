package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BlightedGorge.class, GrizzlyBears.class})
class BlightedGorgeTest extends BaseCardTest {

    @Test
    @DisplayName("Blighted Gorge taps for colorless mana")
    void tapsForColorlessMana() {
        harness.addToBattlefield(player1, new BlightedGorge());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
    }

    @Test
    @DisplayName("Pays five mana, deals 2 damage to a creature, and sacrifices itself")
    void dealsDamageToCreatureAndSacrificesItself() {
        harness.addToBattlefield(player1, new BlightedGorge());
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, 1, null, targetId);
        harness.assertInGraveyard(player1, "Blighted Gorge");
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Deals 2 damage to a target player")
    void dealsDamageToPlayer() {
        harness.setLife(player2, 20);
        harness.addToBattlefield(player1, new BlightedGorge());
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, 1, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }
}

package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SearingSpearTest extends BaseCardTest {

    @Test
    @DisplayName("Searing Spear deals 3 damage to target player")
    void deals3DamageToPlayer() {
        harness.setHand(player1, List.of(new SearingSpear()));
        harness.addMana(player1, ManaColor.RED, 5);
        harness.setLife(player2, 20);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("Searing Spear kills a creature with toughness 3 or less")
    void killsSmallCreature() {
        Permanent bear = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(player2.getId()).add(bear);

        harness.setHand(player1, List.of(new SearingSpear()));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.castInstant(player1, 0, bear.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Searing Spear does not kill a creature with toughness greater than 3")
    void doesNotKillLargeCreature() {
        Permanent elemental = new Permanent(new AirElemental());
        gd.playerBattlefields.get(player2.getId()).add(elemental);

        harness.setHand(player1, List.of(new SearingSpear()));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.castInstant(player1, 0, elemental.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Air Elemental");
    }

    @Test
    @DisplayName("Searing Spear fizzles when its target leaves the battlefield")
    void fizzlesWhenTargetRemoved() {
        Permanent bear = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(player2.getId()).add(bear);

        harness.setHand(player1, List.of(new SearingSpear()));
        harness.addMana(player1, ManaColor.RED, 5);
        harness.setLife(player2, 20);

        harness.castInstant(player1, 0, bear.getId());
        gd.playerBattlefields.get(player2.getId()).clear();
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }
}

package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.b.BayFalcon;
import com.github.laxika.magicalvibes.cards.d.DwarvenNomad;
import com.github.laxika.magicalvibes.cards.z.ZhalfirinKnight;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SubterraneanSpirit.class, BayFalcon.class, DwarvenNomad.class, ZhalfirinKnight.class})
class SubterraneanSpiritTest extends BaseCardTest {

    private Permanent addReadySpirit() {
        return addCreatureReady(player1, new SubterraneanSpirit());
    }

    @Test
    @DisplayName("Deals 1 damage to each creature without flying, sparing flyers")
    void damagesOnlyNonFlyers() {
        addReadySpirit();
        harness.addToBattlefield(player1, new DwarvenNomad());    // 1/1 non-flying -> dies
        harness.addToBattlefield(player2, new DwarvenNomad());    // 1/1 non-flying -> dies
        harness.addToBattlefield(player2, new BayFalcon());       // 1/1 flying -> survives
        harness.addToBattlefield(player2, new ZhalfirinKnight()); // 2/2 non-flying -> survives

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Dwarven Nomad");
        harness.assertNotOnBattlefield(player2, "Dwarven Nomad");
        harness.assertOnBattlefield(player2, "Bay Falcon");
        harness.assertOnBattlefield(player2, "Zhalfirin Knight");
    }

    @Test
    @DisplayName("Protection from red prevents the damage from its own red ability")
    void protectionFromRedPreventsSelfDamage() {
        Permanent spirit = addReadySpirit();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Subterranean Spirit");
        assertThat(spirit.isTapped()).isTrue();
        assertThat(spirit.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Does not damage players")
    void doesNotDamagePlayers() {
        addReadySpirit();
        harness.setLife(player2, 20);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }
}

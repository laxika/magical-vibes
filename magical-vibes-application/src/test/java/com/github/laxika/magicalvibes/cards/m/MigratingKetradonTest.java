package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MigratingKetradonTest extends BaseCardTest {

    @Test
    void entersAndGainsFourLife() {
        harness.setHand(player1, List.of(new MigratingKetradon()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(24);
        harness.assertOnBattlefield(player1, "Migrating Ketradon");
    }

    @Test
    void cyclingDiscardsThisCardAndDraws() {
        harness.setHand(player1, List.of(new MigratingKetradon()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Migrating Ketradon");
        harness.assertInHand(player1, "Grizzly Bears");
    }
}

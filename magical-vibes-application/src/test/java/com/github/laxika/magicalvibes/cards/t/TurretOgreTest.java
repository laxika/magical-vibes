package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TurretOgreTest extends BaseCardTest {

    @Test
    @DisplayName("ETB deals 2 damage to each opponent with another creature of power 4 or greater")
    void etbDamagesEachOpponentWithAnotherLargeCreature() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.addToBattlefield(player1, new AirElemental());

        castTurretOgre();
        resolveAllTriggers();

        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("ETB does not trigger without another qualifying creature you control")
    void etbDoesNotTriggerWithoutAnotherQualifyingCreature() {
        harness.setLife(player2, 20);
        harness.addToBattlefield(player2, new AirElemental());

        castTurretOgre();
        resolveAllTriggers();

        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
    }

    private void castTurretOgre() {
        harness.setHand(player1, List.of(new TurretOgre()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
    }
}

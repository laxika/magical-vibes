package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.c.CloudSprite;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class WarmongerTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 1 damage to each nonflying creature and each player")
    void damagesNonflyingCreaturesAndPlayers() {
        Permanent warmonger = harness.addToBattlefieldAndReturn(player1, new Warmonger());
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent flyingCreature = harness.addToBattlefieldAndReturn(player2, new CloudSprite());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(warmonger.getMarkedDamage()).isEqualTo(1);
        assertThat(ownCreature.getMarkedDamage()).isEqualTo(1);
        assertThat(opposingCreature.getMarkedDamage()).isEqualTo(1);
        assertThat(flyingCreature.getMarkedDamage()).isZero();
        harness.assertLife(player1, 19);
        harness.assertLife(player2, 19);
    }

    @Test
    @DisplayName("Any player may activate Warmonger")
    void opponentMayActivate() {
        Permanent warmonger = harness.addToBattlefieldAndReturn(player1, new Warmonger());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        harness.activateAbility(player2, 0, null, null);
        harness.passBothPriorities();

        assertThat(warmonger.getMarkedDamage()).isEqualTo(1);
        harness.assertLife(player1, 19);
        harness.assertLife(player2, 19);
    }
}

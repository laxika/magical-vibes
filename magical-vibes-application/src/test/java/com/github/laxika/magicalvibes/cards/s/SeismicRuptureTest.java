package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SeismicRuptureTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 2 damage to each creature without flying")
    void damagesCreaturesWithoutFlying() {
        Permanent ownGroundCreature = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        Permanent opposingGroundCreature = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        Permanent opposingFlyingCreature = harness.addToBattlefieldAndReturn(player2, new SuntailHawk());
        harness.setHand(player1, List.of(new SeismicRupture()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(ownGroundCreature.getMarkedDamage()).isEqualTo(2);
        assertThat(opposingGroundCreature.getMarkedDamage()).isEqualTo(2);
        assertThat(opposingFlyingCreature.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Does not deal damage to players")
    void doesNotDamagePlayers() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new SeismicRupture()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }
}

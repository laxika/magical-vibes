package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ImprovisedWeaponry.class, GrizzlyBears.class})
class ImprovisedWeaponryTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 2 damage to a player and creates a Treasure token")
    void damagesPlayerAndCreatesTreasure() {
        int lifeBefore = gd.getLife(player2.getId());
        castImprovisedWeaponry(player2.getId());

        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBefore - 2);
        assertThat(findPermanents(player1, "Treasure")).hasSize(1);
    }

    @Test
    @DisplayName("Deals 2 damage to a creature and creates a Treasure token")
    void damagesCreatureAndCreatesTreasure() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castImprovisedWeaponry(target.getId());

        assertThat(target.getMarkedDamage()).isEqualTo(2);
        assertThat(findPermanents(player1, "Treasure")).hasSize(1);
    }

    private void castImprovisedWeaponry(UUID targetId) {
        harness.setHand(player1, List.of(new ImprovisedWeaponry()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castSorcery(player1, 0, targetId);
        harness.passBothPriorities();
    }
}

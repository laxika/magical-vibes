package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class HallowTest extends BaseCardTest {

    @Test
    @DisplayName("Prevents damage from the targeted spell and gains that much life")
    void preventsTargetedSpellDamageAndGainsLife() {
        Shock shock = new Shock();
        harness.setHand(player1, List.of(new Hallow()));
        harness.setHand(player2, List.of(shock));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, player1.getId());
        harness.castInstant(player1, 0, shock.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(22);
    }

    @Test
    @DisplayName("Prevents damage from the targeted spell to a creature")
    void preventsTargetedSpellDamageToCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Shock shock = new Shock();
        harness.setHand(player1, List.of(new Hallow()));
        harness.setHand(player2, List.of(shock));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, creature.getId());
        harness.castInstant(player1, 0, shock.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(creature.getMarkedDamage()).isZero();
        assertThat(gd.getLife(player1.getId())).isEqualTo(22);
    }
}

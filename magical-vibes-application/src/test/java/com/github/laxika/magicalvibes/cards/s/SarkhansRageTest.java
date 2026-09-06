package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.ColossalDreadmaw;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SarkhansRage.class, ColossalDreadmaw.class, ShivanDragon.class})
class SarkhansRageTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 5 damage to the target player and 2 damage to you without a Dragon")
    void dealsDamageAndHurtsControllerWithoutDragon() {
        castAt(player2.getId());

        assertThat(gd.getLife(player2.getId())).isEqualTo(15);
        assertThat(gd.getLife(player1.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Does not hurt you when you control a Dragon")
    void doesNotHurtControllerWithDragon() {
        harness.addToBattlefield(player1, new ShivanDragon());
        Permanent target = addCreatureReady(player2, new ColossalDreadmaw());

        castAt(target.getId());

        assertThat(target.getMarkedDamage()).isEqualTo(5);
        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
    }

    private void castAt(java.util.UUID targetId) {
        harness.setHand(player1, List.of(new SarkhansRage()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();
    }
}

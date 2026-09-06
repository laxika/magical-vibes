package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BattlefieldMedic.class, GrizzlyBears.class, Shock.class})
class BattlefieldMedicTest extends BaseCardTest {

    @Test
    @DisplayName("Prevents damage equal to the number of Clerics on the battlefield")
    void preventsDamageEqualToClericCount() {
        addCreatureReady(player1, new BattlefieldMedic());
        addCreatureReady(player2, new BattlefieldMedic());
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, bears.getId());
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, bears.getId());
        harness.passBothPriorities();

        assertThat(bears.getMarkedDamage()).isZero();
    }
}

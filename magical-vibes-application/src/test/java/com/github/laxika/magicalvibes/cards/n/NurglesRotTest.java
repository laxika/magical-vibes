package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GrizzlyBears.class, NurglesRot.class})
class NurglesRotTest extends BaseCardTest {

    @Test
    @DisplayName("When the enchanted creature dies, Nurgle's Rot returns to hand and creates a Plaguebearer of Nurgle")
    void returnsToHandAndCreatesPlaguebearerWhenEnchantedCreatureDies() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new NurglesRot()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castEnchantment(player1, 0, bears.getId());
        harness.passBothPriorities();

        bears.setMarkedDamage(2);
        harness.runStateBasedActions();
        harness.passBothPriorities();

        harness.assertInHand(player1, "Nurgle's Rot");
        harness.assertNotOnBattlefield(player1, "Nurgle's Rot");

        Permanent token = findPermanent(player1, "Plaguebearer of Nurgle");
        assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(3);
    }

    @Test
    @DisplayName("Nurgle's Rot can enchant only a creature an opponent controls")
    void cannotEnchantOwnCreature() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new NurglesRot()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}

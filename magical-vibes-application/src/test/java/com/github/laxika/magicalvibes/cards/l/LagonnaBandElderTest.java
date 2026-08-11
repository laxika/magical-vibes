package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.r.RuleOfLaw;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class LagonnaBandElderTest extends BaseCardTest {

    @Test
    @DisplayName("ETB trigger gains 3 life when its controller controls an enchantment")
    void gainsLifeWithControlledEnchantment() {
        harness.setLife(player1, 10);
        harness.addToBattlefield(player1, new RuleOfLaw());
        castLagonnaBandElder();

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(13);
    }

    @Test
    @DisplayName("ETB trigger does not gain life without a controlled enchantment")
    void doesNotGainLifeWithoutControlledEnchantment() {
        harness.setLife(player1, 10);
        castLagonnaBandElder();

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(10);
    }

    @Test
    @DisplayName("An opponent's enchantment does not satisfy the condition")
    void opponentEnchantmentDoesNotSatisfyCondition() {
        harness.setLife(player1, 10);
        harness.addToBattlefield(player2, new RuleOfLaw());
        castLagonnaBandElder();

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(10);
    }

    private void castLagonnaBandElder() {
        harness.setHand(player1, List.of(new LagonnaBandElder()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0);
    }
}

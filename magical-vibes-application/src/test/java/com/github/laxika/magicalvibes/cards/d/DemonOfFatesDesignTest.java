package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.a.Aluren;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DemonOfFatesDesign.class, Aluren.class, Shock.class})
class DemonOfFatesDesignTest extends BaseCardTest {

    @Test
    @DisplayName("Once each turn, the controller may cast an enchantment by paying its mana value in life")
    void castsEnchantmentByPayingManaValueInLife() {
        harness.setLife(player1, 20);
        harness.addToBattlefield(player1, new DemonOfFatesDesign());
        harness.setHand(player1, List.of(new Aluren()));

        harness.castEnchantment(player1, 0);

        assertThat(gd.getLife(player1.getId())).isEqualTo(16);
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("The life alternative is limited to enchantment spells and once each turn")
    void lifeAlternativeIsRestricted() {
        harness.setLife(player1, 20);
        harness.addToBattlefield(player1, new DemonOfFatesDesign());
        harness.setHand(player1, List.of(new Aluren()));
        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new Aluren()));
        assertThatThrownBy(() -> harness.castEnchantment(player1, 0))
                .isInstanceOf(IllegalStateException.class);

        harness.setHand(player1, List.of(new Shock()));
        assertThatThrownBy(() -> harness.castInstant(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Sacrificing another enchantment boosts power by its mana value until end of turn")
    void sacrificesEnchantmentForManaValueBoost() {
        Permanent demon = harness.addToBattlefieldAndReturn(player1, new DemonOfFatesDesign());
        Permanent enchantment = harness.addToBattlefieldAndReturn(player1, new Aluren());
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, enchantment.getId());
        harness.passBothPriorities();

        assertThat(demon.getPowerModifier()).isEqualTo(4);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(enchantment);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(demon.getPowerModifier()).isZero();
    }
}

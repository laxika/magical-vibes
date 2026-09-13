package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.e.EnchantedEvening;
import com.github.laxika.magicalvibes.cards.s.Sanctimony;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TetheredGriffin.class, Sanctimony.class, EnchantedEvening.class})
class TetheredGriffinTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrifices itself when its controller controls no enchantments")
    void sacrificesWhenNoEnchantments() {
        castGriffin();

        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Tethered Griffin");
        harness.assertInGraveyard(player1, "Tethered Griffin");
    }

    @Test
    @DisplayName("Survives while its controller controls an enchantment")
    void survivesWithEnchantment() {
        harness.addToBattlefield(player1, new Sanctimony());
        castGriffin();

        resolveAllTriggers();

        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Tethered Griffin");
    }

    @Test
    @DisplayName("An opponent's enchantment does not satisfy the condition")
    void opponentEnchantmentDoesNotCount() {
        harness.addToBattlefield(player2, new Sanctimony());
        castGriffin();

        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Tethered Griffin");
        harness.assertInGraveyard(player1, "Tethered Griffin");
    }

    @Test
    @DisplayName("Counts an effective enchantment type granted by a static effect")
    void survivesWhenStaticEffectMakesItAnEnchantment() {
        harness.addToBattlefield(player2, new EnchantedEvening());
        Permanent griffin = harness.addToBattlefieldAndReturn(player1, new TetheredGriffin());

        assertThat(gqs.isEnchantment(gd, griffin)).isTrue();

        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Tethered Griffin");
    }

    @Test
    @DisplayName("Gaining an enchantment after the ability triggers does not stop it")
    void conditionIsCheckedWhenAbilityTriggers() {
        castGriffin();

        harness.passBothPriorities();
        assertThat(gd.stack).hasSize(1);
        harness.addToBattlefield(player1, new Sanctimony());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Tethered Griffin");
        harness.assertInGraveyard(player1, "Tethered Griffin");
    }

    private void castGriffin() {
        harness.setHand(player1, List.of(new TetheredGriffin()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castCreature(player1, 0);
    }
}

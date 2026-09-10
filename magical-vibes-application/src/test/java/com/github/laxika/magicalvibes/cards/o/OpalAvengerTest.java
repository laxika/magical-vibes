package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.e.EnchantedEvening;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(OpalAvenger.class)
class OpalAvengerTest extends BaseCardTest {

    @Test
    @DisplayName("Becomes a 3/5 Soldier creature when its controller has 10 or less life")
    void becomesCreatureWhenControllerHasTenOrLessLife() {
        Permanent avenger = harness.addToBattlefieldAndReturn(player1, new OpalAvenger());

        harness.setLife(player1, 10);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, avenger)).isTrue();
        assertThat(gqs.isEnchantment(gd, avenger)).isFalse();
        assertThat(gqs.hasEffectiveSubtype(gd, avenger, CardSubtype.SOLDIER)).isTrue();
        assertThat(gqs.getEffectivePower(gd, avenger)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, avenger)).isEqualTo(5);
    }

    @Test
    @DisplayName("Does not transform above the life threshold and does not revert after transforming")
    void thresholdAndPermanentTransformation() {
        Permanent avenger = harness.addToBattlefieldAndReturn(player1, new OpalAvenger());

        harness.setLife(player1, 11);
        harness.passBothPriorities();
        assertThat(gqs.isCreature(gd, avenger)).isFalse();
        assertThat(gqs.isEnchantment(gd, avenger)).isTrue();

        harness.setLife(player1, 10);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.setLife(player1, 11);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, avenger)).isTrue();
        assertThat(gqs.isEnchantment(gd, avenger)).isFalse();
    }

    @Test
    @DisplayName("Checks its controller's life total, not an opponent's")
    void checksControllerLife() {
        Permanent avenger = harness.addToBattlefieldAndReturn(player1, new OpalAvenger());

        harness.setLife(player2, 10);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, avenger)).isFalse();
        assertThat(gqs.isEnchantment(gd, avenger)).isTrue();
    }

    @Test
    @CardUsed(EnchantedEvening.class)
    @DisplayName("Uses the permanent's effective type when checking whether it is an enchantment")
    void checksEffectiveEnchantmentType() {
        Permanent avenger = harness.addToBattlefieldAndReturn(player1, new OpalAvenger());

        harness.setLife(player1, 10);
        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(gqs.isCreature(gd, avenger)).isTrue();
        assertThat(gqs.isEnchantment(gd, avenger)).isFalse();

        long triggerCountBefore = gd.gameLog.stream()
                .filter(entry -> entry.plainText().contains(
                        "Opal Avenger's state-triggered ability triggers."))
                .count();

        harness.addToBattlefield(player1, new EnchantedEvening());
        assertThat(gqs.isEnchantment(gd, avenger)).isTrue();

        harness.passBothPriorities();

        long triggerCountAfter = gd.gameLog.stream()
                .filter(entry -> entry.plainText().contains(
                        "Opal Avenger's state-triggered ability triggers."))
                .count();
        assertThat(triggerCountAfter).isEqualTo(triggerCountBefore + 1);
    }
}

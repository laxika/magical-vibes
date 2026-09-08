package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.d.DeafeningSilence;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ShamblingSuit.class, DeafeningSilence.class, GrizzlyBears.class, Spellbook.class})
class ShamblingSuitTest extends BaseCardTest {

    @Test
    @DisplayName("Power equals the number of artifacts and enchantments its controller controls")
    void powerEqualsControlledArtifactsAndEnchantments() {
        Permanent suit = harness.addToBattlefieldAndReturn(player1, new ShamblingSuit());

        assertThat(gqs.getEffectivePower(gd, suit)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, suit)).isEqualTo(3);

        harness.addToBattlefield(player1, new DeafeningSilence());
        assertThat(gqs.getEffectivePower(gd, suit)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, suit)).isEqualTo(3);

        harness.addToBattlefield(player1, new Spellbook());
        assertThat(gqs.getEffectivePower(gd, suit)).isEqualTo(3);
    }

    @Test
    @DisplayName("Power ignores non-artifacts, opponent permanents, and updates when permanents leave")
    void powerCountsOnlyMatchingPermanentsControlledByItsController() {
        Permanent suit = harness.addToBattlefieldAndReturn(player1, new ShamblingSuit());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new DeafeningSilence());

        assertThat(gqs.getEffectivePower(gd, suit)).isEqualTo(1);

        Permanent enchantment = harness.addToBattlefieldAndReturn(player1, new DeafeningSilence());
        assertThat(gqs.getEffectivePower(gd, suit)).isEqualTo(2);

        gd.playerBattlefields.get(player1.getId()).remove(enchantment);
        assertThat(gqs.getEffectivePower(gd, suit)).isEqualTo(1);
    }
}

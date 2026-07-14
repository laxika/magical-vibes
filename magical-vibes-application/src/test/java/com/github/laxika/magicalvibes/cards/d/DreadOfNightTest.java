package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.cards.s.SuntailHawk;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DreadOfNightTest extends BaseCardTest {

    @Test
    @DisplayName("White creature gets -1/-1")
    void debuffsWhiteCreature() {
        harness.addToBattlefield(player1, new DreadOfNight());
        harness.addToBattlefield(player2, new SerraAngel());

        Permanent angel = findPermanent(player2, "Serra Angel");

        // 4/4 base -> 3/3
        assertThat(gqs.getEffectivePower(gd, angel)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, angel)).isEqualTo(3);
    }

    @Test
    @DisplayName("Non-white creature is not affected")
    void doesNotDebuffNonWhiteCreature() {
        harness.addToBattlefield(player1, new DreadOfNight());
        harness.addToBattlefield(player2, new GrizzlyBears());

        Permanent bears = findPermanent(player2, "Grizzly Bears");

        // 2/2 green, unaffected
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Affects controller's own white creatures too")
    void debuffsOwnWhiteCreature() {
        harness.addToBattlefield(player1, new DreadOfNight());
        harness.addToBattlefield(player1, new SerraAngel());

        Permanent angel = findPermanent(player1, "Serra Angel");

        assertThat(gqs.getEffectivePower(gd, angel)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, angel)).isEqualTo(3);
    }

    @Test
    @DisplayName("Two Dread of Night give -2/-2 to a white creature")
    void twoStack() {
        harness.addToBattlefield(player1, new DreadOfNight());
        harness.addToBattlefield(player1, new DreadOfNight());
        harness.addToBattlefield(player2, new SerraAngel());

        Permanent angel = findPermanent(player2, "Serra Angel");

        // 4/4 base -> 2/2
        assertThat(gqs.getEffectivePower(gd, angel)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, angel)).isEqualTo(2);
    }

    @Test
    @DisplayName("Reduces a 1/1 white creature to 0/0")
    void reducesSmallWhiteCreatureToZero() {
        harness.addToBattlefield(player1, new DreadOfNight());
        harness.addToBattlefield(player2, new SuntailHawk());

        Permanent hawk = findPermanent(player2, "Suntail Hawk");

        assertThat(gqs.getEffectivePower(gd, hawk)).isEqualTo(0);
        assertThat(gqs.getEffectiveToughness(gd, hawk)).isEqualTo(0);
    }

    @Test
    @DisplayName("Debuff is removed when Dread of Night leaves the battlefield")
    void debuffRemovedWhenSourceLeaves() {
        harness.addToBattlefield(player1, new DreadOfNight());
        harness.addToBattlefield(player2, new SerraAngel());

        Permanent angel = findPermanent(player2, "Serra Angel");
        assertThat(gqs.getEffectivePower(gd, angel)).isEqualTo(3);

        gd.playerBattlefields.get(player1.getId())
                .removeIf(p -> p.getCard().getName().equals("Dread of Night"));

        // Back to base 4/4
        assertThat(gqs.getEffectivePower(gd, angel)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, angel)).isEqualTo(4);
    }
}

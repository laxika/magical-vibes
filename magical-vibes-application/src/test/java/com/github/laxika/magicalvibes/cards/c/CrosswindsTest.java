package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CrosswindsTest extends BaseCardTest {

    @Test
    @DisplayName("Creatures with flying get -2/-0")
    void debuffsCreaturesWithFlying() {
        harness.addToBattlefield(player1, new Crosswinds());
        harness.addToBattlefield(player2, new SerraAngel());

        Permanent angel = findPermanent(player2, "Serra Angel");

        assertThat(gqs.getEffectivePower(gd, angel)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, angel)).isEqualTo(4);
    }

    @Test
    @DisplayName("Creatures without flying are unaffected")
    void doesNotDebuffCreaturesWithoutFlying() {
        harness.addToBattlefield(player1, new Crosswinds());
        harness.addToBattlefield(player2, new GrizzlyBears());

        Permanent bears = findPermanent(player2, "Grizzly Bears");

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("The effect applies to flying creatures controlled by either player")
    void affectsOwnFlyingCreature() {
        harness.addToBattlefield(player1, new Crosswinds());
        harness.addToBattlefield(player1, new SerraAngel());

        Permanent angel = findPermanent(player1, "Serra Angel");

        assertThat(gqs.getEffectivePower(gd, angel)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, angel)).isEqualTo(4);
    }

    @Test
    @DisplayName("Two Crosswinds effects stack")
    void effectsStack() {
        harness.addToBattlefield(player1, new Crosswinds());
        harness.addToBattlefield(player1, new Crosswinds());
        harness.addToBattlefield(player2, new SerraAngel());

        Permanent angel = findPermanent(player2, "Serra Angel");

        assertThat(gqs.getEffectivePower(gd, angel)).isEqualTo(0);
        assertThat(gqs.getEffectiveToughness(gd, angel)).isEqualTo(4);
    }
}

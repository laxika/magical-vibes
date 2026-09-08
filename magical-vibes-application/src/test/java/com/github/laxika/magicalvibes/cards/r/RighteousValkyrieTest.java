package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.c.CodespellCleric;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RighteousValkyrieTest extends BaseCardTest {

    @Test
    @DisplayName("Gains life equal to the toughness of an entering Angel")
    void gainsLifeForEnteringAngel() {
        harness.addToBattlefield(player1, new RighteousValkyrie());
        harness.setHand(player1, List.of(new SerraAngel()));
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(24);
    }

    @Test
    @DisplayName("Gains life for an entering Cleric but not a non-Cleric creature")
    void gainsLifeForEnteringCleric() {
        harness.addToBattlefield(player1, new RighteousValkyrie());
        harness.setHand(player1, List.of(new CodespellCleric()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(21);

        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(21);
    }

    @Test
    @DisplayName("Boosts all creatures you control, including Righteous Valkyrie, above the life threshold")
    void boostsAllControlledCreaturesAtThreshold() {
        gd.playerLifeTotals.put(player1.getId(), 27);
        Permanent valkyrie = harness.addToBattlefieldAndReturn(player1, new RighteousValkyrie());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, valkyrie)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, valkyrie)).isEqualTo(6);
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(4);
    }

    @Test
    @DisplayName("Does not boost creatures below the life threshold")
    void doesNotBoostBelowThreshold() {
        gd.playerLifeTotals.put(player1.getId(), 26);
        Permanent valkyrie = harness.addToBattlefieldAndReturn(player1, new RighteousValkyrie());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, valkyrie)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, valkyrie)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }
}

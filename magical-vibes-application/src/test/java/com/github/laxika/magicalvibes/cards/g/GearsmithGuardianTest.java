package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.j.JacesSanctum;
import com.github.laxika.magicalvibes.cards.c.CloudSprite;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GearsmithGuardian.class, CloudSprite.class, JacesSanctum.class})
class GearsmithGuardianTest extends BaseCardTest {

    @Test
    @DisplayName("Has base stats without a blue creature")
    void baseStatsWithoutBlueCreature() {
        Permanent guardian = harness.addToBattlefieldAndReturn(player1, new GearsmithGuardian());

        assertThat(gqs.getEffectivePower(gd, guardian)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, guardian)).isEqualTo(5);
    }

    @Test
    @DisplayName("Gets +2/+0 while its controller controls a blue creature")
    void getsBoostWithControlledBlueCreature() {
        Permanent guardian = harness.addToBattlefieldAndReturn(player1, new GearsmithGuardian());
        harness.addToBattlefield(player1, new CloudSprite());

        assertThat(gqs.getEffectivePower(gd, guardian)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, guardian)).isEqualTo(5);
    }

    @Test
    @DisplayName("An opponent's blue creature does not grant the boost")
    void opponentBlueCreatureDoesNotCount() {
        Permanent guardian = harness.addToBattlefieldAndReturn(player1, new GearsmithGuardian());
        harness.addToBattlefield(player2, new CloudSprite());

        assertThat(gqs.getEffectivePower(gd, guardian)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, guardian)).isEqualTo(5);
    }

    @Test
    @DisplayName("A blue noncreature permanent does not grant the boost")
    void blueNoncreatureDoesNotCount() {
        Permanent guardian = harness.addToBattlefieldAndReturn(player1, new GearsmithGuardian());
        harness.addToBattlefield(player1, new JacesSanctum());

        assertThat(gqs.getEffectivePower(gd, guardian)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, guardian)).isEqualTo(5);
    }

    @Test
    @DisplayName("Loses the boost when the controlled blue creature leaves")
    void losesBoostWhenBlueCreatureLeaves() {
        Permanent guardian = harness.addToBattlefieldAndReturn(player1, new GearsmithGuardian());
        Permanent blueCreature = harness.addToBattlefieldAndReturn(player1, new CloudSprite());

        assertThat(gqs.getEffectivePower(gd, guardian)).isEqualTo(5);

        gd.playerBattlefields.get(player1.getId()).remove(blueCreature);

        assertThat(gqs.getEffectivePower(gd, guardian)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, guardian)).isEqualTo(5);
    }

    @Test
    @DisplayName("Gains the boost immediately when a blue creature enters")
    void gainsBoostWhenBlueCreatureEnters() {
        Permanent guardian = harness.addToBattlefieldAndReturn(player1, new GearsmithGuardian());

        assertThat(gqs.getEffectivePower(gd, guardian)).isEqualTo(3);

        harness.enterBattlefieldAndReturn(player1, new CloudSprite());

        assertThat(gqs.getEffectivePower(gd, guardian)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, guardian)).isEqualTo(5);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Multiple blue creatures grant only one boost, which lasts until the last leaves")
    void multipleBlueCreaturesDoNotStackBoost() {
        Permanent guardian = harness.addToBattlefieldAndReturn(player1, new GearsmithGuardian());
        Permanent first = harness.addToBattlefieldAndReturn(player1, new CloudSprite());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new CloudSprite());

        assertThat(gqs.getEffectivePower(gd, guardian)).isEqualTo(5);

        gd.playerBattlefields.get(player1.getId()).remove(first);

        assertThat(gqs.getEffectivePower(gd, guardian)).isEqualTo(5);

        gd.playerBattlefields.get(player1.getId()).remove(second);

        assertThat(gqs.getEffectivePower(gd, guardian)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, guardian)).isEqualTo(5);
    }

    @Test
    @DisplayName("Another nonblue creature does not grant the boost")
    void nonblueCreatureDoesNotCount() {
        Permanent guardian = harness.addToBattlefieldAndReturn(player1, new GearsmithGuardian());
        harness.addToBattlefield(player1, new GearsmithGuardian());

        assertThat(gqs.getEffectivePower(gd, guardian)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, guardian)).isEqualTo(5);
    }
}

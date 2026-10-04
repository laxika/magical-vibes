package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GatebreakerRam.class, GruulGuildgate.class})
class GatebreakerRamTest extends BaseCardTest {

    @Test
    @DisplayName("Updates the boost and keyword threshold as Gates enter")
    void updatesAsGatesEnter() {
        Permanent ram = harness.addToBattlefieldAndReturn(player1, new GatebreakerRam());

        assertThat(gqs.getEffectivePower(gd, ram)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, ram)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, ram, Keyword.VIGILANCE)).isFalse();
        assertThat(gqs.hasKeyword(gd, ram, Keyword.TRAMPLE)).isFalse();

        for (int gates = 1; gates <= 3; gates++) {
            harness.addToBattlefield(player1, new GruulGuildgate());

            assertThat(gqs.getEffectivePower(gd, ram)).isEqualTo(2 + gates);
            assertThat(gqs.getEffectiveToughness(gd, ram)).isEqualTo(2 + gates);
            assertThat(gqs.hasKeyword(gd, ram, Keyword.VIGILANCE)).isEqualTo(gates >= 2);
            assertThat(gqs.hasKeyword(gd, ram, Keyword.TRAMPLE)).isEqualTo(gates >= 2);
        }
    }

    @Test
    @DisplayName("The keyword grant applies only to its own Ram")
    void keywordsAreNotGrantedToOtherRams() {
        Permanent ownRam = harness.addToBattlefieldAndReturn(player1, new GatebreakerRam());
        Permanent opposingRam = harness.addToBattlefieldAndReturn(player2, new GatebreakerRam());
        harness.addToBattlefield(player1, new GruulGuildgate());
        harness.addToBattlefield(player1, new GruulGuildgate());

        assertThat(gqs.hasKeyword(gd, ownRam, Keyword.VIGILANCE)).isTrue();
        assertThat(gqs.hasKeyword(gd, ownRam, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.getEffectivePower(gd, opposingRam)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opposingRam)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, opposingRam, Keyword.VIGILANCE)).isFalse();
        assertThat(gqs.hasKeyword(gd, opposingRam, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Gets +1/+1 for each Gate controlled")
    void getsBoostForEachControlledGate() {
        Permanent ram = harness.addToBattlefieldAndReturn(player1, new GatebreakerRam());
        harness.addToBattlefield(player1, new GruulGuildgate());
        harness.addToBattlefield(player1, new GruulGuildgate());

        assertThat(gqs.getEffectivePower(gd, ram)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, ram)).isEqualTo(4);
    }

    @Test
    @DisplayName("Has vigilance and trample with two controlled Gates")
    void hasKeywordsWithTwoControlledGates() {
        Permanent ram = harness.addToBattlefieldAndReturn(player1, new GatebreakerRam());
        harness.addToBattlefield(player1, new GruulGuildgate());
        harness.addToBattlefield(player1, new GruulGuildgate());

        assertThat(gqs.hasKeyword(gd, ram, Keyword.VIGILANCE)).isTrue();
        assertThat(gqs.hasKeyword(gd, ram, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("Does not have vigilance or trample with fewer than two controlled Gates")
    void noKeywordsWithOneControlledGate() {
        Permanent ram = harness.addToBattlefieldAndReturn(player1, new GatebreakerRam());
        harness.addToBattlefield(player1, new GruulGuildgate());

        assertThat(gqs.hasKeyword(gd, ram, Keyword.VIGILANCE)).isFalse();
        assertThat(gqs.hasKeyword(gd, ram, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Opponent's Gates do not affect the Ram")
    void opponentGatesDoNotCount() {
        Permanent ram = harness.addToBattlefieldAndReturn(player1, new GatebreakerRam());
        harness.addToBattlefield(player2, new GruulGuildgate());
        harness.addToBattlefield(player2, new GruulGuildgate());

        assertThat(gqs.getEffectivePower(gd, ram)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, ram)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, ram, Keyword.VIGILANCE)).isFalse();
        assertThat(gqs.hasKeyword(gd, ram, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("The boost and keywords update when a Gate leaves")
    void updatesWhenGateLeaves() {
        Permanent ram = harness.addToBattlefieldAndReturn(player1, new GatebreakerRam());
        harness.addToBattlefield(player1, new GruulGuildgate());
        Permanent secondGate = harness.addToBattlefieldAndReturn(player1, new GruulGuildgate());

        assertThat(gqs.getEffectivePower(gd, ram)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, ram, Keyword.TRAMPLE)).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(secondGate);

        assertThat(gqs.getEffectivePower(gd, ram)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, ram)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, ram, Keyword.VIGILANCE)).isFalse();
        assertThat(gqs.hasKeyword(gd, ram, Keyword.TRAMPLE)).isFalse();
    }
}

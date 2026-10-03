package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.b.BottleGnomes;
import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.cards.m.Memnite;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CarapaceForger.class, BottleGnomes.class, LeoninScimitar.class, Spellbook.class, Memnite.class})
class CarapaceForgerTest extends BaseCardTest {

    // ===== Without metalcraft =====

    @Test
    @DisplayName("Base 2/2 with zero artifacts")
    void noMetalcraftWithZeroArtifacts() {
        Permanent forger = harness.addToBattlefieldAndReturn(player1, new CarapaceForger());

        assertThat(gqs.getEffectivePower(gd, forger)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, forger)).isEqualTo(2);
    }

    @Test
    @DisplayName("Base 2/2 with two artifacts")
    void noMetalcraftWithTwoArtifacts() {
        Permanent forger = harness.addToBattlefieldAndReturn(player1, new CarapaceForger());
        harness.addToBattlefield(player1, new Spellbook());
        harness.addToBattlefield(player1, new LeoninScimitar());

        assertThat(gqs.getEffectivePower(gd, forger)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, forger)).isEqualTo(2);
    }

    // ===== With metalcraft =====

    @Test
    @DisplayName("Gets +2/+2 (becomes 4/4) with exactly three artifacts")
    void metalcraftWithThreeArtifacts() {
        Permanent forger = harness.addToBattlefieldAndReturn(player1, new CarapaceForger());
        harness.addToBattlefield(player1, new Spellbook());
        harness.addToBattlefield(player1, new LeoninScimitar());
        harness.addToBattlefield(player1, new BottleGnomes());

        assertThat(gqs.getEffectivePower(gd, forger)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, forger)).isEqualTo(4);
    }

    // ===== Metalcraft lost =====

    @Test
    @DisplayName("Loses boost when artifact count drops below three")
    void losesMetalcraftWhenArtifactRemoved() {
        Permanent forger = harness.addToBattlefieldAndReturn(player1, new CarapaceForger());
        harness.addToBattlefield(player1, new Spellbook());
        harness.addToBattlefield(player1, new LeoninScimitar());
        harness.addToBattlefield(player1, new BottleGnomes());

        assertThat(gqs.getEffectivePower(gd, forger)).isEqualTo(4);

        // Remove one artifact — now only 2
        gd.playerBattlefields.get(player1.getId()).removeIf(p -> p.getCard().getName().equals("Bottle Gnomes"));
        assertThat(gqs.getEffectivePower(gd, forger)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, forger)).isEqualTo(2);
    }

    @Test
    @DisplayName("Opponent's artifacts don't count for metalcraft")
    void opponentArtifactsDontCount() {
        Permanent forger = harness.addToBattlefieldAndReturn(player1, new CarapaceForger());
        harness.addToBattlefield(player2, new Spellbook());
        harness.addToBattlefield(player2, new LeoninScimitar());
        harness.addToBattlefield(player2, new BottleGnomes());

        assertThat(gqs.getEffectivePower(gd, forger)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, forger)).isEqualTo(2);
    }
    @Test
    @DisplayName("Gains metalcraft immediately when the third artifact enters")
    void gainsMetalcraftWhenThirdArtifactEnters() {
        Permanent forger = harness.addToBattlefieldAndReturn(player1, new CarapaceForger());
        harness.addToBattlefield(player1, new Memnite());
        harness.addToBattlefield(player1, new Memnite());
        assertThat(gqs.getEffectivePower(gd, forger)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, forger)).isEqualTo(2);

        harness.enterBattlefieldAndReturn(player1, new Memnite());

        assertThat(gqs.getEffectivePower(gd, forger)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, forger)).isEqualTo(4);
    }

    @Test
    @DisplayName("Four artifacts give one boost to the Forger and none to other creatures")
    void moreThanThreeArtifactsStillGrantOnlyOneSelfBoost() {
        Permanent forger = harness.addToBattlefieldAndReturn(player1, new CarapaceForger());
        Permanent memnite = harness.addToBattlefieldAndReturn(player1, new Memnite());
        harness.addToBattlefield(player1, new Memnite());
        harness.addToBattlefield(player1, new Memnite());
        harness.addToBattlefield(player1, new Memnite());

        assertThat(gqs.getEffectivePower(gd, forger)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, forger)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, memnite)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, memnite)).isEqualTo(1);
    }

    @Test
    @DisplayName("Two own artifacts and one opposing artifact do not enable metalcraft")
    void artifactsAcrossPlayersAreNotCombined() {
        Permanent forger = harness.addToBattlefieldAndReturn(player1, new CarapaceForger());
        harness.addToBattlefield(player1, new Memnite());
        harness.addToBattlefield(player1, new Memnite());
        harness.addToBattlefield(player2, new Memnite());

        assertThat(gqs.getEffectivePower(gd, forger)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, forger)).isEqualTo(2);
    }
}

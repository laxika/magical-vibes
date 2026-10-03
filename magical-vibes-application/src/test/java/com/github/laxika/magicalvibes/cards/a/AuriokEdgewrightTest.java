package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.b.BottleGnomes;
import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.cards.m.Memnite;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;


import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AuriokEdgewright.class, Spellbook.class, LeoninScimitar.class, BottleGnomes.class, Memnite.class})
class AuriokEdgewrightTest extends BaseCardTest {

    @Test
    @DisplayName("No double strike with zero artifacts")
    void noDoubleStrikeWithZeroArtifacts() {
        Permanent edgewright = harness.addToBattlefieldAndReturn(player1, new AuriokEdgewright());

        assertThat(gqs.hasKeyword(gd, edgewright, Keyword.DOUBLE_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("No double strike with two artifacts")
    void noDoubleStrikeWithTwoArtifacts() {
        Permanent edgewright = harness.addToBattlefieldAndReturn(player1, new AuriokEdgewright());
        harness.addToBattlefield(player1, new Spellbook());
        harness.addToBattlefield(player1, new LeoninScimitar());

        assertThat(gqs.hasKeyword(gd, edgewright, Keyword.DOUBLE_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Has double strike with exactly three artifacts")
    void hasDoubleStrikeWithThreeArtifacts() {
        Permanent edgewright = harness.addToBattlefieldAndReturn(player1, new AuriokEdgewright());
        harness.addToBattlefield(player1, new Spellbook());
        harness.addToBattlefield(player1, new LeoninScimitar());
        harness.addToBattlefield(player1, new BottleGnomes());

        assertThat(gqs.hasKeyword(gd, edgewright, Keyword.DOUBLE_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("Loses double strike when artifact count drops below three")
    void losesDoubleStrikeWhenArtifactRemoved() {
        Permanent edgewright = harness.addToBattlefieldAndReturn(player1, new AuriokEdgewright());
        harness.addToBattlefield(player1, new Spellbook());
        harness.addToBattlefield(player1, new LeoninScimitar());
        harness.addToBattlefield(player1, new BottleGnomes());

        assertThat(gqs.hasKeyword(gd, edgewright, Keyword.DOUBLE_STRIKE)).isTrue();

        gd.playerBattlefields.get(player1.getId()).removeIf(p -> p.getCard().getName().equals("Bottle Gnomes"));
        assertThat(gqs.hasKeyword(gd, edgewright, Keyword.DOUBLE_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Gains double strike immediately when the third artifact enters")
    void gainsDoubleStrikeWhenThirdArtifactEnters() {
        Permanent edgewright = harness.addToBattlefieldAndReturn(player1, new AuriokEdgewright());
        harness.addToBattlefield(player1, new Memnite());
        harness.addToBattlefield(player1, new Memnite());

        assertThat(gqs.hasKeyword(gd, edgewright, Keyword.DOUBLE_STRIKE)).isFalse();

        Permanent memnite = harness.enterBattlefieldAndReturn(player1, new Memnite());

        assertThat(gqs.hasKeyword(gd, edgewright, Keyword.DOUBLE_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, memnite, Keyword.DOUBLE_STRIKE)).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Metalcraft deals damage in both combat damage steps")
    void metalcraftDealsCombatDamageTwice() {
        addCreatureReady(player1, new AuriokEdgewright());
        harness.addToBattlefield(player1, new Memnite());
        harness.addToBattlefield(player1, new Memnite());
        harness.addToBattlefield(player1, new Memnite());
        harness.setLife(player2, 20);

        declareAttackers(List.of(0));
        resolveCombat();

        harness.assertLife(player2, 16);
    }

    @Test
    @DisplayName("Without metalcraft deals damage only once")
    void withoutMetalcraftDealsCombatDamageOnce() {
        addCreatureReady(player1, new AuriokEdgewright());
        harness.addToBattlefield(player1, new Memnite());
        harness.addToBattlefield(player1, new Memnite());
        harness.setLife(player2, 20);

        declareAttackers(List.of(0));
        resolveCombat();

        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("Opponent's artifacts don't count for metalcraft")
    void opponentArtifactsDontCount() {
        Permanent edgewright = harness.addToBattlefieldAndReturn(player1, new AuriokEdgewright());
        // Opponent has 3 artifacts, controller has 0
        harness.addToBattlefield(player2, new Spellbook());
        harness.addToBattlefield(player2, new LeoninScimitar());
        harness.addToBattlefield(player2, new BottleGnomes());

        assertThat(gqs.hasKeyword(gd, edgewright, Keyword.DOUBLE_STRIKE)).isFalse();
    }
}

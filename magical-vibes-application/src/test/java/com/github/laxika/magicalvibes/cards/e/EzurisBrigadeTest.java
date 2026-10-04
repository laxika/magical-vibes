package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.m.Memnite;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({EzurisBrigade.class, Memnite.class})
class EzurisBrigadeTest extends BaseCardTest {

    @Test
    @DisplayName("Base 4/4 without trample when no metalcraft")
    void noMetalcraftBaseStats() {
        harness.addToBattlefield(player1, new EzurisBrigade());

        Permanent brigade = findBrigade();
        assertThat(gqs.getEffectivePower(gd, brigade)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, brigade)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, brigade, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Still 4/4 without trample with only two artifacts")
    void noMetalcraftWithTwoArtifacts() {
        harness.addToBattlefield(player1, new EzurisBrigade());
        harness.addToBattlefield(player1, new Memnite());
        harness.addToBattlefield(player1, new Memnite());

        Permanent brigade = findBrigade();
        assertThat(gqs.getEffectivePower(gd, brigade)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, brigade)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, brigade, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Gets +4/+4 and trample with three artifacts")
    void metalcraftWithThreeArtifacts() {
        harness.addToBattlefield(player1, new EzurisBrigade());
        harness.addToBattlefield(player1, new Memnite());
        harness.addToBattlefield(player1, new Memnite());
        harness.addToBattlefield(player1, new Memnite());

        Permanent brigade = findBrigade();
        assertThat(gqs.getEffectivePower(gd, brigade)).isEqualTo(8);
        assertThat(gqs.getEffectiveToughness(gd, brigade)).isEqualTo(8);
        assertThat(gqs.hasKeyword(gd, brigade, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("Loses boost and trample when artifact count drops below three")
    void losesMetalcraftWhenArtifactRemoved() {
        harness.addToBattlefield(player1, new EzurisBrigade());
        harness.addToBattlefield(player1, new Memnite());
        harness.addToBattlefield(player1, new Memnite());
        harness.addToBattlefield(player1, new Memnite());

        Permanent brigade = findBrigade();
        assertThat(gqs.getEffectivePower(gd, brigade)).isEqualTo(8);
        assertThat(gqs.hasKeyword(gd, brigade, Keyword.TRAMPLE)).isTrue();

        // Remove one artifact, leaving exactly two.
        gd.playerBattlefields.get(player1.getId()).remove(findPermanent(player1, "Memnite"));
        assertThat(gqs.getEffectivePower(gd, brigade)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, brigade)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, brigade, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Opponent's artifacts don't count for metalcraft")
    void opponentArtifactsDontCount() {
        harness.addToBattlefield(player1, new EzurisBrigade());
        harness.addToBattlefield(player2, new Memnite());
        harness.addToBattlefield(player2, new Memnite());
        harness.addToBattlefield(player2, new Memnite());

        Permanent brigade = findBrigade();
        assertThat(gqs.getEffectivePower(gd, brigade)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, brigade)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, brigade, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Metalcraft turns on immediately and does not stack above three artifacts")
    void gainsMetalcraftAsArtifactsEnter() {
        Permanent brigade = harness.addToBattlefieldAndReturn(player1, new EzurisBrigade());
        harness.addToBattlefield(player1, new Memnite());
        harness.addToBattlefield(player1, new Memnite());

        assertThat(gqs.getEffectivePower(gd, brigade)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, brigade)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, brigade, Keyword.TRAMPLE)).isFalse();

        Permanent thirdArtifact = harness.enterBattlefieldAndReturn(player1, new Memnite());

        assertThat(gqs.getEffectivePower(gd, brigade)).isEqualTo(8);
        assertThat(gqs.getEffectiveToughness(gd, brigade)).isEqualTo(8);
        assertThat(gqs.hasKeyword(gd, brigade, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.getEffectivePower(gd, thirdArtifact)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, thirdArtifact)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, thirdArtifact, Keyword.TRAMPLE)).isFalse();

        harness.enterBattlefieldAndReturn(player1, new Memnite());

        assertThat(gqs.getEffectivePower(gd, brigade)).isEqualTo(8);
        assertThat(gqs.getEffectiveToughness(gd, brigade)).isEqualTo(8);
        assertThat(gqs.hasKeyword(gd, brigade, Keyword.TRAMPLE)).isTrue();
    }

    private Permanent findBrigade() {
        return findPermanent(player1, "Ezuri's Brigade");
    }
}

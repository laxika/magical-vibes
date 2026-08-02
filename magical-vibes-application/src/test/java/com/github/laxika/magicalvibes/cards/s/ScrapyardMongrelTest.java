package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ScrapyardMongrelTest extends BaseCardTest {

    @Test
    @DisplayName("Is a plain 3/3 without trample when no artifact is controlled")
    void noBonusWithoutControlledArtifact() {
        harness.addToBattlefield(player1, new ScrapyardMongrel());

        Permanent mongrel = findMongrel();
        assertThat(gqs.getEffectivePower(gd, mongrel)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, mongrel)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, mongrel, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Gets +2/+0 and trample while its controller controls an artifact")
    void boostedWithControlledArtifact() {
        harness.addToBattlefield(player1, new ScrapyardMongrel());
        harness.addToBattlefield(player1, new LeoninScimitar());

        Permanent mongrel = findMongrel();
        assertThat(gqs.getEffectivePower(gd, mongrel)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, mongrel)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, mongrel, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("Loses the bonus when the controlled artifact leaves the battlefield")
    void losesBonusWhenArtifactLeavesBattlefield() {
        harness.addToBattlefield(player1, new ScrapyardMongrel());
        harness.addToBattlefield(player1, new LeoninScimitar());

        Permanent mongrel = findMongrel();
        assertThat(gqs.getEffectivePower(gd, mongrel)).isEqualTo(5);

        gd.playerBattlefields.get(player1.getId()).removeIf(permanent ->
                permanent.getCard().getName().equals("Leonin Scimitar"));

        assertThat(gqs.getEffectivePower(gd, mongrel)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, mongrel, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("An opponent's artifact does not grant the bonus")
    void opponentArtifactDoesNotCount() {
        harness.addToBattlefield(player1, new ScrapyardMongrel());
        harness.addToBattlefield(player2, new LeoninScimitar());

        Permanent mongrel = findMongrel();
        assertThat(gqs.getEffectivePower(gd, mongrel)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, mongrel, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("A non-artifact permanent does not grant the bonus")
    void nonArtifactPermanentDoesNotCount() {
        harness.addToBattlefield(player1, new ScrapyardMongrel());
        harness.addToBattlefield(player1, new Island());

        Permanent mongrel = findMongrel();
        assertThat(gqs.getEffectivePower(gd, mongrel)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, mongrel, Keyword.TRAMPLE)).isFalse();
    }

    private Permanent findMongrel() {
        return findPermanent(player1, "Scrapyard Mongrel");
    }
}

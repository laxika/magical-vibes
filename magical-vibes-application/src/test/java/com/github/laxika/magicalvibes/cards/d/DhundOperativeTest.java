package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DhundOperativeTest extends BaseCardTest {

    @Test
    @DisplayName("Is a 2/2 without deathtouch when its controller controls no artifact")
    void noBonusWithoutControlledArtifact() {
        harness.addToBattlefield(player1, new DhundOperative());

        Permanent operative = findOperative();
        assertThat(gqs.getEffectivePower(gd, operative)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, operative)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, operative, Keyword.DEATHTOUCH)).isFalse();
    }

    @Test
    @DisplayName("Gets +1/+0 and deathtouch while its controller controls an artifact")
    void boostedWithControlledArtifact() {
        harness.addToBattlefield(player1, new DhundOperative());
        harness.addToBattlefield(player1, new LeoninScimitar());

        Permanent operative = findOperative();
        assertThat(gqs.getEffectivePower(gd, operative)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, operative)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, operative, Keyword.DEATHTOUCH)).isTrue();
    }

    @Test
    @DisplayName("Loses the bonus when the controlled artifact leaves the battlefield")
    void losesBonusWhenArtifactLeavesBattlefield() {
        harness.addToBattlefield(player1, new DhundOperative());
        harness.addToBattlefield(player1, new LeoninScimitar());

        Permanent operative = findOperative();
        assertThat(gqs.getEffectivePower(gd, operative)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, operative, Keyword.DEATHTOUCH)).isTrue();

        gd.playerBattlefields.get(player1.getId()).removeIf(permanent ->
                permanent.getCard().getName().equals("Leonin Scimitar"));

        assertThat(gqs.getEffectivePower(gd, operative)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, operative, Keyword.DEATHTOUCH)).isFalse();
    }

    @Test
    @DisplayName("An opponent's artifact does not grant the bonus")
    void opponentArtifactDoesNotCount() {
        harness.addToBattlefield(player1, new DhundOperative());
        harness.addToBattlefield(player2, new LeoninScimitar());

        Permanent operative = findOperative();
        assertThat(gqs.getEffectivePower(gd, operative)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, operative, Keyword.DEATHTOUCH)).isFalse();
    }

    @Test
    @DisplayName("A non-artifact permanent does not grant the bonus")
    void nonArtifactPermanentDoesNotCount() {
        harness.addToBattlefield(player1, new DhundOperative());
        harness.addToBattlefield(player1, new Island());

        Permanent operative = findOperative();
        assertThat(gqs.getEffectivePower(gd, operative)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, operative, Keyword.DEATHTOUCH)).isFalse();
    }

    private Permanent findOperative() {
        return findPermanent(player1, "Dhund Operative");
    }
}

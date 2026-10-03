package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.p.PropheticPrism;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DhundOperative.class, PropheticPrism.class, Island.class})
class DhundOperativeTest extends BaseCardTest {

    @Test
    @DisplayName("Is a 2/2 without deathtouch when its controller controls no artifact")
    void noBonusWithoutControlledArtifact() {
        Permanent operative = harness.addToBattlefieldAndReturn(player1, new DhundOperative());

        assertThat(gqs.getEffectivePower(gd, operative)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, operative)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, operative, Keyword.DEATHTOUCH)).isFalse();
    }

    @Test
    @DisplayName("Gets +1/+0 and deathtouch while its controller controls an artifact")
    void boostedWithControlledArtifact() {
        Permanent operative = harness.addToBattlefieldAndReturn(player1, new DhundOperative());
        harness.addToBattlefield(player1, new PropheticPrism());

        assertThat(gqs.getEffectivePower(gd, operative)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, operative)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, operative, Keyword.DEATHTOUCH)).isTrue();
    }

    @Test
    @DisplayName("Loses the bonus when the controlled artifact leaves the battlefield")
    void losesBonusWhenArtifactLeavesBattlefield() {
        Permanent operative = harness.addToBattlefieldAndReturn(player1, new DhundOperative());
        harness.addToBattlefield(player1, new PropheticPrism());

        assertThat(gqs.getEffectivePower(gd, operative)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, operative, Keyword.DEATHTOUCH)).isTrue();

        gd.playerBattlefields.get(player1.getId()).removeIf(permanent ->
                permanent.getCard().getName().equals("Prophetic Prism"));

        assertThat(gqs.getEffectivePower(gd, operative)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, operative, Keyword.DEATHTOUCH)).isFalse();
    }

    @Test
    @DisplayName("An opponent's artifact does not grant the bonus")
    void opponentArtifactDoesNotCount() {
        Permanent operative = harness.addToBattlefieldAndReturn(player1, new DhundOperative());
        harness.addToBattlefield(player2, new PropheticPrism());

        assertThat(gqs.getEffectivePower(gd, operative)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, operative, Keyword.DEATHTOUCH)).isFalse();
    }

    @Test
    @DisplayName("A non-artifact permanent does not grant the bonus")
    void nonArtifactPermanentDoesNotCount() {
        Permanent operative = harness.addToBattlefieldAndReturn(player1, new DhundOperative());
        harness.addToBattlefield(player1, new Island());

        assertThat(gqs.getEffectivePower(gd, operative)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, operative, Keyword.DEATHTOUCH)).isFalse();
    }

    @Test
    @DisplayName("Gains the bonus immediately when an artifact is added later")
    void gainsBonusWhenArtifactArrives() {
        Permanent operative = harness.addToBattlefieldAndReturn(player1, new DhundOperative());
        assertThat(gqs.getEffectivePower(gd, operative)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, operative, Keyword.DEATHTOUCH)).isFalse();

        harness.addToBattlefield(player1, new PropheticPrism());

        assertThat(gqs.getEffectivePower(gd, operative)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, operative)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, operative, Keyword.DEATHTOUCH)).isTrue();
    }

    @Test
    @DisplayName("Multiple artifacts grant only one bonus, retained while an artifact remains")
    void multipleArtifactsDoNotStackTheBonus() {
        Permanent operative = harness.addToBattlefieldAndReturn(player1, new DhundOperative());
        Permanent firstArtifact = harness.addToBattlefieldAndReturn(player1, new PropheticPrism());
        harness.addToBattlefield(player1, new PropheticPrism());

        assertThat(gqs.getEffectivePower(gd, operative)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, operative)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, operative, Keyword.DEATHTOUCH)).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(firstArtifact);

        assertThat(gqs.getEffectivePower(gd, operative)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, operative)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, operative, Keyword.DEATHTOUCH)).isTrue();
    }

    @Test
    @DisplayName("Artifacts in hand and graveyard do not grant the bonus")
    void artifactsOutsideBattlefieldDoNotCount() {
        Permanent operative = harness.addToBattlefieldAndReturn(player1, new DhundOperative());
        harness.setHand(player1, List.of(new PropheticPrism()));
        harness.setGraveyard(player1, List.of(new PropheticPrism()));

        assertThat(gqs.getEffectivePower(gd, operative)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, operative)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, operative, Keyword.DEATHTOUCH)).isFalse();
    }
}

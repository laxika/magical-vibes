package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Gaelicat.class, Spellbook.class})
class GaelicatTest extends BaseCardTest {

    @Test
    @DisplayName("Has base stats with fewer than two artifacts")
    void hasBaseStatsWithFewerThanTwoArtifacts() {
        Permanent gaelicat = harness.addToBattlefieldAndReturn(player1, new Gaelicat());
        harness.addToBattlefield(player1, new Spellbook());

        assertThat(gqs.getEffectivePower(gd, gaelicat)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, gaelicat)).isEqualTo(3);
    }

    @Test
    @DisplayName("Gets +2/+0 with two artifacts")
    void getsBoostWithTwoArtifacts() {
        Permanent gaelicat = harness.addToBattlefieldAndReturn(player1, new Gaelicat());
        harness.addToBattlefield(player1, new Spellbook());
        harness.addToBattlefield(player1, new Spellbook());

        assertThat(gqs.getEffectivePower(gd, gaelicat)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, gaelicat)).isEqualTo(3);
    }

    @Test
    @DisplayName("Opponent artifacts do not count")
    void opponentArtifactsDoNotCount() {
        Permanent gaelicat = harness.addToBattlefieldAndReturn(player1, new Gaelicat());
        harness.addToBattlefield(player2, new Spellbook());
        harness.addToBattlefield(player2, new Spellbook());

        assertThat(gqs.getEffectivePower(gd, gaelicat)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, gaelicat)).isEqualTo(3);
    }

    @Test
    @DisplayName("Loses the boost when an artifact is removed")
    void losesBoostWhenArtifactRemoved() {
        Permanent gaelicat = harness.addToBattlefieldAndReturn(player1, new Gaelicat());
        harness.addToBattlefield(player1, new Spellbook());
        harness.addToBattlefield(player1, new Spellbook());

        assertThat(gqs.getEffectivePower(gd, gaelicat)).isEqualTo(3);

        gd.playerBattlefields.get(player1.getId()).removeIf(p -> p.getCard() instanceof Spellbook);

        assertThat(gqs.getEffectivePower(gd, gaelicat)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, gaelicat)).isEqualTo(3);
    }
}

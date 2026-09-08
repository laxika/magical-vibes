package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class NimGrotesqueTest extends BaseCardTest {

    @Test
    @DisplayName("Gets +1/+0 for each artifact you control")
    void scalesWithControlledArtifacts() {
        Permanent nim = harness.addToBattlefieldAndReturn(player1, new NimGrotesque());
        harness.addToBattlefield(player1, new Spellbook());
        harness.addToBattlefield(player1, new LeoninScimitar());

        assertThat(gqs.getEffectivePower(gd, nim)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, nim)).isEqualTo(6);
    }

    @Test
    @DisplayName("Has no bonus with no artifacts")
    void hasNoBonusWithoutArtifacts() {
        Permanent nim = harness.addToBattlefieldAndReturn(player1, new NimGrotesque());

        assertThat(gqs.getEffectivePower(gd, nim)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, nim)).isEqualTo(6);
    }

    @Test
    @DisplayName("Counts only artifacts controlled by its controller")
    void opponentArtifactsDoNotCount() {
        Permanent nim = harness.addToBattlefieldAndReturn(player1, new NimGrotesque());
        harness.addToBattlefield(player2, new Spellbook());
        harness.addToBattlefield(player2, new LeoninScimitar());

        assertThat(gqs.getEffectivePower(gd, nim)).isEqualTo(3);
    }

    @Test
    @DisplayName("Bonus updates when a controlled artifact leaves")
    void updatesWhenArtifactLeaves() {
        Permanent nim = harness.addToBattlefieldAndReturn(player1, new NimGrotesque());
        harness.addToBattlefield(player1, new Spellbook());
        harness.addToBattlefield(player1, new LeoninScimitar());

        assertThat(gqs.getEffectivePower(gd, nim)).isEqualTo(5);

        gd.playerBattlefields.get(player1.getId()).removeIf(
                permanent -> permanent.getCard().getName().equals("Spellbook"));

        assertThat(gqs.getEffectivePower(gd, nim)).isEqualTo(4);
    }
}

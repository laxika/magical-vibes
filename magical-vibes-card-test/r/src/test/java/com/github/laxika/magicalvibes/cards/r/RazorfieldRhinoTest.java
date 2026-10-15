package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RazorfieldRhino.class, Spellbook.class})
class RazorfieldRhinoTest extends BaseCardTest {

    @Test
    @DisplayName("Base 4/4 without metalcraft (only itself as artifact)")
    void noMetalcraftBaseStats() {
        harness.addToBattlefield(player1, new RazorfieldRhino());

        Permanent rhino = findRhino();
        // Rhino is itself an artifact, so only 1 artifact — no metalcraft
        assertThat(gqs.getEffectivePower(gd, rhino)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, rhino)).isEqualTo(4);
    }

    @Test
    @DisplayName("Still 4/4 with only two artifacts total (itself + one)")
    void noMetalcraftWithTwoArtifacts() {
        harness.addToBattlefield(player1, new RazorfieldRhino());
        harness.addToBattlefield(player1, new Spellbook());

        Permanent rhino = findRhino();
        assertThat(gqs.getEffectivePower(gd, rhino)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, rhino)).isEqualTo(4);
    }

    @Test
    @DisplayName("Gets +2/+2 with three artifacts (itself + two) becoming 6/6")
    void metalcraftWithThreeArtifacts() {
        harness.addToBattlefield(player1, new RazorfieldRhino());
        harness.addToBattlefield(player1, new Spellbook());
        harness.addToBattlefield(player1, new Spellbook());

        Permanent rhino = findRhino();
        assertThat(gqs.getEffectivePower(gd, rhino)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, rhino)).isEqualTo(6);
    }

    @Test
    @DisplayName("Loses boost when artifact count drops below three")
    void losesMetalcraftWhenArtifactRemoved() {
        harness.addToBattlefield(player1, new RazorfieldRhino());
        harness.addToBattlefield(player1, new Spellbook());
        harness.addToBattlefield(player1, new Spellbook());

        Permanent rhino = findRhino();
        assertThat(gqs.getEffectivePower(gd, rhino)).isEqualTo(6);

        // Remove one Spellbook — now only 2 artifacts (Rhino + 1 Spellbook)
        gd.playerBattlefields.get(player1.getId()).remove(findPermanent(player1, "Spellbook"));

        assertThat(gqs.getEffectivePower(gd, rhino)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, rhino)).isEqualTo(4);
    }

    @Test
    @DisplayName("Opponent's artifacts don't count for metalcraft")
    void opponentArtifactsDontCount() {
        harness.addToBattlefield(player1, new RazorfieldRhino());
        harness.addToBattlefield(player2, new Spellbook());
        harness.addToBattlefield(player2, new Spellbook());
        harness.addToBattlefield(player2, new Spellbook());

        Permanent rhino = findRhino();
        // Only 1 artifact under player1's control (Rhino itself)
        assertThat(gqs.getEffectivePower(gd, rhino)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, rhino)).isEqualTo(4);
    }


    @Test
    @CardUsed({RazorfieldRhino.class})
    @DisplayName("Metalcraft turns on immediately when the third artifact enters")
    void gainsMetalcraftAfterEntering() {
        Permanent rhino = harness.addToBattlefieldAndReturn(player1, new RazorfieldRhino());
        harness.addToBattlefield(player1, new RazorfieldRhino());
        assertThat(gqs.getEffectivePower(gd, rhino)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, rhino)).isEqualTo(4);

        Permanent thirdArtifact = harness.enterBattlefieldAndReturn(player1, new RazorfieldRhino());

        assertThat(gqs.getEffectivePower(gd, rhino)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, rhino)).isEqualTo(6);
        assertThat(gqs.getEffectivePower(gd, thirdArtifact)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, thirdArtifact)).isEqualTo(6);
    }

    @Test
    @CardUsed({RazorfieldRhino.class})
    @DisplayName("Four artifacts still grant only one +2/+2 boost")
    void extraArtifactsDoNotIncreaseBoost() {
        Permanent rhino = harness.addToBattlefieldAndReturn(player1, new RazorfieldRhino());
        harness.addToBattlefield(player1, new RazorfieldRhino());
        harness.addToBattlefield(player1, new RazorfieldRhino());
        harness.addToBattlefield(player1, new RazorfieldRhino());

        assertThat(gqs.getEffectivePower(gd, rhino)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, rhino)).isEqualTo(6);
    }

    private Permanent findRhino() {
        return findPermanent(player1, "Razorfield Rhino");
    }
}

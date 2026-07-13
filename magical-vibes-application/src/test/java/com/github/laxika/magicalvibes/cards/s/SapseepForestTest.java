package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

// Note: the engine models a land's color as its color identity (MtgjsonOracleLoader), so
// Sapseep Forest itself counts as one green permanent toward its "two or more green
// permanents" activation restriction. Tests are written against that engine color model.
class SapseepForestTest extends BaseCardTest {

    @Test
    @DisplayName("Gain-life ability gains 1 life when controlling two or more green permanents")
    void gainLifeWithTwoGreenPermanents() {
        Permanent forest = addForest(player1);
        addGreenPermanents(player1, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.setLife(player1, 20);

        int forestIdx = gd.playerBattlefields.get(player1.getId()).indexOf(forest);
        harness.activateAbility(player1, forestIdx, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(21);
    }

    @Test
    @DisplayName("Gain-life ability cannot be activated with fewer than two green permanents")
    void gainLifeRejectedWithTooFewGreenPermanents() {
        // Only the forest itself (one green permanent) — below the two-permanent threshold.
        Permanent forest = addForest(player1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        int forestIdx = gd.playerBattlefields.get(player1.getId()).indexOf(forest);
        assertThatThrownBy(() -> harness.activateAbility(player1, forestIdx, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Tap ability adds green mana")
    void manaAbilityAddsGreen() {
        Permanent forest = addForest(player1);

        int forestIdx = gd.playerBattlefields.get(player1.getId()).indexOf(forest);
        harness.activateAbility(player1, forestIdx, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
    }

    // ===== Helpers =====

    private Permanent addForest(Player player) {
        harness.addToBattlefield(player, new SapseepForest());
        Permanent forest = findPermanent(player, "Sapseep Forest");
        forest.setSummoningSick(false);
        forest.untap();
        return forest;
    }

    private void addGreenPermanents(Player player, int count) {
        for (int i = 0; i < count; i++) {
            Permanent p = new Permanent(new GrizzlyBears());
            p.setSummoningSick(false);
            gd.playerBattlefields.get(player.getId()).add(p);
        }
    }
}

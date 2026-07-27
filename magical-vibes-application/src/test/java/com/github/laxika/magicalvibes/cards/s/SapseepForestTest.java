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

// Note: Sapseep Forest is colorless (CR 202.2 — a land has no mana cost), so it does not count
// itself toward its own "two or more green permanents" activation restriction.
class SapseepForestTest extends BaseCardTest {

    @Test
    @DisplayName("Gain-life ability gains 1 life when controlling two or more green permanents")
    void gainLifeWithTwoGreenPermanents() {
        Permanent forest = addForest(player1);
        addGreenPermanents(player1, 2); // the colorless land contributes nothing, so both come from here
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
        Permanent forest = addForest(player1);
        // One green permanent — the colorless forest does not make up the second.
        addGreenPermanents(player1, 1);
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

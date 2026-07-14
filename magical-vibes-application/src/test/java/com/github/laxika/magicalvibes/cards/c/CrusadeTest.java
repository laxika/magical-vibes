package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.e.EliteVanguard;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CrusadeTest extends BaseCardTest {

    private Permanent find(com.github.laxika.magicalvibes.model.Player player, String name) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(p -> p.getCard().getName().equals(name))
                .findFirst().orElseThrow();
    }

    // ===== Buffs white creatures (all controllers) =====

    @Test
    @DisplayName("Own white creatures get +1/+1")
    void buffsOwnWhiteCreatures() {
        harness.addToBattlefield(player1, new Crusade());
        harness.addToBattlefield(player1, new EliteVanguard());

        Permanent vanguard = find(player1, "Elite Vanguard");

        assertThat(gqs.getEffectivePower(gd, vanguard)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, vanguard)).isEqualTo(2);
    }

    @Test
    @DisplayName("Opponent's white creatures also get +1/+1")
    void buffsOpponentWhiteCreatures() {
        harness.addToBattlefield(player1, new Crusade());
        harness.addToBattlefield(player2, new EliteVanguard());

        Permanent vanguard = find(player2, "Elite Vanguard");

        assertThat(gqs.getEffectivePower(gd, vanguard)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, vanguard)).isEqualTo(2);
    }

    // ===== Does not affect nonwhite creatures =====

    @Test
    @DisplayName("Nonwhite creatures are unaffected")
    void doesNotBuffNonwhiteCreatures() {
        harness.addToBattlefield(player1, new Crusade());
        harness.addToBattlefield(player1, new GrizzlyBears());

        Permanent bears = find(player1, "Grizzly Bears");

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    // ===== Stacking =====

    @Test
    @DisplayName("Two Crusades give +2/+2 to white creatures")
    void twoCrusadesStack() {
        harness.addToBattlefield(player1, new Crusade());
        harness.addToBattlefield(player1, new Crusade());
        harness.addToBattlefield(player1, new EliteVanguard());

        Permanent vanguard = find(player1, "Elite Vanguard");

        assertThat(gqs.getEffectivePower(gd, vanguard)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, vanguard)).isEqualTo(3);
    }

    // ===== Bonus gone when source leaves =====

    @Test
    @DisplayName("Bonus removed when Crusade leaves the battlefield")
    void bonusRemovedWhenSourceLeaves() {
        harness.addToBattlefield(player1, new Crusade());
        harness.addToBattlefield(player1, new EliteVanguard());

        Permanent vanguard = find(player1, "Elite Vanguard");
        assertThat(gqs.getEffectivePower(gd, vanguard)).isEqualTo(3);

        gd.playerBattlefields.get(player1.getId())
                .removeIf(p -> p.getCard().getName().equals("Crusade"));

        assertThat(gqs.getEffectivePower(gd, vanguard)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, vanguard)).isEqualTo(1);
    }
}

package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.w.WhiteKnight;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Crusade.class, WhiteKnight.class, GrizzlyBears.class})
class CrusadeTest extends BaseCardTest {

    // ===== Buffs white creatures (all controllers) =====

    @Test
    @DisplayName("Own white creatures get +1/+1")
    void buffsOwnWhiteCreatures() {
        harness.addToBattlefield(player1, new Crusade());
        Permanent vanguard = harness.addToBattlefieldAndReturn(player1, new WhiteKnight());

        assertThat(gqs.getEffectivePower(gd, vanguard)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, vanguard)).isEqualTo(3);
    }

    @Test
    @DisplayName("Opponent's white creatures also get +1/+1")
    void buffsOpponentWhiteCreatures() {
        harness.addToBattlefield(player1, new Crusade());
        Permanent vanguard = harness.addToBattlefieldAndReturn(player2, new WhiteKnight());

        assertThat(gqs.getEffectivePower(gd, vanguard)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, vanguard)).isEqualTo(3);
    }

    // ===== Does not affect nonwhite creatures =====

    @Test
    @DisplayName("Nonwhite creatures are unaffected")
    void doesNotBuffNonwhiteCreatures() {
        harness.addToBattlefield(player1, new Crusade());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    // ===== Stacking =====

    @Test
    @DisplayName("Two Crusades give +2/+2 to white creatures")
    void twoCrusadesStack() {
        harness.addToBattlefield(player1, new Crusade());
        harness.addToBattlefield(player1, new Crusade());
        Permanent vanguard = harness.addToBattlefieldAndReturn(player1, new WhiteKnight());

        assertThat(gqs.getEffectivePower(gd, vanguard)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, vanguard)).isEqualTo(4);
    }

    // ===== Bonus gone when source leaves =====

    @Test
    @DisplayName("Bonus removed when Crusade leaves the battlefield")
    void bonusRemovedWhenSourceLeaves() {
        Permanent crusade = harness.addToBattlefieldAndReturn(player1, new Crusade());
        Permanent vanguard = harness.addToBattlefieldAndReturn(player1, new WhiteKnight());

        assertThat(gqs.getEffectivePower(gd, vanguard)).isEqualTo(3);

        gd.playerBattlefields.get(player1.getId()).remove(crusade);

        assertThat(gqs.getEffectivePower(gd, vanguard)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, vanguard)).isEqualTo(2);
    }
}

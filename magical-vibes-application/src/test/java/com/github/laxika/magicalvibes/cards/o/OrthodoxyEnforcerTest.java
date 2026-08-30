package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class OrthodoxyEnforcerTest extends BaseCardTest {

    @Test
    @DisplayName("Has base power and toughness with fewer than two artifacts")
    void hasBaseStatsWithFewerThanTwoArtifacts() {
        Permanent enforcer = harness.addToBattlefieldAndReturn(player1, new OrthodoxyEnforcer());
        harness.addToBattlefield(player1, new Spellbook());

        assertThat(gqs.getEffectivePower(gd, enforcer)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, enforcer)).isEqualTo(4);
    }

    @Test
    @DisplayName("Gets +2/+0 with two artifacts")
    void getsBoostWithTwoArtifacts() {
        Permanent enforcer = harness.addToBattlefieldAndReturn(player1, new OrthodoxyEnforcer());
        harness.addToBattlefield(player1, new Spellbook());
        harness.addToBattlefield(player1, new LeoninScimitar());

        assertThat(gqs.getEffectivePower(gd, enforcer)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, enforcer)).isEqualTo(4);
    }

    @Test
    @DisplayName("Loses the boost when an artifact is removed")
    void losesBoostWhenArtifactRemoved() {
        Permanent enforcer = harness.addToBattlefieldAndReturn(player1, new OrthodoxyEnforcer());
        harness.addToBattlefield(player1, new Spellbook());
        harness.addToBattlefield(player1, new LeoninScimitar());

        assertThat(gqs.getEffectivePower(gd, enforcer)).isEqualTo(4);

        gd.playerBattlefields.get(player1.getId()).removeIf(p -> p.getCard().getName().equals("Leonin Scimitar"));

        assertThat(gqs.getEffectivePower(gd, enforcer)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, enforcer)).isEqualTo(4);
    }

    @Test
    @DisplayName("Opponent artifacts do not count")
    void opponentArtifactsDoNotCount() {
        Permanent enforcer = harness.addToBattlefieldAndReturn(player1, new OrthodoxyEnforcer());
        harness.addToBattlefield(player2, new Spellbook());
        harness.addToBattlefield(player2, new LeoninScimitar());

        assertThat(gqs.getEffectivePower(gd, enforcer)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, enforcer)).isEqualTo(4);
    }
}

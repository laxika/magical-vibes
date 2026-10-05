package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({OrthodoxyEnforcer.class, Spellbook.class, LeoninScimitar.class})
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
    @Test
    @DisplayName("Vigilance keeps the creature untapped when attacking without artifacts")
    void attacksWithoutTappingWithoutArtifacts() {
        Permanent enforcer = harness.addToBattlefieldAndReturn(player1, new OrthodoxyEnforcer());
        enforcer.setSummoningSick(false);

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThat(enforcer.isTapped()).isFalse();
        resolveCombat();
        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("The artifact boost applies to combat damage and vigilance remains active")
    void attacksWithBoostWithoutTapping() {
        Permanent enforcer = harness.addToBattlefieldAndReturn(player1, new OrthodoxyEnforcer());
        enforcer.setSummoningSick(false);
        harness.addToBattlefield(player1, new Spellbook());
        harness.addToBattlefield(player1, new LeoninScimitar());

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThat(enforcer.isTapped()).isFalse();
        resolveCombat();
        harness.assertLife(player2, 16);
    }

    @Test
    @DisplayName("More than two artifacts give the same boost, including duplicate artifacts")
    void boostDoesNotScaleWithArtifactCount() {
        Permanent enforcer = harness.addToBattlefieldAndReturn(player1, new OrthodoxyEnforcer());
        harness.addToBattlefield(player1, new Spellbook());
        harness.addToBattlefield(player1, new Spellbook());
        harness.addToBattlefield(player1, new LeoninScimitar());

        assertThat(gqs.getEffectivePower(gd, enforcer)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, enforcer)).isEqualTo(4);
    }

    @Test
    @DisplayName("One own artifact and one opposing artifact do not satisfy the condition")
    void artifactsOfDifferentControllersDoNotCombine() {
        Permanent enforcer = harness.addToBattlefieldAndReturn(player1, new OrthodoxyEnforcer());
        harness.addToBattlefield(player1, new Spellbook());
        harness.addToBattlefield(player2, new LeoninScimitar());

        assertThat(gqs.getEffectivePower(gd, enforcer)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, enforcer)).isEqualTo(4);
    }
}

package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ArdentRecruitTest extends BaseCardTest {

    // ===== Without metalcraft =====

    @Test
    @DisplayName("Base 1/1 without metalcraft")
    void noMetalcraftBaseStats() {
        harness.addToBattlefield(player1, new ArdentRecruit());

        Permanent recruit = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(gqs.getEffectivePower(gd, recruit)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, recruit)).isEqualTo(1);
    }

    @Test
    @DisplayName("Still 1/1 with only two artifacts")
    void noMetalcraftWithTwoArtifacts() {
        harness.addToBattlefield(player1, new ArdentRecruit());
        harness.addToBattlefield(player1, new Spellbook());
        harness.addToBattlefield(player1, new Spellbook());

        Permanent recruit = findRecruit();
        assertThat(gqs.getEffectivePower(gd, recruit)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, recruit)).isEqualTo(1);
    }

    // ===== With metalcraft =====

    @Test
    @DisplayName("Gets +2/+2 with three artifacts becoming 3/3")
    void metalcraftWithThreeArtifacts() {
        harness.addToBattlefield(player1, new ArdentRecruit());
        harness.addToBattlefield(player1, new Spellbook());
        harness.addToBattlefield(player1, new Spellbook());
        harness.addToBattlefield(player1, new Spellbook());

        Permanent recruit = findRecruit();
        assertThat(gqs.getEffectivePower(gd, recruit)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, recruit)).isEqualTo(3);
    }

    // ===== Metalcraft lost =====

    @Test
    @DisplayName("Loses boost when artifact count drops below three")
    void losesMetalcraftWhenArtifactRemoved() {
        harness.addToBattlefield(player1, new ArdentRecruit());
        harness.addToBattlefield(player1, new Spellbook());
        harness.addToBattlefield(player1, new Spellbook());
        harness.addToBattlefield(player1, new Spellbook());

        Permanent recruit = findRecruit();
        assertThat(gqs.getEffectivePower(gd, recruit)).isEqualTo(3);

        // Remove one artifact — now only 2
        gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Spellbook"))
                .findFirst()
                .ifPresent(p -> gd.playerBattlefields.get(player1.getId()).remove(p));

        assertThat(gqs.getEffectivePower(gd, recruit)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, recruit)).isEqualTo(1);
    }

    // ===== Opponent's artifacts =====

    @Test
    @DisplayName("Opponent's artifacts don't count for metalcraft")
    void opponentArtifactsDontCount() {
        harness.addToBattlefield(player1, new ArdentRecruit());
        harness.addToBattlefield(player2, new Spellbook());
        harness.addToBattlefield(player2, new Spellbook());
        harness.addToBattlefield(player2, new Spellbook());

        Permanent recruit = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(gqs.getEffectivePower(gd, recruit)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, recruit)).isEqualTo(1);
    }

    // ===== Helpers =====

    private Permanent findRecruit() {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Ardent Recruit"))
                .findFirst().orElseThrow();
    }
}

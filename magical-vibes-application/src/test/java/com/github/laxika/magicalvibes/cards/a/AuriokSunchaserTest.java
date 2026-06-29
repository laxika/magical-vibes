package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.b.BottleGnomes;
import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AuriokSunchaserTest extends BaseCardTest {

    // ===== Without metalcraft =====

    @Test
    @DisplayName("No flying and base 1/1 with zero artifacts")
    void noMetalcraftWithZeroArtifacts() {
        harness.addToBattlefield(player1, new AuriokSunchaser());

        Permanent sunchaser = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(gqs.hasKeyword(gd, sunchaser, Keyword.FLYING)).isFalse();
        assertThat(gqs.getEffectivePower(gd, sunchaser)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, sunchaser)).isEqualTo(1);
    }

    @Test
    @DisplayName("No flying and base 1/1 with two artifacts")
    void noMetalcraftWithTwoArtifacts() {
        harness.addToBattlefield(player1, new AuriokSunchaser());
        harness.addToBattlefield(player1, new Spellbook());
        harness.addToBattlefield(player1, new LeoninScimitar());

        Permanent sunchaser = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Auriok Sunchaser"))
                .findFirst().orElseThrow();
        assertThat(gqs.hasKeyword(gd, sunchaser, Keyword.FLYING)).isFalse();
        assertThat(gqs.getEffectivePower(gd, sunchaser)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, sunchaser)).isEqualTo(1);
    }

    // ===== With metalcraft =====

    @Test
    @DisplayName("Has flying and 3/3 with exactly three artifacts")
    void metalcraftWithThreeArtifacts() {
        harness.addToBattlefield(player1, new AuriokSunchaser());
        harness.addToBattlefield(player1, new Spellbook());
        harness.addToBattlefield(player1, new LeoninScimitar());
        harness.addToBattlefield(player1, new BottleGnomes());

        Permanent sunchaser = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Auriok Sunchaser"))
                .findFirst().orElseThrow();
        assertThat(gqs.hasKeyword(gd, sunchaser, Keyword.FLYING)).isTrue();
        assertThat(gqs.getEffectivePower(gd, sunchaser)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, sunchaser)).isEqualTo(3);
    }

    // ===== Metalcraft lost =====

    @Test
    @DisplayName("Loses flying and boost when artifact count drops below three")
    void losesMetalcraftWhenArtifactRemoved() {
        harness.addToBattlefield(player1, new AuriokSunchaser());
        harness.addToBattlefield(player1, new Spellbook());
        harness.addToBattlefield(player1, new LeoninScimitar());
        harness.addToBattlefield(player1, new BottleGnomes());

        Permanent sunchaser = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Auriok Sunchaser"))
                .findFirst().orElseThrow();
        assertThat(gqs.hasKeyword(gd, sunchaser, Keyword.FLYING)).isTrue();
        assertThat(gqs.getEffectivePower(gd, sunchaser)).isEqualTo(3);

        // Remove one artifact — now only 2
        gd.playerBattlefields.get(player1.getId()).removeIf(p -> p.getCard().getName().equals("Bottle Gnomes"));
        assertThat(gqs.hasKeyword(gd, sunchaser, Keyword.FLYING)).isFalse();
        assertThat(gqs.getEffectivePower(gd, sunchaser)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, sunchaser)).isEqualTo(1);
    }

    @Test
    @DisplayName("Opponent's artifacts don't count for metalcraft")
    void opponentArtifactsDontCount() {
        harness.addToBattlefield(player1, new AuriokSunchaser());
        harness.addToBattlefield(player2, new Spellbook());
        harness.addToBattlefield(player2, new LeoninScimitar());
        harness.addToBattlefield(player2, new BottleGnomes());

        Permanent sunchaser = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(gqs.hasKeyword(gd, sunchaser, Keyword.FLYING)).isFalse();
        assertThat(gqs.getEffectivePower(gd, sunchaser)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, sunchaser)).isEqualTo(1);
    }
}

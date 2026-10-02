package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.b.BottleGnomes;
import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AuriokSunchaser.class, Spellbook.class, LeoninScimitar.class, BottleGnomes.class})
class AuriokSunchaserTest extends BaseCardTest {

    // ===== Without metalcraft =====

    @Test
    @DisplayName("No flying and base 1/1 with zero artifacts")
    void noMetalcraftWithZeroArtifacts() {
        Permanent sunchaser = harness.addToBattlefieldAndReturn(player1, new AuriokSunchaser());
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

        Permanent sunchaser = findPermanent(player1, "Auriok Sunchaser");
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

        Permanent sunchaser = findPermanent(player1, "Auriok Sunchaser");
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

        Permanent sunchaser = findPermanent(player1, "Auriok Sunchaser");
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

    @Test
    @DisplayName("Gains flying and +2/+2 immediately when the third artifact enters")
    void gainsMetalcraftAfterEntering() {
        Permanent sunchaser = harness.addToBattlefieldAndReturn(player1, new AuriokSunchaser());
        harness.addToBattlefield(player1, new Spellbook());
        harness.addToBattlefield(player1, new LeoninScimitar());

        assertThat(gqs.hasKeyword(gd, sunchaser, Keyword.FLYING)).isFalse();
        assertThat(gqs.getEffectivePower(gd, sunchaser)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, sunchaser)).isEqualTo(1);

        harness.addToBattlefield(player1, new BottleGnomes());

        assertThat(gqs.hasKeyword(gd, sunchaser, Keyword.FLYING)).isTrue();
        assertThat(gqs.getEffectivePower(gd, sunchaser)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, sunchaser)).isEqualTo(3);
    }

    @Test
    @DisplayName("More than three artifacts grant the bonus only once, including tapped artifacts")
    void metalcraftWithFourTappedArtifacts() {
        Permanent sunchaser = harness.addToBattlefieldAndReturn(player1, new AuriokSunchaser());
        for (int i = 0; i < 4; i++) {
            harness.addToBattlefieldAndReturn(player1, new Spellbook()).setTapped(true);
        }

        assertThat(gqs.hasKeyword(gd, sunchaser, Keyword.FLYING)).isTrue();
        assertThat(gqs.getEffectivePower(gd, sunchaser)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, sunchaser)).isEqualTo(3);
    }

    @Test
    @DisplayName("Nonartifact creatures do not count toward metalcraft")
    void nonartifactCreaturesDoNotCount() {
        Permanent sunchaser = harness.addToBattlefieldAndReturn(player1, new AuriokSunchaser());
        harness.addToBattlefield(player1, new AuriokSunchaser());
        harness.addToBattlefield(player1, new AuriokSunchaser());
        harness.addToBattlefield(player1, new Spellbook());
        harness.addToBattlefield(player1, new LeoninScimitar());

        assertThat(gqs.hasKeyword(gd, sunchaser, Keyword.FLYING)).isFalse();
        assertThat(gqs.getEffectivePower(gd, sunchaser)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, sunchaser)).isEqualTo(1);
    }
}

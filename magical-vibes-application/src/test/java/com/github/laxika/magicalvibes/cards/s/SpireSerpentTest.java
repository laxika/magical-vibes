package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.b.BottleGnomes;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SpireSerpent.class, Spellbook.class, LeoninScimitar.class, BottleGnomes.class, GrizzlyBears.class})
class SpireSerpentTest extends BaseCardTest {

    @Test
    @DisplayName("Base 3/5 with defender and no artifacts")
    void noMetalcraftBaseStats() {
        harness.addToBattlefield(player1, new SpireSerpent());

        Permanent serpent = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(gqs.getEffectivePower(gd, serpent)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, serpent)).isEqualTo(5);
        assertThat(gqs.hasKeyword(gd, serpent, Keyword.DEFENDER)).isTrue();
    }

    @Test
    @DisplayName("Base 3/5 with two artifacts (no metalcraft)")
    void noMetalcraftWithTwoArtifacts() {
        harness.addToBattlefield(player1, new SpireSerpent());
        harness.addToBattlefield(player1, new Spellbook());
        harness.addToBattlefield(player1, new LeoninScimitar());

        Permanent serpent = findPermanent(player1, "Spire Serpent");
        assertThat(gqs.getEffectivePower(gd, serpent)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, serpent)).isEqualTo(5);
    }

    @Test
    @DisplayName("Cannot attack without metalcraft (defender)")
    void cannotAttackWithoutMetalcraft() {
        harness.addToBattlefield(player1, new SpireSerpent());
        Permanent serpent = gd.playerBattlefields.get(player1.getId()).getFirst();
        serpent.setSummoningSick(false);
        harness.addToBattlefield(player2, new GrizzlyBears());

        assertThatThrownBy(() -> declareAttackers(player1, List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
    }

    @Test
    @DisplayName("Gets +2/+2 (becomes 5/7) with exactly three artifacts")
    void metalcraftWithThreeArtifacts() {
        harness.addToBattlefield(player1, new SpireSerpent());
        harness.addToBattlefield(player1, new Spellbook());
        harness.addToBattlefield(player1, new LeoninScimitar());
        harness.addToBattlefield(player1, new BottleGnomes());

        Permanent serpent = findPermanent(player1, "Spire Serpent");
        assertThat(gqs.getEffectivePower(gd, serpent)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, serpent)).isEqualTo(7);
    }

    @Test
    @DisplayName("Can attack with metalcraft despite having defender")
    void canAttackWithMetalcraft() {
        harness.addToBattlefield(player1, new SpireSerpent());
        harness.addToBattlefield(player1, new Spellbook());
        harness.addToBattlefield(player1, new LeoninScimitar());
        harness.addToBattlefield(player1, new BottleGnomes());
        Permanent serpent = findPermanent(player1, "Spire Serpent");
        serpent.setSummoningSick(false);
        harness.addToBattlefield(player2, new GrizzlyBears());

        int serpentIndex = gd.playerBattlefields.get(player1.getId()).indexOf(serpent);
        declareAttackers(player1, List.of(serpentIndex));

        assertThat(serpent.isAttacking()).isTrue();
    }

    @Test
    @DisplayName("Loses boost when artifact count drops below three")
    void losesMetalcraftWhenArtifactRemoved() {
        harness.addToBattlefield(player1, new SpireSerpent());
        harness.addToBattlefield(player1, new Spellbook());
        harness.addToBattlefield(player1, new LeoninScimitar());
        harness.addToBattlefield(player1, new BottleGnomes());

        Permanent serpent = findPermanent(player1, "Spire Serpent");
        assertThat(gqs.getEffectivePower(gd, serpent)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, serpent)).isEqualTo(7);

        // Remove one artifact — now only 2
        gd.playerBattlefields.get(player1.getId()).removeIf(p -> p.getCard().getName().equals("Bottle Gnomes"));
        assertThat(gqs.getEffectivePower(gd, serpent)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, serpent)).isEqualTo(5);
    }

    @Test
    @DisplayName("Cannot attack after losing metalcraft")
    void cannotAttackAfterLosingMetalcraft() {
        harness.addToBattlefield(player1, new SpireSerpent());
        harness.addToBattlefield(player1, new Spellbook());
        harness.addToBattlefield(player1, new LeoninScimitar());
        harness.addToBattlefield(player1, new BottleGnomes());
        Permanent serpent = findPermanent(player1, "Spire Serpent");
        serpent.setSummoningSick(false);
        harness.addToBattlefield(player2, new GrizzlyBears());

        // Remove one artifact — lose metalcraft
        gd.playerBattlefields.get(player1.getId()).removeIf(p -> p.getCard().getName().equals("Bottle Gnomes"));

        int serpentIndex = gd.playerBattlefields.get(player1.getId()).indexOf(serpent);
        assertThatThrownBy(() -> declareAttackers(player1, List.of(serpentIndex)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
    }

    @Test
    @DisplayName("Opponent's artifacts don't count for metalcraft")
    void opponentArtifactsDontCount() {
        harness.addToBattlefield(player1, new SpireSerpent());
        harness.addToBattlefield(player2, new Spellbook());
        harness.addToBattlefield(player2, new LeoninScimitar());
        harness.addToBattlefield(player2, new BottleGnomes());

        Permanent serpent = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(gqs.getEffectivePower(gd, serpent)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, serpent)).isEqualTo(5);
    }

    @Test
    @DisplayName("Gaining the third artifact immediately enables the boost and attacking")
    void gainsMetalcraftWhenThirdArtifactEnters() {
        Permanent serpent = harness.addToBattlefieldAndReturn(player1, new SpireSerpent());
        serpent.setSummoningSick(false);
        harness.addToBattlefield(player1, new Spellbook());
        harness.addToBattlefield(player1, new LeoninScimitar());
        harness.addToBattlefield(player2, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, serpent)).isEqualTo(3);
        assertThatThrownBy(() -> declareAttackers(player1, List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");

        harness.addToBattlefield(player1, new BottleGnomes());
        assertThat(gqs.getEffectivePower(gd, serpent)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, serpent)).isEqualTo(7);
        declareAttackers(player1, List.of(0));
        assertThat(serpent.isAttacking()).isTrue();
    }

    @Test
    @DisplayName("More than three artifacts gives only one boost and preserves defender")
    void fourArtifactsDoNotStackBoostOrRemoveDefender() {
        Permanent serpent = harness.addToBattlefieldAndReturn(player1, new SpireSerpent());
        harness.addToBattlefield(player1, new Spellbook());
        harness.addToBattlefield(player1, new Spellbook());
        harness.addToBattlefield(player1, new LeoninScimitar());
        harness.addToBattlefield(player1, new BottleGnomes());

        assertThat(gqs.getEffectivePower(gd, serpent)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, serpent)).isEqualTo(7);
        assertThat(gqs.hasKeyword(gd, serpent, Keyword.DEFENDER)).isTrue();
    }

    @Test
    @DisplayName("Metalcraft does not allow attacking while summoning sick")
    void metalcraftDoesNotBypassSummoningSickness() {
        Permanent serpent = harness.addToBattlefieldAndReturn(player1, new SpireSerpent());
        serpent.setSummoningSick(true);
        harness.addToBattlefield(player1, new Spellbook());
        harness.addToBattlefield(player1, new LeoninScimitar());
        harness.addToBattlefield(player1, new BottleGnomes());

        assertThat(gqs.getEffectivePower(gd, serpent)).isEqualTo(5);
        assertThatThrownBy(() -> declareAttackers(player1, List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
    }

    @Test
    @DisplayName("Losing metalcraft after declaration reduces stats but does not remove the attacker")
    void losingMetalcraftDuringCombatDoesNotUndoAttack() {
        Permanent serpent = harness.addToBattlefieldAndReturn(player1, new SpireSerpent());
        serpent.setSummoningSick(false);
        harness.addToBattlefield(player1, new Spellbook());
        harness.addToBattlefield(player1, new LeoninScimitar());
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new BottleGnomes());
        harness.addToBattlefield(player2, new GrizzlyBears());

        declareAttackersAndPrepareBlockers(player1, List.of(0));
        assertThat(serpent.isAttacking()).isTrue();
        gd.playerBattlefields.get(player1.getId()).remove(artifact);

        assertThat(gqs.getEffectivePower(gd, serpent)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, serpent)).isEqualTo(5);
        assertThat(serpent.isAttacking()).isTrue();
    }
}

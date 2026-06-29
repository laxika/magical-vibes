package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.b.BottleGnomes;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SpireSerpentTest extends BaseCardTest {

    // ===== Without metalcraft =====

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

        Permanent serpent = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Spire Serpent"))
                .findFirst().orElseThrow();
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

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        gd.interaction.beginAttackerDeclaration(player1.getId());

        assertThatThrownBy(() -> gs.declareAttackers(gd, player1, List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
    }

    // ===== With metalcraft =====

    @Test
    @DisplayName("Gets +2/+2 (becomes 5/7) with exactly three artifacts")
    void metalcraftWithThreeArtifacts() {
        harness.addToBattlefield(player1, new SpireSerpent());
        harness.addToBattlefield(player1, new Spellbook());
        harness.addToBattlefield(player1, new LeoninScimitar());
        harness.addToBattlefield(player1, new BottleGnomes());

        Permanent serpent = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Spire Serpent"))
                .findFirst().orElseThrow();
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
        Permanent serpent = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Spire Serpent"))
                .findFirst().orElseThrow();
        serpent.setSummoningSick(false);
        harness.addToBattlefield(player2, new GrizzlyBears());

        int serpentIndex = gd.playerBattlefields.get(player1.getId()).indexOf(serpent);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        gd.interaction.beginAttackerDeclaration(player1.getId());

        gs.declareAttackers(gd, player1, List.of(serpentIndex));

        assertThat(serpent.isAttacking()).isTrue();
    }

    // ===== Metalcraft lost =====

    @Test
    @DisplayName("Loses boost when artifact count drops below three")
    void losesMetalcraftWhenArtifactRemoved() {
        harness.addToBattlefield(player1, new SpireSerpent());
        harness.addToBattlefield(player1, new Spellbook());
        harness.addToBattlefield(player1, new LeoninScimitar());
        harness.addToBattlefield(player1, new BottleGnomes());

        Permanent serpent = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Spire Serpent"))
                .findFirst().orElseThrow();
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
        Permanent serpent = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Spire Serpent"))
                .findFirst().orElseThrow();
        serpent.setSummoningSick(false);
        harness.addToBattlefield(player2, new GrizzlyBears());

        // Remove one artifact — lose metalcraft
        gd.playerBattlefields.get(player1.getId()).removeIf(p -> p.getCard().getName().equals("Bottle Gnomes"));

        int serpentIndex = gd.playerBattlefields.get(player1.getId()).indexOf(serpent);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        gd.interaction.beginAttackerDeclaration(player1.getId());

        assertThatThrownBy(() -> gs.declareAttackers(gd, player1, List.of(serpentIndex)))
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
}

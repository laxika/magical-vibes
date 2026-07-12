package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.d.Demystify;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class StealArtifactTest extends BaseCardTest {

    // ===== Casting =====

    @Test
    @DisplayName("Casting Steal Artifact targeting an artifact puts it on the stack")
    void castingPutsOnStack() {
        harness.addToBattlefield(player2, new Ornithopter());
        Permanent thopter = gd.playerBattlefields.get(player2.getId()).getFirst();
        harness.setHand(player1, List.of(new StealArtifact()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castEnchantment(player1, 0, thopter.getId());

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ENCHANTMENT_SPELL);
        assertThat(entry.getTargetId()).isEqualTo(thopter.getId());
    }

    // ===== Resolution =====

    @Test
    @DisplayName("Resolving Steal Artifact steals opponent's artifact")
    void resolvingStealsArtifact() {
        harness.addToBattlefield(player2, new Ornithopter());
        Permanent thopter = gd.playerBattlefields.get(player2.getId()).getFirst();
        harness.setHand(player1, List.of(new StealArtifact()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castEnchantment(player1, 0, thopter.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getId().equals(thopter.getId()));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getId().equals(thopter.getId()));

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Steal Artifact")
                        && p.isAttached()
                        && p.getAttachedTo().equals(thopter.getId()));

        assertThat(gd.stolenCreatures).containsEntry(thopter.getId(), player2.getId());
    }

    @Test
    @DisplayName("Steal Artifact fizzles if target artifact is no longer on the battlefield")
    void fizzlesIfTargetGone() {
        harness.addToBattlefield(player2, new Ornithopter());
        Permanent thopter = gd.playerBattlefields.get(player2.getId()).getFirst();
        harness.setHand(player1, List.of(new StealArtifact()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castEnchantment(player1, 0, thopter.getId());

        gd.playerBattlefields.get(player2.getId()).remove(thopter);

        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Steal Artifact"));
    }

    @Test
    @DisplayName("Artifact returns to owner when Steal Artifact is destroyed")
    void artifactReturnsWhenAuraDestroyed() {
        harness.addToBattlefield(player2, new Ornithopter());
        Permanent thopter = gd.playerBattlefields.get(player2.getId()).getFirst();
        harness.setHand(player1, List.of(new StealArtifact()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castEnchantment(player1, 0, thopter.getId());
        harness.passBothPriorities();

        Permanent auraPerm = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Steal Artifact"))
                .findFirst().orElseThrow();

        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Demystify()));
        harness.addMana(player2, ManaColor.WHITE, 1);

        harness.passPriority(player1);
        harness.castInstant(player2, 0, auraPerm.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getId().equals(thopter.getId()));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getId().equals(thopter.getId()));
        assertThat(gd.stolenCreatures).doesNotContainKey(thopter.getId());
    }

    // ===== Targeting restriction =====

    @Test
    @DisplayName("Cannot target a non-artifact permanent with Steal Artifact")
    void cannotTargetNonArtifact() {
        harness.addToBattlefield(player2, new Ornithopter()); // valid target so spell is playable
        harness.addToBattlefield(player2, new GrizzlyBears());
        Permanent bears = gd.playerBattlefields.get(player2.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Grizzly Bears"))
                .findFirst().orElseThrow();
        harness.setHand(player1, List.of(new StealArtifact()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, bears.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an artifact");
    }
}

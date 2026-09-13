package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.d.Disenchant;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.PhyrexianHulk;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({StealArtifact.class, Disenchant.class, GrizzlyBears.class, PhyrexianHulk.class,
        Spellbook.class})
class StealArtifactTest extends BaseCardTest {
    @Test
    @DisplayName("Casting Steal Artifact targeting an artifact puts it on the stack")
    void castingPutsOnStack() {
        Permanent hulk = harness.addToBattlefieldAndReturn(player2, new PhyrexianHulk());
        harness.setHand(player1, List.of(new StealArtifact()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castEnchantment(player1, 0, hulk.getId());

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ENCHANTMENT_SPELL);
        assertThat(entry.getTargetId()).isEqualTo(hulk.getId());
    }
    @Test
    @DisplayName("Resolving Steal Artifact steals opponent's artifact")
    void resolvingStealsArtifact() {
        Permanent hulk = harness.addToBattlefieldAndReturn(player2, new PhyrexianHulk());
        StealArtifact stealArtifact = new StealArtifact();
        harness.setHand(player1, List.of(stealArtifact));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castEnchantment(player1, 0, hulk.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getId().equals(hulk.getId()));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getId().equals(hulk.getId()));

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getId().equals(stealArtifact.getId())
                        && p.isAttached()
                        && p.getAttachedTo().equals(hulk.getId()));

        assertThat(gd.stolenCreatures).containsEntry(hulk.getId(), player2.getId());
    }

    @Test
    @DisplayName("Resolving Steal Artifact steals an opponent's noncreature artifact")
    void resolvingStealsNoncreatureArtifact() {
        Permanent spellbook = harness.addToBattlefieldAndReturn(player2, new Spellbook());
        harness.setHand(player1, List.of(new StealArtifact()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castEnchantment(player1, 0, spellbook.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getId().equals(spellbook.getId()));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getId().equals(spellbook.getId()));
    }

    @Test
    @DisplayName("Can target an artifact you control")
    void canTargetOwnArtifact() {
        Permanent spellbook = harness.addToBattlefieldAndReturn(player1, new Spellbook());
        StealArtifact stealArtifact = new StealArtifact();
        harness.setHand(player1, List.of(stealArtifact));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castEnchantment(player1, 0, spellbook.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getId().equals(spellbook.getId()));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getId().equals(stealArtifact.getId())
                        && p.isAttached()
                        && p.getAttachedTo().equals(spellbook.getId()));
    }

    @Test
    @DisplayName("Steal Artifact fizzles if target artifact is no longer on the battlefield")
    void fizzlesIfTargetGone() {
        Permanent hulk = harness.addToBattlefieldAndReturn(player2, new PhyrexianHulk());
        StealArtifact stealArtifact = new StealArtifact();
        harness.setHand(player1, List.of(stealArtifact));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castEnchantment(player1, 0, hulk.getId());

        gd.playerBattlefields.get(player2.getId()).remove(hulk);

        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card.getId().equals(stealArtifact.getId()));
    }

    @Test
    @DisplayName("Artifact returns to owner when Steal Artifact is destroyed")
    void artifactReturnsWhenAuraDestroyed() {
        Permanent hulk = harness.addToBattlefieldAndReturn(player2, new PhyrexianHulk());
        StealArtifact stealArtifact = new StealArtifact();
        harness.setHand(player1, List.of(stealArtifact));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castEnchantment(player1, 0, hulk.getId());
        harness.passBothPriorities();

        Permanent auraPerm = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getId().equals(stealArtifact.getId()))
                .findFirst().orElseThrow();

        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Disenchant()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, auraPerm.getId());

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getId().equals(hulk.getId()));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getId().equals(hulk.getId()));
        assertThat(gd.stolenCreatures).doesNotContainKey(hulk.getId());
    }
    @Test
    @DisplayName("Cannot target a non-artifact permanent with Steal Artifact")
    void cannotTargetNonArtifact() {
        harness.addToBattlefieldAndReturn(player2, new PhyrexianHulk()); // valid target so spell is playable
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new StealArtifact()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, bears.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an artifact");
    }
}

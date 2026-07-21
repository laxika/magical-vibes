package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EtherswornCanonistTest extends BaseCardTest {

    @Test
    @DisplayName("First nonartifact spell is still castable")
    void allowsFirstNonartifactSpell() {
        harness.addToBattlefield(player1, new EtherswornCanonist());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
    }

    @Test
    @DisplayName("Second nonartifact spell is not playable")
    void blocksSecondNonartifactSpell() {
        harness.addToBattlefield(player1, new EtherswornCanonist());
        harness.setHand(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Artifact spell is still castable after a nonartifact spell")
    void allowsArtifactSpellAfterNonartifact() {
        harness.addToBattlefield(player1, new EtherswornCanonist());
        harness.setHand(player1, List.of(new GrizzlyBears(), new Ornithopter()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        // Ornithopter is an artifact creature; casting it is unrestricted.
        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ARTIFACT_SPELL);
    }

    @Test
    @DisplayName("A nonartifact spell is castable after an artifact spell")
    void allowsNonartifactSpellAfterArtifact() {
        harness.addToBattlefield(player1, new EtherswornCanonist());
        harness.setHand(player1, List.of(new Ornithopter(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        // Artifact spell doesn't count as a nonartifact spell cast this turn.
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
    }

    @Test
    @DisplayName("Restriction affects both players")
    void affectsBothPlayers() {
        harness.addToBattlefield(player1, new EtherswornCanonist());
        harness.setHand(player2, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.addMana(player2, ManaColor.GREEN, 4);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castCreature(player2, 0);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.castCreature(player2, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Counts nonartifact spells cast before Canonist entered")
    void countsSpellsCastBeforeCanonistEntered() {
        // Ruling: takes into account spells cast earlier in the turn before it ETB'd.
        gd.recordSpellCast(player1.getId(), new GrizzlyBears());
        harness.addToBattlefield(player1, new EtherswornCanonist());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("One player's nonartifact spell does not restrict the other")
    void restrictionIsPerPlayer() {
        harness.addToBattlefield(player1, new EtherswornCanonist());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player2, ManaColor.GREEN, 2);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.forceActivePlayer(player2);
        harness.clearPriorityPassed();
        harness.castCreature(player2, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
    }
}

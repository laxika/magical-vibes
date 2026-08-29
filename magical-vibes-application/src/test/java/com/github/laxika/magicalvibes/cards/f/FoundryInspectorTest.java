package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.j.JhoirasFamiliar;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FoundryInspectorTest extends BaseCardTest {

    @Test
    @DisplayName("Artifact spells you cast cost {1} less")
    void artifactSpellsCostOneLess() {
        harness.addToBattlefield(player1, new FoundryInspector());
        harness.setHand(player1, List.of(new JhoirasFamiliar()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castArtifact(player1, 0);

        assertThat(gd.stack).anyMatch(entry -> entry.getCard().getName().equals("Jhoira's Familiar"));
    }

    @Test
    @DisplayName("Nonartifact spells are not reduced")
    void nonartifactSpellsNotReduced() {
        harness.addToBattlefield(player1, new FoundryInspector());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The reduction only applies to the controller's spells")
    void opponentArtifactSpellsNotReduced() {
        harness.addToBattlefield(player1, new FoundryInspector());
        harness.setHand(player2, List.of(new JhoirasFamiliar()));
        harness.addMana(player2, ManaColor.COLORLESS, 3);
        harness.forceActivePlayer(player2);

        assertThatThrownBy(() -> harness.castArtifact(player2, 0))
                .isInstanceOf(IllegalStateException.class);
    }
}

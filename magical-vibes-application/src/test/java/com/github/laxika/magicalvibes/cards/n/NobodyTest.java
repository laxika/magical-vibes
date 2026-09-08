package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Nobody.class, Spellbook.class})
class NobodyTest extends BaseCardTest {

    @Test
    @DisplayName("ETB returns another artifact you control and then scries 1")
    void returnsAnotherArtifactAndScries() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new Spellbook());
        harness.setLibrary(player1, List.of(new Spellbook()));
        castNobody();

        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, artifact.getId());
        harness.passBothPriorities();

        harness.assertInHand(player1, "Spellbook");
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.Scry.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards()).hasSize(1);
    }

    @Test
    @DisplayName("ETB still scries 1 when no artifact is returned")
    void scriesWithoutChoosingAnArtifact() {
        harness.setLibrary(player1, List.of(new Spellbook()));
        castNobody();

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.Scry.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards()).hasSize(1);
    }

    @Test
    @DisplayName("ETB cannot target an opponent's artifact")
    void targetMustBeAnotherArtifactYouControl() {
        Permanent opponentArtifact = harness.addToBattlefieldAndReturn(player2, new Spellbook());
        harness.setHand(player1, List.of(new Nobody()));
        addNobodyMana();

        assertThatThrownBy(() -> harness.castCreature(player1, 0, 0, opponentArtifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("another artifact you control");
    }

    private void castNobody() {
        harness.setHand(player1, List.of(new Nobody()));
        addNobodyMana();
        harness.castCreature(player1, 0);
    }

    private void addNobodyMana() {
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }
}

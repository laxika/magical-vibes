package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.i.Island;
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

@CardUsed({Nobody.class, Island.class})
class NobodyTest extends BaseCardTest {

    @Test
    @DisplayName("ETB returns another artifact you control and then scries 1")
    void returnsAnotherArtifactAndScries() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new Nobody());
        harness.setLibrary(player1, List.of(new Nobody()));
        castNobody();

        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, artifact.getId());
        harness.passBothPriorities();

        harness.assertInHand(player1, "Nobody");
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.Scry.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards()).hasSize(1);
    }

    @Test
    @DisplayName("ETB still scries 1 when no artifact is returned")
    void scriesWithoutChoosingAnArtifact() {
        harness.setLibrary(player1, List.of(new Nobody()));
        castNobody();

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.Scry.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards()).hasSize(1);
    }

    @Test
    @DisplayName("ETB cannot target an opponent's artifact")
    void targetMustBeAnotherArtifactYouControl() {
        Permanent opponentArtifact = harness.addToBattlefieldAndReturn(player2, new Nobody());
        harness.setHand(player1, List.of(new Nobody()));
        addNobodyMana();

        assertThatThrownBy(() -> harness.castCreature(player1, 0, 0, opponentArtifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("another artifact you control");
    }

    @Test
    @DisplayName("ETB may decline returning an available artifact and still scry")
    void declinesAvailableArtifact() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new Nobody());
        harness.setLibrary(player1, List.of(new Nobody()));
        castNobody();

        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(artifact);
        harness.assertNotInHand(player1, "Nobody");
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.Scry.class);
    }

    @Test
    @DisplayName("ETB cannot target the Nobody that just entered")
    void cannotReturnItself() {
        harness.addToBattlefield(player1, new Nobody());
        castNobody();
        harness.passBothPriorities();
        Permanent entering = gd.playerBattlefields.get(player1.getId()).getLast();

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, entering.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("ETB does not scry if its only chosen target leaves before resolution")
    void doesNotScryWhenOnlyTargetLeaves() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new Nobody());
        Nobody topCard = new Nobody();
        harness.setLibrary(player1, List.of(topCard));
        castNobody();

        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, artifact.getId());
        gd.playerBattlefields.get(player1.getId()).remove(artifact);
        gd.playerGraveyards.get(player1.getId()).add(artifact.getCard());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);
        harness.assertNotInHand(player1, "Nobody");
    }

    @Test
    @DisplayName("ETB cannot target a nonartifact permanent you control")
    void cannotReturnNonartifact() {
        Permanent island = harness.addToBattlefieldAndReturn(player1, new Island());
        harness.addToBattlefield(player1, new Nobody());
        castNobody();
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, island.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("ETB resolves with no target and an empty library")
    void resolvesWithEmptyLibrary() {
        harness.setLibrary(player1, List.of());
        castNobody();

        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Nobody");
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
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

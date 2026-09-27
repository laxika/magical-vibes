package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.a.AetherSpellbomb;
import com.github.laxika.magicalvibes.cards.a.Annul;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ThirstForKnowledge.class, AetherSpellbomb.class, Annul.class, Forest.class, Island.class,
        Mountain.class})
class ThirstForKnowledgeTest extends BaseCardTest {

    @Test
    @DisplayName("Draws three cards and allows one artifact discard")
    void drawsThreeAndCanDiscardOneArtifact() {
        ThirstForKnowledge spell = new ThirstForKnowledge();
        Annul nonArtifact = new Annul();
        AetherSpellbomb artifact = new AetherSpellbomb();
        harness.setLibrary(player1, List.of(new Forest(), new Island(), new Mountain()));
        harness.setHand(player1, List.of(spell, nonArtifact, artifact));
        addMana();

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(5);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();

        harness.handleCardChosen(player1, 1);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(artifact);
        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(4);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .contains(spell, artifact)
                .doesNotContain(nonArtifact);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("May choose a second card after discarding an artifact")
    void mayChooseTwoCardsIncludingAnArtifact() {
        ThirstForKnowledge spell = new ThirstForKnowledge();
        AetherSpellbomb firstArtifact = new AetherSpellbomb();
        AetherSpellbomb secondArtifact = new AetherSpellbomb();
        harness.setLibrary(player1, List.of(new Forest(), new Island(), new Mountain()));
        harness.setHand(player1, List.of(spell, firstArtifact, secondArtifact));
        addMana();

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        harness.handleCardChosen(player1, 0);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(spell, firstArtifact, secondArtifact);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Requires two discards when no artifact is discarded")
    void requiresTwoDiscardsWithoutArtifact() {
        ThirstForKnowledge spell = new ThirstForKnowledge();
        Annul firstNonArtifact = new Annul();
        Annul secondNonArtifact = new Annul();
        harness.setLibrary(player1, List.of(new Forest(), new Island(), new Mountain()));
        harness.setHand(player1, List.of(spell, firstNonArtifact, secondNonArtifact));
        addMana();

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        harness.handleCardChosen(player1, 0);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(spell, firstNonArtifact, secondNonArtifact);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Causes a loss when drawing from an empty library")
    void losesWhenDrawingFromEmptyLibrary() {
        ThirstForKnowledge spell = new ThirstForKnowledge();
        harness.setLibrary(player1, List.of());
        harness.setHand(player1, List.of(spell));
        addMana();

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(spell);
        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
        assertThat(gd.stack).isEmpty();
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }
}

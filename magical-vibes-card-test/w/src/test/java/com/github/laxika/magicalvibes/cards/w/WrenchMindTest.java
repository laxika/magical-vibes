package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.c.CopperMyr;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WrenchMind.class, CopperMyr.class, Forest.class})
class WrenchMindTest extends BaseCardTest {

    @Test
    @DisplayName("Target player may discard one artifact instead of two cards")
    void artifactMakesSecondDiscardOptional() {
        CopperMyr artifact = new CopperMyr();
        Forest nonArtifact = new Forest();
        harness.setHand(player2, List.of(artifact, nonArtifact));
        harness.setHand(player1, List.of(new WrenchMind()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player2, 0);
        harness.handleCardChosen(player2, -1);

        assertThat(gd.playerHands.get(player2.getId())).containsExactly(nonArtifact);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(artifact);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Target player must discard two cards when no artifact is discarded")
    void requiresTwoDiscardsWithoutArtifact() {
        Forest first = new Forest();
        Forest second = new Forest();
        harness.setHand(player2, List.of(first, second));
        harness.setHand(player1, List.of(new WrenchMind()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        harness.handleCardChosen(player2, 0);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(first, second);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Target player must discard a second card when the artifact is discarded second")
    void artifactDiscardedSecondStopsFurtherDiscards() {
        Forest nonArtifact = new Forest();
        CopperMyr artifact = new CopperMyr();
        harness.setHand(player2, List.of(nonArtifact, artifact));
        harness.setHand(player1, List.of(new WrenchMind()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        harness.handleCardChosen(player2, 0);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(nonArtifact, artifact);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void mayDiscardTwoArtifactsInsteadOfStoppingAfterOne() {
        CopperMyr first = new CopperMyr();
        CopperMyr second = new CopperMyr();
        Forest retained = new Forest();
        harness.setHand(player2, List.of(first, second, retained));
        harness.setHand(player1, List.of(new WrenchMind()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castAndResolveSorcery(player1, 0, player2.getId());
        harness.handleCardChosen(player2, 0);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).containsExactly(retained);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(first, second);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotStopAfterDiscardingANonartifact() {
        Forest first = new Forest();
        Forest second = new Forest();
        CopperMyr retained = new CopperMyr();
        harness.setHand(player2, List.of(first, second, retained));
        harness.setHand(player1, List.of(new WrenchMind()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castAndResolveSorcery(player1, 0, player2.getId());
        harness.handleCardChosen(player2, 0);
        harness.handleCardChosen(player2, -1);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(second, retained);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).containsExactly(retained);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(first, second);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void discardsOnlyAvailableCardWhenHandHasOneNonartifact() {
        Forest onlyCard = new Forest();
        harness.setHand(player2, List.of(onlyCard));
        harness.setHand(player1, List.of(new WrenchMind()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castAndResolveSorcery(player1, 0, player2.getId());
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(onlyCard);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void resolvesWithoutDiscardChoiceWhenTargetHandIsEmpty() {
        harness.setHand(player2, List.of());
        harness.setHand(player1, List.of(new WrenchMind()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void mayTargetSelfAndDiscardOneArtifact() {
        WrenchMind spell = new WrenchMind();
        CopperMyr artifact = new CopperMyr();
        Forest retained = new Forest();
        harness.setHand(player1, List.of(spell, artifact, retained));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castAndResolveSorcery(player1, 0, player1.getId());
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(retained);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactlyInAnyOrder(spell, artifact);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }
}

package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Retrieve.class, GrizzlyBears.class, LeoninScimitar.class, Shock.class})
class RetrieveTest extends BaseCardTest {

    @Test
    void returnsUpToOneCreatureAndNoncreaturePermanentToHand() {
        Card creature = new GrizzlyBears();
        Card artifact = new LeoninScimitar();
        Card instant = new Shock();
        Retrieve spell = new Retrieve();
        harness.setGraveyard(player1, List.of(creature, artifact, instant));
        harness.setHand(player1, List.of(spell));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castSorcery(player1, 0, 0);

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.maxCount()).isEqualTo(2);
        assertThat(choice.validCardIds()).containsExactlyInAnyOrder(creature.getId(), artifact.getId());

        harness.handleMultipleCardsChosen(player1, List.of(creature.getId(), artifact.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId()))
                .containsExactlyInAnyOrder(creature, artifact);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(instant);
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(spell);
    }

    @Test
    void bothReturnsAreOptional() {
        Card creature = new GrizzlyBears();
        Card artifact = new LeoninScimitar();
        Retrieve spell = new Retrieve();
        harness.setGraveyard(player1, List.of(creature, artifact));
        harness.setHand(player1, List.of(spell));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castSorcery(player1, 0, 0);
        harness.handleMultipleCardsChosen(player1, List.of());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(creature, artifact);
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(spell);
    }

    @Test
    void returnsOnlyTheChosenCreature() {
        Card creature = new GrizzlyBears();
        Card artifact = new LeoninScimitar();
        Retrieve spell = new Retrieve();
        harness.setGraveyard(player1, List.of(creature, artifact));
        harness.setHand(player1, List.of(spell));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castSorcery(player1, 0);
        harness.handleMultipleCardsChosen(player1, List.of(creature.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(creature);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(artifact);
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(spell);
    }

    @Test
    void returnsOnlyTheChosenNoncreaturePermanent() {
        Card creature = new GrizzlyBears();
        Card artifact = new LeoninScimitar();
        Retrieve spell = new Retrieve();
        harness.setGraveyard(player1, List.of(creature, artifact));
        harness.setHand(player1, List.of(spell));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castSorcery(player1, 0);
        harness.handleMultipleCardsChosen(player1, List.of(artifact.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(artifact);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(creature);
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(spell);
    }

    @Test
    void cannotChooseTwoCreaturesOrTwoNoncreaturePermanents() {
        Card creature = new GrizzlyBears();
        Card secondCreature = new GrizzlyBears();
        Card artifact = new LeoninScimitar();
        Card secondArtifact = new LeoninScimitar();
        Retrieve spell = new Retrieve();
        harness.setGraveyard(player1, List.of(creature, secondCreature, artifact, secondArtifact));
        harness.setHand(player1, List.of(spell));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castSorcery(player1, 0);

        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1,
                List.of(creature.getId(), secondCreature.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1,
                List.of(artifact.getId(), secondArtifact.getId())))
                .isInstanceOf(IllegalStateException.class);

        harness.handleMultipleCardsChosen(player1, List.of(creature.getId(), artifact.getId()));
        harness.passBothPriorities();
        assertThat(gd.playerHands.get(player1.getId())).containsExactlyInAnyOrder(creature, artifact);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .containsExactly(secondCreature, secondArtifact);
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(spell);
    }

    @Test
    void exilesItselfWhenThereAreNoLegalTargets() {
        Card instant = new Shock();
        Card opposingCreature = new GrizzlyBears();
        Retrieve spell = new Retrieve();
        harness.setGraveyard(player1, List.of(instant));
        harness.setGraveyard(player2, List.of(opposingCreature));
        harness.setHand(player1, List.of(spell));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castSorcery(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(instant);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(opposingCreature);
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(spell);
    }
}

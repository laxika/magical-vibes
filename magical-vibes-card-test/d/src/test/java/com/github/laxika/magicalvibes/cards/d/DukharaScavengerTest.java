package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.p.PrakhataClubSecurity;
import com.github.laxika.magicalvibes.cards.p.PropheticPrism;
import com.github.laxika.magicalvibes.cards.t.TidyConclusion;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DukharaScavenger.class, TidyConclusion.class, PropheticPrism.class, PrakhataClubSecurity.class})
class DukharaScavengerTest extends BaseCardTest {

    @Test
    void etbPutsArtifactOrCreatureOnTopOfLibrary() {
        TidyConclusion instant = new TidyConclusion();
        PropheticPrism artifact = new PropheticPrism();
        PrakhataClubSecurity creature = new PrakhataClubSecurity();
        TidyConclusion libraryCard = new TidyConclusion();
        harness.setGraveyard(player1, List.of(instant, artifact, creature));
        harness.setLibrary(player1, List.of(libraryCard));
        castScavenger();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactlyInAnyOrder(artifact.getId(), creature.getId());
        harness.handleMultipleCardsChosen(player1, List.of(artifact.getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(artifact, libraryCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(instant, creature);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void etbCanBeDeclined() {
        PrakhataClubSecurity creature = new PrakhataClubSecurity();
        harness.setGraveyard(player1, List.of(creature));
        harness.setLibrary(player1, List.of());
        castScavenger();
        harness.handleMultipleCardsChosen(player1, List.of(creature.getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(creature);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void etbDoesNotOfferOtherCards() {
        TidyConclusion instant = new TidyConclusion();
        harness.setGraveyard(player1, List.of(instant));
        harness.setLibrary(player1, List.of());
        castScavenger();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(instant);
    }

    @Test
    void etbCanPutCreatureOnTopOfNonemptyLibrary() {
        PrakhataClubSecurity creature = new PrakhataClubSecurity();
        PropheticPrism libraryCard = new PropheticPrism();
        harness.setGraveyard(player1, List.of(creature));
        harness.setLibrary(player1, List.of(libraryCard));
        castScavenger();
        harness.handleMultipleCardsChosen(player1, List.of(creature.getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(creature, libraryCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void etbCannotTargetOpponentsGraveyard() {
        PropheticPrism artifact = new PropheticPrism();
        PrakhataClubSecurity creature = new PrakhataClubSecurity();
        harness.setGraveyard(player1, List.of());
        harness.setGraveyard(player2, List.of(artifact, creature));
        castScavenger();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(artifact, creature);
    }

    @Test
    void emptyGraveyardLeavesNoTriggerOnStack() {
        harness.setGraveyard(player1, List.of());
        castScavenger();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void targetLeavingGraveyardDoesNotAllowChoosingAnotherCard() {
        PropheticPrism target = new PropheticPrism();
        PrakhataClubSecurity other = new PrakhataClubSecurity();
        harness.setGraveyard(player1, List.of(target, other));
        harness.setLibrary(player1, List.of());
        castScavenger();
        harness.handleMultipleCardsChosen(player1, List.of(target.getId()));
        harness.setGraveyard(player1, List.of(other));
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(other);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    private void castScavenger() {
        harness.setHand(player1, List.of(new DukharaScavenger()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
    }
}

package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SarinthSteelseeker.class, Forest.class, GrizzlyBears.class, Ornithopter.class})
class SarinthSteelseekerTest extends BaseCardTest {

    @Test
    @DisplayName("An artifact entering under your control lets you reveal a land into your hand")
    void artifactEntryCanPutLandIntoHand() {
        Card land = new Forest();
        gd.playerDecks.get(player1.getId()).addFirst(land);

        triggerWithArtifact();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).contains(land);
        assertThat(gd.playerDecks.get(player1.getId())).doesNotContain(land);
    }

    @Test
    @DisplayName("Declining the land reveal offers to put it into your graveyard")
    void decliningLandRevealCanPutLandIntoGraveyard() {
        Card land = new Forest();
        gd.playerDecks.get(player1.getId()).addFirst(land);

        triggerWithArtifact();

        harness.handleMayAbilityChosen(player1, false);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(land);
        assertThat(gd.playerDecks.get(player1.getId())).doesNotContain(land);
    }

    @Test
    @DisplayName("A nonland top card may be put into your graveyard")
    void nonlandCanBePutIntoGraveyard() {
        Card nonland = new GrizzlyBears();
        gd.playerDecks.get(player1.getId()).addFirst(nonland);

        triggerWithArtifact();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(nonland);
        assertThat(gd.playerDecks.get(player1.getId())).doesNotContain(nonland);
    }

    @Test
    @DisplayName("An artifact entering under an opponent's control does not trigger")
    void opponentArtifactDoesNotTrigger() {
        harness.addToBattlefield(player1, new SarinthSteelseeker());
        harness.forceActivePlayer(player2);

        harness.castFromHand(player2, new Ornithopter(), "{0}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Declining both land choices leaves the land on top")
    void decliningBothChoicesLeavesLandOnTop() {
        Card land = new Forest();
        harness.setLibrary(player1, List.of(land));

        triggerWithArtifact();
        harness.handleMayAbilityChosen(player1, false);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(land);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(land);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(land);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Declining the nonland graveyard choice leaves it on top")
    void decliningNonlandGraveyardChoiceLeavesCardOnTop() {
        Card nonland = new GrizzlyBears();
        harness.setLibrary(player1, List.of(nonland));

        triggerWithArtifact();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(nonland);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(nonland);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(nonland);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("An empty library resolves without a choice")
    void emptyLibraryDoesNothing() {
        harness.setLibrary(player1, List.of());

        triggerWithArtifact();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A nonartifact entering under your control does not trigger")
    void nonartifactDoesNotTrigger() {
        Card land = new Forest();
        harness.setLibrary(player1, List.of(land));
        harness.addToBattlefield(player1, new SarinthSteelseeker());

        harness.castFromHand(player1, new GrizzlyBears(), "{1}{G}");
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(land);
    }

    private void triggerWithArtifact() {
        harness.addToBattlefield(player1, new SarinthSteelseeker());

        harness.castFromHand(player1, new Ornithopter(), "{0}");
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}

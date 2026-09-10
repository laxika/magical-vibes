package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TreasureHunter.class, Spellbook.class})
class TreasureHunterTest extends BaseCardTest {

    @Test
    @DisplayName("Accepting the ETB ability returns the chosen artifact to hand")
    void acceptingAbilityReturnsChosenArtifact() {
        Spellbook spellbook = new Spellbook();
        harness.setGraveyard(player1, List.of(spellbook));

        harness.enterBattlefieldAndReturn(player1, new TreasureHunter());

        PendingInteraction.MultiGraveyardChoice targetChoice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(targetChoice).isNotNull();
        assertThat(targetChoice.validCardIds()).containsExactly(spellbook.getId());

        harness.handleMultipleCardsChosen(player1, List.of(spellbook.getId()));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertInHand(player1, "Spellbook");
        harness.assertNotInGraveyard(player1, "Spellbook");
    }

    @Test
    @DisplayName("Declining the ETB ability leaves the targeted artifact in the graveyard")
    void decliningAbilityLeavesArtifactInGraveyard() {
        Spellbook spellbook = new Spellbook();
        harness.setGraveyard(player1, List.of(spellbook));

        harness.enterBattlefieldAndReturn(player1, new TreasureHunter());
        harness.handleMultipleCardsChosen(player1, List.of(spellbook.getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertInGraveyard(player1, "Spellbook");
        harness.assertNotInHand(player1, "Spellbook");
    }

    @Test
    @DisplayName("The ETB ability offers only artifact cards in the controller's graveyard")
    void offersOnlyOwnArtifactCards() {
        TreasureHunter nonArtifact = new TreasureHunter();
        Spellbook ownArtifact = new Spellbook();
        Spellbook opponentArtifact = new Spellbook();
        harness.setGraveyard(player1, List.of(nonArtifact, ownArtifact));
        harness.setGraveyard(player2, List.of(opponentArtifact));

        harness.enterBattlefieldAndReturn(player1, new TreasureHunter());

        PendingInteraction.MultiGraveyardChoice targetChoice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(targetChoice).isNotNull();
        assertThat(targetChoice.validCardIds()).containsExactly(ownArtifact.getId());

        harness.handleMultipleCardsChosen(player1, List.of(ownArtifact.getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(card -> card.getId().equals(ownArtifact.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .containsExactly(nonArtifact);
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .containsExactly(opponentArtifact);
    }

    @Test
    @DisplayName("The ETB ability is not put on the stack without a legal artifact target")
    void noLegalArtifactTargetSkipsAbility() {
        TreasureHunter nonArtifact = new TreasureHunter();
        harness.setGraveyard(player1, List.of(nonArtifact));

        harness.enterBattlefieldAndReturn(player1, new TreasureHunter());

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Treasure Hunter");
    }
}

package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AetherFlash;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SylvanHierophant.class, AetherFlash.class, StripedBears.class})
class SylvanHierophantTest extends BaseCardTest {

    /** Enters Sylvan Hierophant under Aether Flash and resolves the damage trigger. */
    private Card enterAndKillHierophant() {
        harness.addToBattlefield(player1, new AetherFlash());
        Card hierophant = new SylvanHierophant();
        harness.enterBattlefieldAndReturn(player1, hierophant);
        harness.passBothPriorities();
        return hierophant;
    }

    @Test
    @DisplayName("On death it exiles itself and returns the targeted creature card to hand")
    void deathExilesSelfAndReturnsTargetedCreature() {
        Card bears = new StripedBears();
        harness.setGraveyard(player1, List.of(bears));

        Card hierophant = enterAndKillHierophant();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(bears.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(c -> c.getId().equals(bears.getId()));
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getId().equals(hierophant.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(c -> c.getId().equals(hierophant.getId()));
    }

    @Test
    @DisplayName("\"Another\" excludes the Hierophant itself from the legal targets")
    void selfIsNotALegalTarget() {
        Card bears = new StripedBears();
        harness.setGraveyard(player1, List.of(bears));
        Card hierophant = enterAndKillHierophant();

        var choice = gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).contains(bears.getId());
        assertThat(choice.validCardIds()).doesNotContain(hierophant.getId());
    }

    @Test
    @DisplayName("Only the controller's own graveyard is searched")
    void opponentGraveyardCardNotTargetable() {
        Card opponentBears = new StripedBears();
        harness.setGraveyard(player2, List.of(opponentBears));

        enterAndKillHierophant();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
    }

    @Test
    @DisplayName("With no other creature card in the graveyard the trigger is skipped and it is not exiled")
    void noLegalTargetLeavesHierophantInGraveyard() {
        Card hierophant = enterAndKillHierophant();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getId().equals(hierophant.getId()));
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .noneMatch(c -> c.getId().equals(hierophant.getId()));
    }

    @Test
    @DisplayName("If the chosen card leaves the graveyard, the trigger does not exile the Hierophant")
    void illegalTargetDoesNotExileHierophant() {
        Card target = new StripedBears();
        harness.setGraveyard(player1, List.of(target));
        Card hierophant = enterAndKillHierophant();

        harness.handleMultipleCardsChosen(player1, List.of(target.getId()));
        harness.setGraveyard(player1, List.of(hierophant));
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getId().equals(hierophant.getId()));
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .noneMatch(c -> c.getId().equals(hierophant.getId()));
        assertThat(gd.playerHands.get(player1.getId()))
                .noneMatch(c -> c.getId().equals(target.getId()));
    }

    @Test
    @DisplayName("If the chosen card moves to the opponent's graveyard, it is no longer a legal target")
    void targetInOpponentsGraveyardIsIllegal() {
        Card target = new StripedBears();
        harness.setGraveyard(player1, List.of(target));
        Card hierophant = enterAndKillHierophant();

        harness.handleMultipleCardsChosen(player1, List.of(target.getId()));
        harness.setGraveyard(player1, List.of(hierophant));
        harness.setGraveyard(player2, List.of(target));
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getId().equals(hierophant.getId()));
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .noneMatch(c -> c.getId().equals(hierophant.getId()));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getId().equals(target.getId()));
    }
}

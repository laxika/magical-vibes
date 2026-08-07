package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.w.WrathOfGod;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SylvanHierophantTest extends BaseCardTest {

    /** Wraths the board so Sylvan Hierophant dies, firing its ON_DEATH trigger. */
    private void wrathToKillHierophant() {
        harness.setHand(player1, List.of(new WrathOfGod()));
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.getGameService().playCard(gd, player1, 0, 0, null, null);
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("On death it exiles itself and returns the targeted creature card to hand")
    void deathExilesSelfAndReturnsTargetedCreature() {
        Card hierophant = new SylvanHierophant();
        harness.addToBattlefield(player1, hierophant);
        Card bears = new GrizzlyBears();
        harness.setGraveyard(player1, new ArrayList<>(List.of(bears)));

        wrathToKillHierophant();

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
        Card hierophant = new SylvanHierophant();
        harness.addToBattlefield(player1, hierophant);
        Card bears = new GrizzlyBears();
        harness.setGraveyard(player1, new ArrayList<>(List.of(bears)));

        wrathToKillHierophant();

        var choice = gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).contains(bears.getId());
        assertThat(choice.validCardIds()).doesNotContain(hierophant.getId());
    }

    @Test
    @DisplayName("Only the controller's own graveyard is searched")
    void opponentGraveyardCardNotTargetable() {
        harness.addToBattlefield(player1, new SylvanHierophant());
        Card opponentBears = new GrizzlyBears();
        harness.setGraveyard(player2, new ArrayList<>(List.of(opponentBears)));

        wrathToKillHierophant();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
    }

    @Test
    @DisplayName("With no other creature card in the graveyard the trigger is skipped and it is not exiled")
    void noLegalTargetLeavesHierophantInGraveyard() {
        Card hierophant = new SylvanHierophant();
        harness.addToBattlefield(player1, hierophant);

        wrathToKillHierophant();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getId().equals(hierophant.getId()));
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .noneMatch(c -> c.getId().equals(hierophant.getId()));
    }
}

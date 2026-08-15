package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GoldMyr;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class OvalchaseDaredevilTest extends BaseCardTest {

    private void prepareMain(Player active) {
        harness.forceActivePlayer(active);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

    @Test
    @DisplayName("Accepting the artifact trigger returns Ovalchase Daredevil from graveyard to hand")
    void artifactReturnsFromGraveyardOnAccept() {
        OvalchaseDaredevil daredevil = new OvalchaseDaredevil();
        harness.setGraveyard(player1, List.of(daredevil));
        prepareMain(player1);

        harness.setHand(player1, List.of(new GoldMyr()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0);
        int handBefore = gd.playerHands.get(player1.getId()).size();

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
        assertThat(gd.playerHands.get(player1.getId())).contains(daredevil);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(daredevil);
    }

    @Test
    @DisplayName("Declining the artifact trigger keeps Ovalchase Daredevil in the graveyard")
    void artifactDeclineKeepsDaredevilInGraveyard() {
        OvalchaseDaredevil daredevil = new OvalchaseDaredevil();
        harness.setGraveyard(player1, List.of(daredevil));
        prepareMain(player1);

        harness.setHand(player1, List.of(new GoldMyr()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0);

        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(daredevil);
    }

    @Test
    @DisplayName("A nonartifact permanent entering does not trigger")
    void nonartifactPermanentDoesNotTrigger() {
        harness.setGraveyard(player1, List.of(new OvalchaseDaredevil()));
        prepareMain(player1);

        harness.setHand(player1, List.of(new Forest()));
        harness.castCreature(player1, 0);

        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("An artifact an opponent controls entering does not trigger")
    void opponentArtifactDoesNotTrigger() {
        OvalchaseDaredevil daredevil = new OvalchaseDaredevil();
        harness.setGraveyard(player1, List.of(daredevil));
        prepareMain(player2);

        harness.setHand(player2, List.of(new GoldMyr()));
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.castCreature(player2, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
    }
}

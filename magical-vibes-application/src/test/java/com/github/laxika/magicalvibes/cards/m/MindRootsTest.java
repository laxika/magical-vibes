package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.p.Peek;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MindRootsTest extends BaseCardTest {

    @Test
    void putsOneOfTheDiscardedLandsOntoBattlefieldTappedUnderCasterControl() {
        harness.setHand(player2, new ArrayList<>(List.of(new Forest(), new Mountain(), new GrizzlyBears())));
        harness.setHand(player1, List.of(new MindRoots()));
        addMindRootsMana();

        castAndResolveToDiscardChoice();
        harness.handleCardChosen(player2, 0);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.GraveyardChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.GraveyardChoice.class).playerId())
                .isEqualTo(player1.getId());

        harness.handleGraveyardCardChosen(player1, 0);

        assertThat(findPermanent(player1, "Forest").isTapped()).isTrue();
        harness.assertNotOnBattlefield(player2, "Forest");
        harness.assertInGraveyard(player2, "Mountain");
    }

    @Test
    void cannotChooseALandThatWasAlreadyInTheGraveyard() {
        harness.setGraveyard(player2, List.of(new Forest()));
        harness.setHand(player2, new ArrayList<>(List.of(new GrizzlyBears(), new Peek())));
        harness.setHand(player1, List.of(new MindRoots()));
        addMindRootsMana();

        castAndResolveToDiscardChoice();
        harness.handleCardChosen(player2, 0);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player2, "Forest");
        harness.assertNotOnBattlefield(player1, "Forest");
    }

    @Test
    void mayDeclinePuttingADiscardedLandOntoTheBattlefield() {
        harness.setHand(player2, new ArrayList<>(List.of(new Forest(), new Peek())));
        harness.setHand(player1, List.of(new MindRoots()));
        addMindRootsMana();

        castAndResolveToDiscardChoice();
        harness.handleCardChosen(player2, 0);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.GraveyardChoice.class);
        harness.handleGraveyardCardChosen(player1, -1);

        harness.assertInGraveyard(player2, "Forest");
        harness.assertNotOnBattlefield(player1, "Forest");
    }

    private void castAndResolveToDiscardChoice() {
        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
    }

    private void addMindRootsMana() {
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }
}

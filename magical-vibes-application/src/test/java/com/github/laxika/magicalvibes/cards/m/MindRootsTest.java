package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.p.Peek;
import com.github.laxika.magicalvibes.cards.o.ObstinateBaloth;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MindRoots.class, Forest.class, Mountain.class, GrizzlyBears.class, Peek.class})
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

    @Test
    void canTakeTheOnlyCardWhenTargetHasOneLandInHand() {
        harness.setHand(player2, List.of(new Forest()));
        harness.setHand(player1, List.of(new MindRoots()));
        addMindRootsMana();

        castAndResolveToDiscardChoice();
        harness.handleCardChosen(player2, 0);
        harness.handleGraveyardCardChosen(player1, 0);

        assertThat(findPermanent(player1, "Forest").isTapped()).isTrue();
        harness.assertNotInGraveyard(player2, "Forest");
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
    }

    @Test
    void resolvesWithoutAChoiceWhenTargetHasNoCards() {
        harness.setHand(player2, List.of());
        harness.setHand(player1, List.of(new MindRoots()));
        addMindRootsMana();

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player1, "Mind Roots");
    }

    @Test
    @CardUsed({ObstinateBaloth.class})
    void selfTargetingDoesNotApplyOpponentDiscardReplacement() {
        harness.setHand(player1, List.of(new MindRoots(), new ObstinateBaloth(), new Forest()));
        addMindRootsMana();

        harness.castAndResolveSorcery(player1, 0, player1.getId());
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);
        harness.handleGraveyardCardChosen(player1, 0);

        harness.assertInGraveyard(player1, "Obstinate Baloth");
        harness.assertNotOnBattlefield(player1, "Obstinate Baloth");
        assertThat(findPermanent(player1, "Forest").isTapped()).isTrue();
        harness.assertLife(player1, 20);
    }

    private void castAndResolveToDiscardChoice() {
        harness.castAndResolveSorcery(player1, 0, player2.getId());
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
    }

    private void addMindRootsMana() {
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }
}

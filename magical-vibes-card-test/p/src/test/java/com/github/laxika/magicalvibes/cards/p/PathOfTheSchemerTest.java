package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.r.RecklessFireweaver;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ChoiceContext;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.planar.PlanarObject;
import com.github.laxika.magicalvibes.model.planar.PlanechaseState;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PathOfTheSchemer.class, Panopticon.class, Forest.class, GrizzlyBears.class,
        RecklessFireweaver.class})
class PathOfTheSchemerTest extends BaseCardTest {

    private PlanarObject startingPlane;

    @BeforeEach
    void preparePlanechase() {
        gd.planechase = new PlanechaseState();
        gd.planechase.controllerId = player1.getId();
        startingPlane = new PlanarObject(new Panopticon(), gd.nextTimestamp());
        gd.planechase.faceUp.add(startingPlane);
        gd.planechase.deck.add(new Panopticon());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

    @Test
    void millsEachPlayerReanimatesCreatureAsArtifactAndPlaneswalks() {
        Card ownMilledCard = new Forest();
        Card ownSecondMilledCard = new Forest();
        Card opponentMilledCard = new Forest();
        Card opponentSecondMilledCard = new Forest();
        Card creature = new GrizzlyBears();
        harness.setLibrary(player1, List.of(ownMilledCard, ownSecondMilledCard));
        harness.setLibrary(player2, List.of(opponentMilledCard, opponentSecondMilledCard));
        harness.setGraveyard(player2, List.of(creature));

        cast();

        GameData gameData = harness.getGameData();
        assertThat(gameData.playerGraveyards.get(player1.getId()))
                .extracting(Card::getId)
                .containsExactlyInAnyOrder(ownMilledCard.getId(), ownSecondMilledCard.getId());
        assertThat(gameData.playerGraveyards.get(player2.getId()))
                .extracting(Card::getId)
                .containsExactlyInAnyOrder(
                        creature.getId(), opponentMilledCard.getId(), opponentSecondMilledCard.getId());
        assertThat(gameData.interaction.activeInteraction()).isInstanceOf(PendingInteraction.GraveyardChoice.class);

        harness.handleGraveyardCardChosen(player1, 0);
        var reanimated = findPermanent(player1, "Grizzly Bears");
        assertThat(gqs.isArtifact(gd, reanimated)).isTrue();

        harness.handleListChoice(player1, ChoiceContext.WillOfThePlaneswalkersChoice.PLANESWALK);
        harness.handleListChoice(player2, ChoiceContext.WillOfThePlaneswalkersChoice.PLANESWALK);

        assertThat(gd.planechase.faceUp).noneMatch(object -> object == startingPlane).hasSize(1);
    }

    @Test
    void tiedVoteCausesChaosAfterReanimation() {
        Card creature = new GrizzlyBears();
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));
        harness.setLibrary(player2, List.of(new Forest(), new Forest()));
        harness.setGraveyard(player1, List.of(creature));

        cast();
        harness.handleGraveyardCardChosen(player1, 0);
        harness.handleListChoice(player1, ChoiceContext.WillOfThePlaneswalkersChoice.PLANESWALK);
        harness.handleListChoice(player2, ChoiceContext.WillOfThePlaneswalkersChoice.CHAOS);
        harness.passBothPriorities();

        assertThat(gd.planechase.faceUp).containsExactly(startingPlane);
        assertThat(findPermanent(player1, "Grizzly Bears")).isNotNull();
    }

    @Test
    void cannotDeclineReanimationWhenACreatureIsAvailable() {
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest()));
        harness.setLibrary(player2, List.of(new Forest(), new Forest(), new Forest()));
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));

        cast();

        assertThatThrownBy(() -> harness.handleGraveyardCardChosen(player1, -1))
                .isInstanceOf(IllegalStateException.class);
        harness.handleGraveyardCardChosen(player1, 0);
        harness.handleListChoice(player1, ChoiceContext.WillOfThePlaneswalkersChoice.CHAOS);
        harness.handleListChoice(player2, ChoiceContext.WillOfThePlaneswalkersChoice.CHAOS);
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    void canReanimateACreatureMilledByTheSameSpell() {
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of());
        harness.setGraveyard(player2, List.of());
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest()));
        harness.setLibrary(player2, List.of(creature, new Forest(), new Forest()));

        cast();
        harness.handleGraveyardCardChosen(player1, 0);
        var returned = findPermanent(player1, "Grizzly Bears");
        assertThat(returned.getCard().getId()).isEqualTo(creature.getId());
        assertThat(gqs.isArtifact(gd, returned)).isTrue();
        assertThat(gqs.isCreature(gd, returned)).isTrue();
        harness.assertNotInGraveyard(player2, "Grizzly Bears");
        harness.handleListChoice(player1, ChoiceContext.WillOfThePlaneswalkersChoice.CHAOS);
        harness.handleListChoice(player2, ChoiceContext.WillOfThePlaneswalkersChoice.CHAOS);
        harness.passBothPriorities();
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void chaosOrTiedVoteStillTriggersThePlaneWhenNoCreatureCanBeReturned(boolean tiedVote) {
        Card drawnCard = new Forest();
        harness.setGraveyard(player1, List.of());
        harness.setGraveyard(player2, List.of());
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), drawnCard));
        harness.setLibrary(player2, List.of(new Forest(), new Forest(), new Forest()));

        cast();
        harness.handleListChoice(player1, tiedVote
                ? ChoiceContext.WillOfThePlaneswalkersChoice.PLANESWALK
                : ChoiceContext.WillOfThePlaneswalkersChoice.CHAOS);
        harness.handleListChoice(player2, ChoiceContext.WillOfThePlaneswalkersChoice.CHAOS);
        harness.passBothPriorities();

        assertThat(gd.planechase.faceUp).containsExactly(startingPlane);
        assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getId)
                .containsExactly(drawnCard.getId());
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Path of the Schemer");
    }

    @Test
    void reanimatedCreatureEntersAsAnArtifactForEntryTriggers() {
        harness.addToBattlefield(player1, new RecklessFireweaver());
        harness.setLife(player2, 20);
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest()));
        harness.setLibrary(player2, List.of(new Forest(), new Forest(), new Forest()));
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));

        cast();
        harness.handleGraveyardCardChosen(player1, 0);
        harness.handleListChoice(player1, ChoiceContext.WillOfThePlaneswalkersChoice.CHAOS);
        harness.handleListChoice(player2, ChoiceContext.WillOfThePlaneswalkersChoice.CHAOS);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player2, 19);
    }

    private void cast() {
        harness.setHand(player1, List.of(new PathOfTheSchemer()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castAndResolveSorcery(player1, 0, 0);
    }
}

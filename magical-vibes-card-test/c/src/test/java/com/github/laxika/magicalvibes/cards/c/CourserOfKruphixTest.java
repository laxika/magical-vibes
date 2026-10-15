package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.t.TurnToFrog;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CourserOfKruphix.class, Forest.class})
class CourserOfKruphixTest extends BaseCardTest {

    @Test
    @DisplayName("plays a land from the top of the library and gains 1 life")
    void playsLandFromTopAndGainsLife() {
        harness.addToBattlefield(player1, new CourserOfKruphix());
        Forest forest = new Forest();
        harness.setLibrary(player1, List.of(forest));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castAndResolveFromLibraryTop(player1);

        harness.assertOnBattlefield(player1, "Forest");
        assertThat(gd.playerDecks.get(player1.getId())).doesNotContain(forest);
        assertThat(gd.landsPlayedThisTurn.get(player1.getId())).isEqualTo(1);
        harness.assertLife(player1, 21);
    }

    @Test
    @DisplayName("does not allow playing a land from the top without Courser of Kruphix")
    void requiresPermissionToPlayLandFromTop() {
        Forest forest = new Forest();
        harness.setLibrary(player1, List.of(forest));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.castFromLibraryTop(player1))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(forest);
    }

    @Test
    void revealsOnlyControllersTopCardToBothPlayers() {
        harness.addToBattlefield(player1, new CourserOfKruphix());
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setLibrary(player2, List.of(new CourserOfKruphix()));
        harness.clearMessages();

        harness.publishState();

        for (var connection : List.of(harness.getConn1(), harness.getConn2())) {
            assertThat(connection.getSentMessages()).anyMatch(message ->
                    message.contains("\"revealedLibraryTopCards\":[[{")
                            && message.contains("Forest")
                            && message.contains("}],[]]"));
        }
    }

    @Test
    void stopsRevealingAndPermittingLandPlayWhenCourserLeaves() {
        harness.addToBattlefield(player1, new CourserOfKruphix());
        Forest forest = new Forest();
        harness.setLibrary(player1, List.of(forest));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        gd.playerBattlefields.get(player1.getId()).clear();
        harness.clearMessages();

        harness.publishState();

        for (var connection : List.of(harness.getConn1(), harness.getConn2())) {
            assertThat(connection.getSentMessages()).anyMatch(message ->
                    message.contains("\"revealedLibraryTopCards\":[[],[]]"));
        }
        assertThatThrownBy(() -> harness.castFromLibraryTop(player1))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(forest);
    }

    @Test
    void landPlayedFromHandUsesTheSameLandAllowance() {
        harness.addToBattlefield(player1, new CourserOfKruphix());
        Forest forest = new Forest();
        harness.setLibrary(player1, List.of(forest));
        harness.setHand(player1, List.of(new Forest()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.playLand(player1, 0);
        harness.passBothPriorities();

        harness.assertLife(player1, 21);
        assertThatThrownBy(() -> harness.castFromLibraryTop(player1))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(forest);
    }

    @Test
    void cannotPlayLandOutsideMainPhaseOrOnOpponentsTurn() {
        harness.addToBattlefield(player1, new CourserOfKruphix());
        Forest forest = new Forest();
        harness.setLibrary(player1, List.of(forest));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UPKEEP);

        assertThatThrownBy(() -> harness.castFromLibraryTop(player1))
                .isInstanceOf(IllegalStateException.class);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.castFromLibraryTop(player1))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(forest);
    }

    @Test
    void doesNotPermitCastingNonlandTopCard() {
        harness.addToBattlefield(player1, new CourserOfKruphix());
        CourserOfKruphix topCard = new CourserOfKruphix();
        harness.setLibrary(player1, List.of(topCard));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.GREEN, 3);

        assertThatThrownBy(() -> harness.castFromLibraryTop(player1))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void opponentsLandDoesNotTriggerLifeGain() {
        harness.addToBattlefield(player1, new CourserOfKruphix());
        harness.setHand(player2, List.of(new Forest()));
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.playLand(player2, 0);

        assertThat(gd.stack).isEmpty();
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    @CardUsed({TurnToFrog.class})
    void doesNotTriggerLandfallAfterLosingAllAbilities() {
        var courser = harness.addToBattlefieldAndReturn(player1, new CourserOfKruphix());
        harness.setHand(player1, List.of(new TurnToFrog(), new Forest()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castAndResolveInstant(player1, 0, courser.getId());
        assertThat(gqs.hasLostPrintedAbilities(gd, courser)).isTrue();
        harness.playLand(player1, 0);

        assertThat(gd.stack).isEmpty();
        harness.assertLife(player1, 20);
    }

    @Test
    void cannotPlayAnotherLandWhileLandfallTriggerIsOnStack() {
        harness.addToBattlefield(player1, new CourserOfKruphix());
        Forest nextLand = new Forest();
        harness.setLibrary(player1, List.of(new Forest(), nextLand));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castFromLibraryTop(player1);

        assertThat(gd.stack).hasSize(1);
        gd.landsPlayedThisTurn.put(player1.getId(), 0);
        assertThat(gd.landsPlayedThisTurn.get(player1.getId())).isZero();
        assertThatThrownBy(() -> harness.castFromLibraryTop(player1))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(nextLand);
        harness.passBothPriorities();
        harness.assertLife(player1, 21);
    }

    @Test
    void eachCourserTriggersAndTriggersSurviveTheirSourcesLeaving() {
        harness.addToBattlefield(player1, new CourserOfKruphix());
        harness.addToBattlefield(player1, new CourserOfKruphix());
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castFromLibraryTop(player1);

        assertThat(gd.stack).hasSize(2);
        harness.assertLife(player1, 20);
        gd.playerBattlefields.get(player1.getId()).removeIf(
                permanent -> permanent.getCard() instanceof CourserOfKruphix);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player1, 22);
        harness.assertLife(player2, 20);
    }
}

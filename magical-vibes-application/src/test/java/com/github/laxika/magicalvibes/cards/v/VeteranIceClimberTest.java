package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({VeteranIceClimber.class})
class VeteranIceClimberTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking mills a target player by Veteran Ice Climber's power")
    void attackingMillsTargetPlayerByPower() {
        Permanent veteran = addReadyVeteran();
        veteran.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.setLibrary(player2, libraryWithFiveCards());

        declareAttackers(List.of(indexOf(player1, veteran)));

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).hasSize(3);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Attacking can resolve without choosing a player")
    void attackingCanResolveWithoutTarget() {
        Permanent veteran = addReadyVeteran();
        harness.setLibrary(player2, libraryWithFiveCards());

        declareAttackers(List.of(indexOf(player1, veteran)));

        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).hasSize(5);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Veteran Ice Climber cannot be blocked")
    void cannotBeBlocked() {
        addCreatureReady(player2, new VeteranIceClimber());

        Permanent veteran = addReadyVeteran();
        veteran.setAttacking(true);

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be blocked");
    }

    @Test
    @DisplayName("The controller can be chosen as the milling target")
    void attackingCanMillController() {
        Permanent veteran = addReadyVeteran();
        harness.setLibrary(player1, libraryWithFiveCards());
        harness.setLibrary(player2, libraryWithFiveCards());

        declareAttackers(List.of(indexOf(player1, veteran)));
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(4);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(5);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Milling uses power at resolution rather than when attacking")
    void millingUsesPowerAtResolution() {
        Permanent veteran = addReadyVeteran();
        harness.setLibrary(player2, libraryWithFiveCards());

        declareAttackers(List.of(indexOf(player1, veteran)));
        harness.handlePermanentChosen(player1, player2.getId());
        veteran.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).hasSize(2);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(3);
    }

    @Test
    @DisplayName("Zero power mills no cards")
    void zeroPowerMillsNoCards() {
        Permanent veteran = addReadyVeteran();
        veteran.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 1);
        harness.setLibrary(player2, libraryWithFiveCards());

        declareAttackers(List.of(indexOf(player1, veteran)));
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).hasSize(5);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Milling more cards than remain mills the entire library")
    void millingStopsAtEmptyLibrary() {
        Permanent veteran = addReadyVeteran();
        veteran.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        harness.setLibrary(player2, List.of(new VeteranIceClimber()));

        declareAttackers(List.of(indexOf(player1, veteran)));
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Attacking with vigilance leaves Veteran Ice Climber untapped")
    void attackingDoesNotTapVeteran() {
        Permanent veteran = addReadyVeteran();

        declareAttackers(List.of(indexOf(player1, veteran)));

        assertThat(veteran.isTapped()).isFalse();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
    }

    private Permanent addReadyVeteran() {
        return addCreatureReady(player1, new VeteranIceClimber());
    }

    private int indexOf(com.github.laxika.magicalvibes.model.Player player, Permanent permanent) {
        return gd.playerBattlefields.get(player.getId()).indexOf(permanent);
    }

    private List<Card> libraryWithFiveCards() {
        return List.of(
                new VeteranIceClimber(),
                new VeteranIceClimber(),
                new VeteranIceClimber(),
                new VeteranIceClimber(),
                new VeteranIceClimber()
        );
    }
}

package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.b.BorderPatrol;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MistOfStagnation.class, BorderPatrol.class})
class MistOfStagnationTest extends BaseCardTest {

    @Test
    @DisplayName("Prevents permanents from untapping during their controllers' untap steps")
    void preventsUntapDuringUntapSteps() {
        harness.addToBattlefieldAndReturn(player1, new MistOfStagnation());
        Permanent ownPermanent = addCreatureReady(player1, new BorderPatrol());
        Permanent opponentPermanent = addCreatureReady(player2, new BorderPatrol());
        ownPermanent.tap();
        opponentPermanent.tap();

        advanceToUpkeep(player1);

        assertThat(ownPermanent.isTapped()).isTrue();

        advanceToUpkeep(player2);

        assertThat(opponentPermanent.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Does not present a choice when the active player's graveyard is empty")
    void doesNotPresentChoiceForEmptyActivePlayersGraveyard() {
        harness.addToBattlefieldAndReturn(player1, new MistOfStagnation());
        Permanent ownPermanent = addCreatureReady(player1, new BorderPatrol());
        ownPermanent.tap();
        harness.setGraveyard(player2, List.of());

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class)).isNull();
        assertThat(ownPermanent.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The active player chooses distinct permanents using their graveyard count")
    void choosesDistinctPermanentsForActivePlayersGraveyard() {
        Permanent mist = harness.addToBattlefieldAndReturn(player1, new MistOfStagnation());
        Permanent ownPermanent = addCreatureReady(player1, new BorderPatrol());
        Permanent opponentPermanent = addCreatureReady(player2, new BorderPatrol());
        ownPermanent.tap();
        opponentPermanent.tap();

        harness.setGraveyard(player1, List.of(new BorderPatrol(), new BorderPatrol(), new BorderPatrol()));
        harness.setGraveyard(player2, List.of(new BorderPatrol()));

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player2.getId());
        assertThat(choice.maxCount()).isEqualTo(1);
        assertThat(choice.validIds()).containsExactlyInAnyOrder(
                mist.getId(), ownPermanent.getId(), opponentPermanent.getId());

        assertThatThrownBy(() -> harness.handleMultiplePermanentsChosen(player2, List.of()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class)).isNotNull();

        harness.handleMultiplePermanentsChosen(player2, List.of(opponentPermanent.getId()));

        assertThat(opponentPermanent.isTapped()).isFalse();
        assertThat(ownPermanent.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Untaps all available permanents when the graveyard count is larger")
    void untapsAllAvailablePermanentsWhenCountIsLarger() {
        Permanent mist = harness.addToBattlefieldAndReturn(player1, new MistOfStagnation());
        Permanent ownPermanent = addCreatureReady(player1, new BorderPatrol());
        mist.tap();
        ownPermanent.tap();
        harness.setGraveyard(player2, List.of(new BorderPatrol(), new BorderPatrol(), new BorderPatrol(), new BorderPatrol()));

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(mist.isTapped()).isFalse();
        assertThat(ownPermanent.isTapped()).isFalse();
    }
}

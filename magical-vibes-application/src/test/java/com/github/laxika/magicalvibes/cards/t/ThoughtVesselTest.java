package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.n.NullProfusion;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ThoughtVessel.class, NullProfusion.class})
class ThoughtVesselTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping Thought Vessel adds one colorless mana")
    void tappingAddsColorlessMana() {
        Permanent vessel = harness.addToBattlefieldAndReturn(player1, new ThoughtVessel());

        harness.activateAbility(player1, 0, null, null);

        assertThat(vessel.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Thought Vessel gives its controller no maximum hand size")
    void controllerHasNoMaximumHandSize() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.END_STEP);
        harness.addToBattlefield(player1, new ThoughtVessel());
        harness.setHand(player1, List.of(
                new ThoughtVessel(), new ThoughtVessel(), new ThoughtVessel(),
                new ThoughtVessel(), new ThoughtVessel(), new ThoughtVessel(),
                new ThoughtVessel(), new ThoughtVessel(), new ThoughtVessel()
        ));

        harness.getGameService().advanceStep(gd);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class)).isNull();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(9);
    }

    @Test
    @DisplayName("Thought Vessel can produce mana the turn it enters the battlefield")
    void canTapImmediatelyAfterResolving() {
        harness.castFromHand(player1, new ThoughtVessel(), "{2}");
        harness.passBothPriorities();

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A tapped Thought Vessel cannot pay its tap cost again")
    void cannotActivateAgainWhileTapped() {
        harness.addToBattlefield(player1, new ThoughtVessel());
        harness.activateAbility(player1, 0, null, null);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Thought Vessel does not remove its opponent's hand size limit")
    void opponentStillDiscardsDuringCleanup() {
        harness.addToBattlefield(player1, new ThoughtVessel());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.END_STEP);
        harness.setHand(player2, List.of(
                new ThoughtVessel(), new ThoughtVessel(), new ThoughtVessel(),
                new ThoughtVessel(), new ThoughtVessel(), new ThoughtVessel(),
                new ThoughtVessel(), new ThoughtVessel(), new ThoughtVessel()
        ));

        gs.advanceStep(gd);

        PendingInteraction.DiscardChoice choice = gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player2.getId());
        assertThat(choice.remainingCount()).isEqualTo(2);
    }

    @Test
    @DisplayName("Removing Thought Vessel restores the normal hand size limit")
    void removingVesselRestoresHandSizeLimit() {
        Permanent vessel = harness.addToBattlefieldAndReturn(player1, new ThoughtVessel());
        harness.getPermanentRemovalService().removePermanentToGraveyard(gd, vessel);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.END_STEP);
        harness.setHand(player1, List.of(
                new ThoughtVessel(), new ThoughtVessel(), new ThoughtVessel(),
                new ThoughtVessel(), new ThoughtVessel(), new ThoughtVessel(),
                new ThoughtVessel(), new ThoughtVessel(), new ThoughtVessel()
        ));

        gs.advanceStep(gd);

        PendingInteraction.DiscardChoice choice = gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.remainingCount()).isEqualTo(2);
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(7);
    }

    @Test
    @DisplayName("A later Null Profusion overrides Thought Vessel's unlimited hand size")
    void laterNumericLimitOverridesUnlimitedHandSize() {
        harness.enterBattlefieldAndReturn(player1, new ThoughtVessel());
        harness.enterBattlefieldAndReturn(player1, new NullProfusion());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.END_STEP);
        harness.setHand(player1, List.of(new ThoughtVessel(), new ThoughtVessel(), new ThoughtVessel()));

        gs.advanceStep(gd);

        PendingInteraction.DiscardChoice choice = gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player1.getId());
        assertThat(choice.remainingCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("A later Thought Vessel overrides Null Profusion's numeric hand size limit")
    void laterUnlimitedHandSizeOverridesNumericLimit() {
        harness.enterBattlefieldAndReturn(player1, new NullProfusion());
        harness.enterBattlefieldAndReturn(player1, new ThoughtVessel());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.END_STEP);
        harness.setHand(player1, List.of(new ThoughtVessel(), new ThoughtVessel(), new ThoughtVessel()));

        gs.advanceStep(gd);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class)).isNull();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
    }
}

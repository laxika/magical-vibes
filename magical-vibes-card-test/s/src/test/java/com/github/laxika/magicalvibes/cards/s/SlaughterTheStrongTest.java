package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.z.ZetalpaPrimalDawn;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.TestCards;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SlaughterTheStrong.class, GrizzlyBears.class, HillGiant.class, ZetalpaPrimalDawn.class})
class SlaughterTheStrongTest extends BaseCardTest {

    @Test
    @DisplayName("Each player chooses creatures to keep in APNAP order")
    void eachPlayerChoosesInApnapOrder() {
        Permanent player1Kept = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent player1Sacrificed = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        Permanent player2Sacrificed = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent player2Kept = harness.addToBattlefieldAndReturn(player2, new HillGiant());

        cast();

        PendingInteraction.MultiPermanentChoice firstChoice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(firstChoice.playerId()).isEqualTo(player1.getId());
        harness.handleMultiplePermanentsChosen(player1, List.of(player1Kept.getId()));

        PendingInteraction.MultiPermanentChoice secondChoice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(secondChoice.playerId()).isEqualTo(player2.getId());
        harness.handleMultiplePermanentsChosen(player2, List.of(player2Kept.getId()));

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(player1Kept).doesNotContain(player1Sacrificed);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(player2Kept).doesNotContain(player2Sacrificed);
        harness.assertInGraveyard(player1, "Hill Giant");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("A choice above four total power is rejected and leaves the prompt active")
    void rejectsChoiceAbovePowerLimit() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent hillGiant = harness.addToBattlefieldAndReturn(player1, new HillGiant());

        cast();

        assertThatThrownBy(() -> harness.handleMultiplePermanentsChosen(
                player1, List.of(bears.getId(), hillGiant.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("above 4");
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class)).isNotNull();

        harness.handleMultiplePermanentsChosen(player1, List.of(bears.getId()));

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Hill Giant");
    }

    @Test
    @DisplayName("Negative power reduces the total power of the creatures kept")
    void negativePowerReducesTotal() {
        Permanent negativePowerCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        TestCards.mutableCard(negativePowerCreature).setPower(-1);
        Permanent hillGiant = harness.addToBattlefieldAndReturn(player1, new HillGiant());

        cast();

        harness.handleMultiplePermanentsChosen(player1,
                List.of(negativePowerCreature.getId(), hillGiant.getId()));

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .contains(negativePowerCreature, hillGiant);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void canKeepMultipleCreaturesWithExactlyFourTotalPower() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent giant = harness.addToBattlefieldAndReturn(player1, new HillGiant());

        cast();
        harness.handleMultiplePermanentsChosen(player1, List.of(first.getId(), second.getId()));

        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactlyInAnyOrder(first, second);
        harness.assertInGraveyard(player1, "Hill Giant");
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(giant);
    }

    @Test
    void mayChooseNoCreaturesAndSacrificesIndestructibleCreatures() {
        harness.addToBattlefield(player1, new ZetalpaPrimalDawn());

        cast();
        harness.handleMultiplePermanentsChosen(player1, List.of());

        harness.assertInGraveyard(player1, "Zetalpa, Primal Dawn");
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void negativePowerAllowsKeepingACreatureWithPowerAboveFour() {
        Permanent negative = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        negative.setPowerModifier(-3);
        Permanent giant = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        giant.setPowerModifier(2);

        cast();
        harness.handleMultiplePermanentsChosen(player1, List.of(negative.getId(), giant.getId()));

        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactlyInAnyOrder(negative, giant);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void usesModifiedPowerWhenCheckingTheLimit() {
        Permanent zetalpa = harness.addToBattlefieldAndReturn(player1, new ZetalpaPrimalDawn());
        zetalpa.setPowerModifier(1);

        cast();

        assertThatThrownBy(() -> harness.handleMultiplePermanentsChosen(player1, List.of(zetalpa.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("above 4");
        harness.handleMultiplePermanentsChosen(player1, List.of());

        harness.assertInGraveyard(player1, "Zetalpa, Primal Dawn");
    }

    @Test
    void waitsForAllPlayersChoicesBeforeSacrificing() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new ZetalpaPrimalDawn());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new ZetalpaPrimalDawn());

        cast();
        harness.handleMultiplePermanentsChosen(player1, List.of());

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(first);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(second);

        harness.handleMultiplePermanentsChosen(player2, List.of());

        harness.assertInGraveyard(player1, "Zetalpa, Primal Dawn");
        harness.assertInGraveyard(player2, "Zetalpa, Primal Dawn");
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
    }

    @Test
    void skipsPlayersWithoutCreatures() {
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new ZetalpaPrimalDawn());

        cast();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class).playerId())
                .isEqualTo(player2.getId());
        harness.handleMultiplePermanentsChosen(player2, List.of(opponentCreature.getId()));

        harness.assertOnBattlefield(player2, "Zetalpa, Primal Dawn");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void cannotChooseAnOpponentsCreatureToKeep() {
        Permanent own = harness.addToBattlefieldAndReturn(player1, new ZetalpaPrimalDawn());
        Permanent opponent = harness.addToBattlefieldAndReturn(player2, new ZetalpaPrimalDawn());

        cast();

        assertThatThrownBy(() -> harness.handleMultiplePermanentsChosen(player1, List.of(opponent.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid selection");
        harness.handleMultiplePermanentsChosen(player1, List.of(own.getId()));
        harness.handleMultiplePermanentsChosen(player2, List.of(opponent.getId()));

        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(own);
        assertThat(gd.playerBattlefields.get(player2.getId())).containsExactly(opponent);
    }

    @Test
    void resolvesWithoutAChoiceWhenNeitherPlayerControlsCreatures() {
        cast();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertInGraveyard(player1, "Slaughter the Strong");
    }

    private void cast() {
        harness.castFromHand(player1, new SlaughterTheStrong(), "{1}{W}{W}");
        harness.passBothPriorities();
    }
}

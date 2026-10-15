package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.d.DynamiteDiver;
import com.github.laxika.magicalvibes.cards.e.EndriderSpikespitter;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ClamorousIronclad.class, EndriderSpikespitter.class, DynamiteDiver.class})
class ClamorousIroncladTest extends BaseCardTest {

    @Test
    @DisplayName("Cycling discards Clamorous Ironclad and draws a card")
    void cyclingDrawsACard() {
        harness.setHand(player1, List.of(new ClamorousIronclad()));
        harness.setLibrary(player1, List.of(new EndriderSpikespitter()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Clamorous Ironclad");
        harness.assertInHand(player1, "Endrider Spikespitter");
    }

    @Test
    @DisplayName("Crew 3 animates Clamorous Ironclad and taps the crew")
    void crewAnimatesIroncladAndTapsCrew() {
        Permanent ironclad = addCreatureReady(player1, new ClamorousIronclad());
        Permanent crew = addCreatureReady(player1, new EndriderSpikespitter());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, ironclad)).isTrue();
        assertThat(crew.isTapped()).isTrue();
    }

    @Test
    void cyclingDiscardsAsCostAndDrawsOnlyOnResolution() {
        harness.setHand(player1, List.of(new ClamorousIronclad()));
        harness.setLibrary(player1, List.of(new EndriderSpikespitter()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateHandAbility(player1, 0, null);

        harness.assertInGraveyard(player1, "Clamorous Ironclad");
        harness.assertNotInHand(player1, "Clamorous Ironclad");
        harness.assertNotInHand(player1, "Endrider Spikespitter");
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();
        harness.assertInHand(player1, "Endrider Spikespitter");
    }

    @Test
    void cyclingCannotBePaidWithOnlyGenericMana() {
        harness.setHand(player1, List.of(new ClamorousIronclad()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInHand(player1, "Clamorous Ironclad");
        harness.assertNotInGraveyard(player1, "Clamorous Ironclad");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void summoningSickCreatureCanCrewAndAnimationWaitsForResolution() {
        Permanent ironclad = harness.addToBattlefieldAndReturn(player1, new ClamorousIronclad());
        Permanent crew = harness.addToBattlefieldAndReturn(player1, new EndriderSpikespitter());
        crew.setSummoningSick(true);

        harness.activateAbility(player1, 0, null, null);

        assertThat(crew.isTapped()).isTrue();
        assertThat(gqs.isCreature(gd, ironclad)).isFalse();
        assertThat(ironclad.isTapped()).isFalse();

        harness.passBothPriorities();
        assertThat(gqs.isCreature(gd, ironclad)).isTrue();
        assertThat(ironclad.isTapped()).isFalse();
    }

    @Test
    void tappedAndOpposingCreaturesCannotPayCrewCost() {
        Permanent ironclad = harness.addToBattlefieldAndReturn(player1, new ClamorousIronclad());
        Permanent tappedCrew = harness.addToBattlefieldAndReturn(player1, new EndriderSpikespitter());
        tappedCrew.tap();
        harness.addToBattlefield(player2, new EndriderSpikespitter());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gqs.isCreature(gd, ironclad)).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void pilotCrewPowerBonusPaysCrewThree() {
        Permanent ironclad = harness.addToBattlefieldAndReturn(player1, new ClamorousIronclad());
        Permanent pilot = harness.addToBattlefieldAndReturn(player1, new DynamiteDiver());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(pilot.isTapped()).isTrue();
        assertThat(gqs.isCreature(gd, ironclad)).isTrue();
    }

    @Test
    void crewAnimationExpiresAtEndOfTurn() {
        Permanent ironclad = harness.addToBattlefieldAndReturn(player1, new ClamorousIronclad());
        harness.addToBattlefield(player1, new EndriderSpikespitter());
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(gqs.isCreature(gd, ironclad)).isTrue();

        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(gqs.isCreature(gd, ironclad)).isFalse();
    }

    @Test
    void crewedIroncladRequiresAtLeastTwoBlockers() {
        addCreatureReady(player1, new ClamorousIronclad());
        addCreatureReady(player1, new EndriderSpikespitter());
        Permanent firstBlocker = addCreatureReady(player2, new EndriderSpikespitter());
        Permanent secondBlocker = addCreatureReady(player2, new EndriderSpikespitter());
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("two or more creatures");

        gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0), new BlockerAssignment(1, 0)));

        assertThat(firstBlocker.isBlocking()).isTrue();
        assertThat(secondBlocker.isBlocking()).isTrue();
    }
}
package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MessengerHawk;
import com.github.laxika.magicalvibes.cards.o.Oxidize;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FireNationWarship.class, GrizzlyBears.class, Oxidize.class, MessengerHawk.class})
class FireNationWarshipTest extends BaseCardTest {

    @Test
    @DisplayName("When Fire Nation Warship dies, its controller creates a Clue")
    void deathTriggerCreatesClueForController() {
        Permanent warship = addWarshipReady(player1);

        harness.setHand(player1, List.of(new Oxidize()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castInstant(player1, 0, warship.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Fire Nation Warship");
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Clue")).hasSize(1);
        assertThat(findPermanents(player2, "Clue")).isEmpty();
    }

    @Test
    @DisplayName("Crew 2 animates Fire Nation Warship and taps the crew")
    void crewAnimatesWarshipAndTapsCrew() {
        Permanent warship = addWarshipReady(player1);
        Permanent crew = addCreatureReady(player1, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, warship)).isTrue();
        assertThat(crew.isTapped()).isTrue();
    }

    private Permanent addWarshipReady(Player player) {
        return addCreatureReady(player, new FireNationWarship());
    }

    @Test
    void summoningSickCreatureCanCrew() {
        Permanent warship = harness.addToBattlefieldAndReturn(player1, new FireNationWarship());
        Permanent crew = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        crew.setSummoningSick(true);

        harness.activateAbility(player1, 0, null, null);
        assertThat(crew.isTapped()).isTrue();
        assertThat(gqs.isCreature(gd, warship)).isFalse();
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, warship)).isTrue();
        assertThat(warship.isTapped()).isFalse();
    }

    @Test
    void tappedCreatureCannotPayCrewCost() {
        Permanent warship = addWarshipReady(player1);
        Permanent crew = addCreatureReady(player1, new GrizzlyBears());
        crew.tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gqs.isCreature(gd, warship)).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void crewedWarshipCreatesExactlyOneClueWhenDestroyed() {
        Permanent warship = addWarshipReady(player1);
        addCreatureReady(player1, new GrizzlyBears());
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new Oxidize()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castInstant(player1, 0, warship.getId());
        harness.passBothPriorities();
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Fire Nation Warship");
        assertThat(findPermanents(player1, "Clue")).hasSize(1);
    }

    @Test
    void destroyingOpponentsWarshipGivesClueToOpponent() {
        Permanent warship = addWarshipReady(player2);
        harness.setHand(player1, List.of(new Oxidize()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castInstant(player1, 0, warship.getId());
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Clue")).isEmpty();
        assertThat(findPermanents(player2, "Clue")).hasSize(1);
    }

    @Test
    void clueCanBeSacrificedForTwoManaToDraw() {
        Permanent warship = addWarshipReady(player1);
        harness.setHand(player1, List.of(new Oxidize()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castInstant(player1, 0, warship.getId());
        harness.passBothPriorities();
        resolveAllTriggers();
        harness.setLibrary(player1, List.of(new FireNationWarship()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        assertThat(findPermanents(player1, "Clue")).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.passBothPriorities();

        harness.assertInHand(player1, "Fire Nation Warship");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    void crewAnimationExpiresAtEndOfTurn() {
        Permanent warship = addWarshipReady(player1);
        addCreatureReady(player1, new GrizzlyBears());
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(gqs.isCreature(gd, warship)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, warship)).isFalse();
        assertThat(gqs.isArtifact(warship)).isTrue();
    }

    @Test
    void twoOnePowerCreaturesCanCrewWarshipToBlockFlyingAttacker() {
        Permanent warship = addWarshipReady(player1);
        Permanent firstCrew = addCreatureReady(player1, new MessengerHawk());
        Permanent secondCrew = addCreatureReady(player1, new MessengerHawk());
        addCreatureReady(player2, new MessengerHawk());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(firstCrew.isTapped()).isTrue();
        assertThat(secondCrew.isTapped()).isTrue();

        declareAttackersAndPrepareBlockers(player2, List.of(0));
        gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(0, 0)));

        assertThat(warship.isBlocking()).isTrue();
    }
}

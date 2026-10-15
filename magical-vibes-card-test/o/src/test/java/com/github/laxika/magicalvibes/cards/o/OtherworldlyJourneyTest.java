package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.i.IsamaruHoundOfKonda;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({OtherworldlyJourney.class, IsamaruHoundOfKonda.class, Island.class})
class OtherworldlyJourneyTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles a creature and returns it at the next end step with a +1/+1 counter")
    void returnsCreatureWithCounterAtNextEndStep() {
        harness.addToBattlefield(player1, new IsamaruHoundOfKonda());
        harness.setHand(player1, List.of(new OtherworldlyJourney()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        UUID originalId = harness.getPermanentId(player1, "Isamaru, Hound of Konda");
        harness.castAndResolveInstant(player1, 0, originalId);

        harness.assertNotOnBattlefield(player1, "Isamaru, Hound of Konda");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Isamaru, Hound of Konda"));

        advanceToEndStep();

        Permanent returned = findPermanent(player1, "Isamaru, Hound of Konda");
        assertThat(returned.getId()).isNotEqualTo(originalId);
        assertThat(returned.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .noneMatch(card -> card.getName().equals("Isamaru, Hound of Konda"));
    }

    @Test
    @DisplayName("Returns an opponent's creature under its owner's control with a +1/+1 counter")
    void returnsOpponentsCreatureToItsOwner() {
        harness.addToBattlefield(player2, new IsamaruHoundOfKonda());
        harness.setHand(player1, List.of(new OtherworldlyJourney()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        UUID originalId = harness.getPermanentId(player2, "Isamaru, Hound of Konda");
        harness.castAndResolveInstant(player1, 0, originalId);

        harness.assertNotOnBattlefield(player2, "Isamaru, Hound of Konda");
        harness.assertNotOnBattlefield(player1, "Isamaru, Hound of Konda");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getName().equals("Isamaru, Hound of Konda"));

        advanceToEndStep();

        Permanent returned = findPermanent(player2, "Isamaru, Hound of Konda");
        assertThat(returned.getId()).isNotEqualTo(originalId);
        assertThat(returned.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .noneMatch(card -> card.getName().equals("Isamaru, Hound of Konda"));
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreaturePermanent() {
        harness.addToBattlefield(player1, new Island());
        harness.setHand(player1, List.of(new OtherworldlyJourney()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        UUID islandId = harness.getPermanentId(player1, "Island");
        assertThatThrownBy(() -> harness.castInstant(player1, 0, islandId))
                .isInstanceOf(IllegalStateException.class);
    }

    private void advanceToEndStep() {
        harness.passUntil(player1, TurnStep.END_STEP);
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("A stolen creature returns to its owner rather than its previous controller")
    void returnsStolenCreatureToOwner() {
        Permanent stolen = harness.addToBattlefieldAndReturn(player1, new IsamaruHoundOfKonda());
        gd.stolenCreatures.put(stolen.getId(), player2.getId());
        stolen.getCard().setOwnerId(player2.getId());
        harness.setHand(player1, List.of(new OtherworldlyJourney()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castAndResolveInstant(player1, 0, stolen.getId());
        advanceToEndStep();

        harness.assertNotOnBattlefield(player1, "Isamaru, Hound of Konda");
        assertThat(findPermanent(player2, "Isamaru, Hound of Konda")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Returning creates an untapped new creature with only the new counter")
    void discardsOldCountersAndTappedState() {
        Permanent original = harness.addToBattlefieldAndReturn(player1, new IsamaruHoundOfKonda());
        original.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);
        original.tap();
        harness.setHand(player1, List.of(new OtherworldlyJourney()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castAndResolveInstant(player1, 0, original.getId());
        advanceToEndStep();

        Permanent returned = findPermanent(player1, "Isamaru, Hound of Konda");
        assertThat(returned.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(returned.isTapped()).isFalse();
        assertThat(returned.isSummoningSick()).isTrue();
    }

    @Test
    @DisplayName("Casting during an end step waits until the following turn's end step")
    void castDuringEndStepReturnsAtFollowingEndStep() {
        harness.addToBattlefield(player1, new IsamaruHoundOfKonda());
        harness.setHand(player1, List.of(new OtherworldlyJourney()));
        harness.passUntil(player1, TurnStep.END_STEP);
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castAndResolveInstant(player1, 0,
                harness.getPermanentId(player1, "Isamaru, Hound of Konda"));
        harness.assertNotOnBattlefield(player1, "Isamaru, Hound of Konda");

        harness.passUntilWithNoAttackers(player2, TurnStep.POSTCOMBAT_MAIN);
        harness.assertNotOnBattlefield(player1, "Isamaru, Hound of Konda");
        harness.passUntil(player2, TurnStep.END_STEP);
        harness.assertNotOnBattlefield(player1, "Isamaru, Hound of Konda");
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Isamaru, Hound of Konda")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("A second Journey removing the target makes the first spell fail to resolve")
    void removedTargetDoesNotCreateAnotherReturnOrCounter() {
        harness.addToBattlefield(player1, new IsamaruHoundOfKonda());
        harness.setHand(player1, List.of(new OtherworldlyJourney()));
        harness.setHand(player2, List.of(new OtherworldlyJourney()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player2, ManaColor.WHITE, 2);
        UUID targetId = harness.getPermanentId(player1, "Isamaru, Hound of Konda");

        harness.castInstant(player1, 0, targetId);
        harness.castAndResolveInstant(player2, 0, targetId);
        harness.passBothPriorities();
        harness.assertNotOnBattlefield(player1, "Isamaru, Hound of Konda");
        harness.assertInGraveyard(player1, "Otherworldly Journey");
        harness.assertInGraveyard(player2, "Otherworldly Journey");

        advanceToEndStep();

        assertThat(findPermanent(player1, "Isamaru, Hound of Konda")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
    }
}

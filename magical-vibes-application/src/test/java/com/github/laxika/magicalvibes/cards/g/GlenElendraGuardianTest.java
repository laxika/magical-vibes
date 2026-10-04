package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.d.Darkblast;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GlenElendraGuardian.class, Forest.class, LlanowarElves.class, Shock.class,
        Darkblast.class, GlenElendrasAnswer.class})
class GlenElendraGuardianTest extends BaseCardTest {

    @Test
    @DisplayName("Enters the battlefield with a -1/-1 counter")
    void entersWithMinusOneMinusOneCounter() {
        harness.setHand(player1, List.of(new GlenElendraGuardian()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent guardian = findPermanent(player1, "Glen Elendra Guardian");
        assertThat(guardian.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Removes a counter to counter a noncreature spell and its controller draws")
    void countersNoncreatureSpellAndItsControllerDraws() {
        Permanent guardian = addReadyGuardian();
        guardian.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 1);
        harness.setLibrary(player2, List.of(new Forest()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        Shock shock = new Shock();
        harness.setHand(player2, List.of(shock));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.setLife(player1, 20);

        harness.passPriority(player1);
        harness.castInstant(player2, 0, player1.getId());
        harness.activateAbility(player1, 0, null, shock.getId());

        assertThat(guardian.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isZero();

        resolveAllTriggers();

        harness.assertInGraveyard(player2, "Shock");
        harness.assertLife(player1, 20);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Cannot target a creature spell")
    void cannotTargetCreatureSpell() {
        Permanent guardian = addReadyGuardian();
        guardian.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        LlanowarElves elves = new LlanowarElves();
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(elves));
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.castCreature(player2, 0);
        harness.passPriority(player2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, elves.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The countered spell can be dredged instead of the controller drawing")
    void countersBeforeOfferingDrawReplacement() {
        Permanent guardian = addReadyGuardian();
        guardian.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 1);
        Darkblast darkblast = new Darkblast();
        harness.setHand(player2, List.of(darkblast));
        harness.setGraveyard(player2, List.of());
        harness.setLibrary(player2, List.of(new Forest(), new Forest(), new Forest()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.passPriority(player1);
        harness.castInstant(player2, 0, guardian.getId());
        harness.activateAbility(player1, 0, null, darkblast.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.GraveyardChoice.class);
        harness.assertInGraveyard(player2, "Darkblast");
        harness.handleGraveyardCardChosen(player2, 0);
        harness.assertInHand(player2, "Darkblast");
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(3);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Can remove a +1/+1 counter while tapped and summoning sick")
    void canSpendPlusCounterWithoutTapping() {
        Permanent guardian = harness.addToBattlefieldAndReturn(player1, new GlenElendraGuardian());
        guardian.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        guardian.setTapped(true);
        guardian.setSummoningSick(true);
        Shock shock = new Shock();
        harness.setHand(player2, List.of(shock));
        harness.setLibrary(player2, List.of(new Forest()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.passPriority(player1);
        harness.castInstant(player2, 0, player1.getId());
        harness.activateAbility(player1, 0, null, shock.getId());
        assertThat(guardian.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        resolveAllTriggers();

        harness.assertInGraveyard(player2, "Shock");
        harness.assertInHand(player2, "Forest");
        assertThat(guardian.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Cannot activate without a counter")
    void cannotActivateWithoutCounter() {
        addReadyGuardian();
        Shock shock = new Shock();
        harness.setHand(player2, List.of(shock));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, player1.getId());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, shock.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("A second activation fizzles after the first removes their shared target")
    void illegalTargetDoesNotDrawAnotherCard() {
        Permanent guardian = addReadyGuardian();
        guardian.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 2);
        Shock shock = new Shock();
        harness.setHand(player2, List.of(shock));
        harness.setLibrary(player2, List.of(new Forest(), new Forest()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.BLUE, 4);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, player1.getId());
        harness.activateAbility(player1, 0, null, shock.getId());
        harness.activateAbility(player1, 0, null, shock.getId());
        resolveAllTriggers();

        assertThat(guardian.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isZero();
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(1);
        harness.assertInGraveyard(player2, "Shock");
    }

    @Test
    @DisplayName("The controller still draws when the targeted spell cannot be countered")
    void uncounterableSpellStillDraws() {
        Permanent guardian = addReadyGuardian();
        guardian.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 1);
        GlenElendrasAnswer answer = new GlenElendrasAnswer();
        harness.setHand(player2, List.of(answer));
        harness.setLibrary(player2, List.of(new Forest()));
        harness.addMana(player2, ManaColor.BLUE, 4);
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.passPriority(player1);
        harness.castInstant(player2, 0);
        harness.activateAbility(player1, 0, null, answer.getId());
        harness.passBothPriorities();

        harness.assertInHand(player2, "Forest");
        assertThat(gd.stack).hasSize(1);
        harness.assertNotInGraveyard(player2, "Glen Elendra's Answer");
    }

    private Permanent addReadyGuardian() {
        return addCreatureReady(player1, new GlenElendraGuardian());
    }
}

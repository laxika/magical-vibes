package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.h.HolyDay;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DoseOfDawnglow.class, HillGiant.class, HolyDay.class})
class DoseOfDawnglowTest extends BaseCardTest {

    @Test
    @DisplayName("Returns a creature from the graveyard without blighting during your main phase")
    void returnsCreatureWithoutBlightDuringYourMainPhase() {
        Card creature = new HillGiant();
        harness.setGraveyard(player1, List.of(creature));
        harness.setHand(player1, List.of(new DoseOfDawnglow()));
        harness.addMana(player1, ManaColor.BLACK, 5);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castAndResolveInstant(player1, 0, creature.getId());

        Permanent returned = findPermanent(player1, "Hill Giant");
        assertThat(returned.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Blights the returned creature outside your main phase")
    void blightsReturnedCreatureOutsideYourMainPhase() {
        Card creature = new HillGiant();
        harness.setGraveyard(player1, List.of(creature));
        harness.setHand(player1, List.of(new DoseOfDawnglow()));
        harness.addMana(player1, ManaColor.BLACK, 5);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);

        harness.castAndResolveInstant(player1, 0, creature.getId());

        Permanent returned = findPermanent(player1, "Hill Giant");
        assertThat(returned.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Cannot target a noncreature card in a graveyard")
    void cannotTargetNoncreatureCard() {
        Card noncreature = new HolyDay();
        harness.setGraveyard(player1, List.of(noncreature));
        harness.setHand(player1, List.of(new DoseOfDawnglow()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, noncreature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void returnsCreatureWithoutBlightDuringYourSecondMainPhase() {
        Card creature = new HillGiant();
        harness.setGraveyard(player1, List.of(creature));
        harness.setHand(player1, List.of(new DoseOfDawnglow()));
        harness.addMana(player1, ManaColor.BLACK, 5);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);

        harness.castAndResolveInstant(player1, 0, creature.getId());

        harness.assertNotInGraveyard(player1, "Hill Giant");
        assertThat(findPermanent(player1, "Hill Giant").getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isZero();
    }

    @Test
    void blightsDuringOpponentsMainPhase() {
        Card creature = new HillGiant();
        harness.setGraveyard(player1, List.of(creature));
        harness.setHand(player1, List.of(new DoseOfDawnglow()));
        harness.addMana(player1, ManaColor.BLACK, 5);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castAndResolveInstant(player1, 0, creature.getId());

        assertThat(findPermanent(player1, "Hill Giant").getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(2);
    }

    @Test
    void canBlightAnotherControlledCreatureInsteadOfReturnedCreature() {
        Permanent existing = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        Permanent opponent = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        Card creature = new HillGiant();
        harness.setGraveyard(player1, List.of(creature));
        harness.setHand(player1, List.of(new DoseOfDawnglow()));
        harness.addMana(player1, ManaColor.BLACK, 5);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.END_STEP);

        harness.castAndResolveInstant(player1, 0, creature.getId());
        harness.handlePermanentChosen(player1, existing.getId());

        assertThat(existing.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(2);
        assertThat(opponent.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isZero();
        assertThat(findPermanents(player1, "Hill Giant")).hasSize(2);
        assertThat(findPermanents(player1, "Hill Giant").stream()
                .filter(p -> !p.getId().equals(existing.getId())).findFirst().orElseThrow()
                .getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isZero();
    }

    @Test
    void cannotTargetCreatureInOpponentsGraveyard() {
        Card creature = new HillGiant();
        harness.setGraveyard(player2, List.of(creature));
        harness.setHand(player1, List.of(new DoseOfDawnglow()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void doesNotBlightWhenOnlyTargetLeavesGraveyardBeforeResolution() {
        Permanent existing = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        Card creature = new HillGiant();
        harness.setGraveyard(player1, List.of(creature));
        harness.setHand(player1, List.of(new DoseOfDawnglow()));
        harness.addMana(player1, ManaColor.BLACK, 5);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.END_STEP);

        harness.castInstant(player1, 0, creature.getId());
        harness.setGraveyard(player1, List.of());
        harness.setExile(player1, List.of(creature));
        harness.passBothPriorities();

        assertThat(existing.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isZero();
        assertThat(countPermanents(player1, "Hill Giant")).isEqualTo(1);
        harness.assertInGraveyard(player1, "Dose of Dawnglow");
    }
}

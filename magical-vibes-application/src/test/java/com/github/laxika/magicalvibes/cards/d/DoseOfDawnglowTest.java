package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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

        harness.castInstant(player1, 0, creature.getId());
        harness.passBothPriorities();

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

        harness.castInstant(player1, 0, creature.getId());
        harness.passBothPriorities();

        Permanent returned = findPermanent(player1, "Hill Giant");
        assertThat(returned.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Cannot target a noncreature card in a graveyard")
    void cannotTargetNoncreatureCard() {
        Card noncreature = new com.github.laxika.magicalvibes.cards.h.HolyDay();
        harness.setGraveyard(player1, List.of(noncreature));
        harness.setHand(player1, List.of(new DoseOfDawnglow()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, noncreature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}

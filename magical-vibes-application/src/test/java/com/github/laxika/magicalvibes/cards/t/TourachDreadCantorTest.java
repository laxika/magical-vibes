package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.d.Distress;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TourachDreadCantor.class, Distress.class, GrizzlyBears.class})
class TourachDreadCantorTest extends BaseCardTest {

    @Test
    @DisplayName("An opponent discarding a card puts a +1/+1 counter on Tourach")
    void opponentDiscardAddsCounter() {
        Permanent tourach = harness.addToBattlefieldAndReturn(player1, new TourachDreadCantor());
        harness.setHand(player2, new ArrayList<>(List.of(new GrizzlyBears())));
        harness.setHand(player1, List.of(new Distress()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(tourach.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("A kicked Tourach makes the target opponent discard two cards at random")
    void kickedTourachDiscardsTwoCards() {
        harness.setHand(player1, List.of(new TourachDreadCantor()));
        harness.setHand(player2, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castKickedCreature(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2);
    }

    @Test
    @DisplayName("A non-kicked Tourach does not require an ETB target")
    void nonKickedTourachDoesNotRequireTarget() {
        harness.setHand(player1, List.of(new TourachDreadCantor()));
        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        harness.assertOnBattlefield(player1, "Tourach, Dread Cantor");
    }

    @Test
    @DisplayName("A kicked Tourach cannot target its controller")
    void kickedTourachCannotTargetController() {
        harness.setHand(player1, List.of(new TourachDreadCantor()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        assertThatThrownBy(() -> harness.castKickedCreature(player1, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an opponent");
    }
}

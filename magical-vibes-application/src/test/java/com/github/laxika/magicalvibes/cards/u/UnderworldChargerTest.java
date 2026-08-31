package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
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

@CardUsed({UnderworldCharger.class, GrizzlyBears.class})
class UnderworldChargerTest extends BaseCardTest {

    @Test
    @DisplayName("Cast from hand enters without +1/+1 counters")
    void castFromHandHasNoCounters() {
        harness.setHand(player1, List.of(new UnderworldCharger()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent charger = findPermanent(player1, "Underworld Charger");
        assertThat(charger.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(charger.getEffectivePower()).isEqualTo(3);
        assertThat(charger.getEffectiveToughness()).isEqualTo(3);
    }

    @Test
    @DisplayName("Escape exiles three other graveyard cards and enters with two +1/+1 counters")
    void escapeExilesOtherCardsAndEntersWithCounters() {
        UnderworldCharger charger = new UnderworldCharger();
        harness.setGraveyard(player1, List.of(charger, new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castFromGraveyard(player1, 0, List.of(1, 2, 3));
        harness.passBothPriorities();

        Permanent escapedCharger = findPermanent(player1, "Underworld Charger");
        assertThat(escapedCharger.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(escapedCharger.getEffectivePower()).isEqualTo(5);
        assertThat(escapedCharger.getEffectiveToughness()).isEqualTo(5);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).hasSize(3);
    }

    @Test
    @DisplayName("Escape requires three other cards in the graveyard")
    void escapeRequiresThreeOtherCards() {
        UnderworldCharger charger = new UnderworldCharger();
        harness.setGraveyard(player1, List.of(charger, new GrizzlyBears(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.castFromGraveyard(player1, 0, List.of(0, 1)))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInGraveyard(player1, "Underworld Charger");
    }

    @Test
    @DisplayName("Underworld Charger cannot be declared as a blocker")
    void cannotBeDeclaredAsBlocker() {
        Permanent charger = addCreatureReady(player2, new UnderworldCharger());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setAttacking(true);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(charger), 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid blocker index");
    }
}

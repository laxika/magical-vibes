package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ElementalistAdept.class, GrizzlyBears.class, Shock.class})
class ElementalistAdeptTest extends BaseCardTest {

    private Permanent addAdept() {
        Permanent adept = harness.addToBattlefieldAndReturn(player1, new ElementalistAdept());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        return adept;
    }

    private void endTurn() {
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Casting a noncreature spell gives +1/+1 until end of turn")
    void noncreatureSpellPumps() {
        Permanent adept = addAdept();

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());

        assertThat(gd.stack.stream()
                .filter(e -> e.getEntryType() == StackEntryType.TRIGGERED_ABILITY)
                .count()).isEqualTo(1);

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, adept)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, adept)).isEqualTo(2);
    }

    @Test
    @DisplayName("Casting a creature spell does not trigger prowess")
    void creatureSpellDoesNotPump() {
        Permanent adept = addAdept();

        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
        assertThat(gqs.getEffectivePower(gd, adept)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, adept)).isEqualTo(1);
    }

    @Test
    @DisplayName("Opponent casting a noncreature spell does not trigger prowess")
    void opponentNoncreatureSpellDoesNotPump() {
        Permanent adept = addAdept();
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, player1.getId());

        assertThat(gd.stack.stream()
                .filter(e -> e.getEntryType() == StackEntryType.TRIGGERED_ABILITY)
                .count()).isZero();
        assertThat(gqs.getEffectivePower(gd, adept)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, adept)).isEqualTo(1);
    }

    @Test
    @DisplayName("The prowess boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        Permanent adept = addAdept();

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, adept)).isEqualTo(3);

        endTurn();

        assertThat(gqs.getEffectivePower(gd, adept)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, adept)).isEqualTo(1);
    }

    @Test
    @DisplayName("Flash allows casting on an opponent's turn in response to a spell")
    void flashAllowsResponseOnOpponentsTurn() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.setHand(player1, List.of(new ElementalistAdept()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castInstant(player2, 0, player1.getId());
        harness.passPriority(player2);
        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Elementalist Adept");
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Each noncreature spell adds a separate prowess boost")
    void repeatedSpellsStackProwessBoosts() {
        Permanent adept = addAdept();
        harness.setHand(player1, List.of(new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castInstant(player1, 0, player2.getId());
        assertThat(gqs.getEffectivePower(gd, adept)).isEqualTo(2);
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, adept)).isEqualTo(3);
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, adept)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, adept)).isEqualTo(3);
        endTurn();
        assertThat(gqs.getEffectivePower(gd, adept)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, adept)).isEqualTo(1);
    }
}

package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.h.Humble;
import com.github.laxika.magicalvibes.cards.s.Shock;
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

@CardUsed({GoldnightCastigator.class, HillGiant.class, Shock.class, Humble.class})
class GoldnightCastigatorTest extends BaseCardTest {

    @Test
    @DisplayName("Doubles spell damage dealt to its controller")
    void doublesSpellDamageToController() {
        harness.addToBattlefield(player1, new GoldnightCastigator());
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.setLife(player1, 20);

        harness.forceActivePlayer(player2);
        harness.castInstant(player2, 0, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(16);
    }

    @Test
    @DisplayName("Doubles spell damage dealt to itself")
    void doublesSpellDamageToSelf() {
        Permanent castigator = harness.addToBattlefieldAndReturn(player1, new GoldnightCastigator());
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.forceActivePlayer(player2);
        harness.castInstant(player2, 0, castigator.getId());
        harness.passBothPriorities();

        assertThat(castigator.getMarkedDamage()).isEqualTo(4);
    }

    @Test
    @DisplayName("Does not double damage dealt to another permanent its controller controls")
    void doesNotDoubleDamageToAnotherPermanent() {
        harness.addToBattlefield(player1, new GoldnightCastigator());
        Permanent otherCreature = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.forceActivePlayer(player2);
        harness.castInstant(player2, 0, otherCreature.getId());
        harness.passBothPriorities();

        assertThat(otherCreature.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("Doubles combat damage dealt to its controller")
    void doublesCombatDamageToController() {
        harness.addToBattlefield(player1, new GoldnightCastigator());
        Permanent attacker = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        attacker.setSummoningSick(false);
        harness.setLife(player1, 20);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        gs.declareAttackers(gd, player2, List.of(0));

        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();
        gs.declareBlockers(gd, player1, List.<BlockerAssignment>of());
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(14);
    }

    @Test
    @DisplayName("Doubles combat damage dealt to itself")
    void doublesCombatDamageToSelf() {
        harness.addToBattlefield(player1, new GoldnightCastigator());
        Permanent attacker = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        attacker.setSummoningSick(false);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        gs.declareAttackers(gd, player2, List.of(0));

        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();
        gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        Permanent castigator = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(castigator.getMarkedDamage()).isEqualTo(6);
    }

    @Test
    void multipleCastigatorsQuadrupleDamageToController() {
        harness.addToBattlefield(player1, new GoldnightCastigator());
        harness.addToBattlefield(player1, new GoldnightCastigator());
        harness.setLife(player1, 20);
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.forceActivePlayer(player2);

        harness.castInstant(player2, 0, player1.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 12);
    }

    @Test
    void anotherCastigatorDoesNotDoubleDamageToSelfAgain() {
        Permanent castigator = harness.addToBattlefieldAndReturn(player1, new GoldnightCastigator());
        harness.addToBattlefield(player1, new GoldnightCastigator());
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.forceActivePlayer(player2);

        harness.castInstant(player2, 0, castigator.getId());
        harness.passBothPriorities();

        assertThat(castigator.getMarkedDamage()).isEqualTo(4);
    }

    @Test
    void doesNotDoubleDamageToOpponent() {
        harness.addToBattlefield(player1, new GoldnightCastigator());
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 18);
    }

    @Test
    void losingAbilitiesStopsDoublingDamageToController() {
        Permanent castigator = harness.addToBattlefieldAndReturn(player1, new GoldnightCastigator());
        harness.setLife(player1, 20);
        harness.setHand(player2, List.of(new Humble(), new Shock()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.addMana(player2, ManaColor.RED, 1);
        harness.forceActivePlayer(player2);

        harness.castInstant(player2, 0, castigator.getId());
        harness.passBothPriorities();
        harness.castInstant(player2, 0, player1.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 18);
    }

    @Test
    void losingAbilitiesStopsDoublingDamageToSelf() {
        Permanent castigator = harness.addToBattlefieldAndReturn(player1, new GoldnightCastigator());
        castigator.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 4);
        harness.setHand(player2, List.of(new Humble(), new Shock()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.addMana(player2, ManaColor.RED, 1);
        harness.forceActivePlayer(player2);

        harness.castInstant(player2, 0, castigator.getId());
        harness.passBothPriorities();
        harness.castInstant(player2, 0, castigator.getId());
        harness.passBothPriorities();

        assertThat(castigator.getMarkedDamage()).isEqualTo(2);
    }
}

package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.b.Blaze;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.t.TurnToFrog;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

@CardUsed({AngrathsMarauders.class, Blaze.class, GrizzlyBears.class, SerraAngel.class, Shock.class, TurnToFrog.class})
class AngrathsMaraudersTest extends BaseCardTest {

    @Test
    @DisplayName("Doubles Shock damage to a player")
    void doublesSpellDamageToPlayer() {
        harness.addToBattlefield(player1, new AngrathsMarauders());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.setLife(player2, 20);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        // 2 damage doubled to 4
        harness.assertLife(player2, 16);
    }

    @Test
    @DisplayName("Doubled spell damage destroys a creature that would survive base damage")
    void doublesSpellDamageToCreature() {
        harness.addToBattlefield(player1, new AngrathsMarauders());
        harness.addToBattlefield(player2, new SerraAngel()); // 4/4
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        UUID serraId = harness.getPermanentId(player2, "Serra Angel");
        harness.castInstant(player1, 0, serraId);
        harness.passBothPriorities();

        // 2 damage doubled to 4 — kills Serra Angel (4/4)
        harness.assertNotOnBattlefield(player2, "Serra Angel");
        harness.assertInGraveyard(player2, "Serra Angel");
    }

    @Test
    @DisplayName("Doubles unblocked combat damage to player")
    void doublesUnblockedCombatDamage() {
        harness.setLife(player2, 20);
        harness.addToBattlefield(player1, new AngrathsMarauders());

        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        bear.setSummoningSick(false);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();

        gs.declareAttackers(gd, player1, List.of(1)); // bear is at index 1 (Marauders at 0)

        // 2 combat damage doubled to 4
        harness.assertLife(player2, 16);
    }

    @Test
    @DisplayName("Doubled combat damage kills blocker that would survive base damage")
    void doublesCombatDamageKillsBlocker() {
        harness.addToBattlefield(player1, new AngrathsMarauders());

        // 2/2 attacker
        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);

        // 4/4 blocker — base 2 damage wouldn't kill it, but doubled 4 does
        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new SerraAngel());
        blocker.setSummoningSick(false);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 1)));
        harness.passBothPriorities();

        // Serra Angel (4/4) takes 2*2=4 doubled damage — exactly lethal
        harness.assertInGraveyard(player2, "Serra Angel");
    }

    @Test
    @DisplayName("Does not double opponent's spell damage")
    void doesNotDoubleOpponentsSpellDamage() {
        harness.addToBattlefield(player1, new AngrathsMarauders());
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.setLife(player1, 20);

        harness.forceActivePlayer(player2);
        harness.castInstant(player2, 0, player1.getId());
        harness.passBothPriorities();

        // 2 damage — NOT doubled (opponent's spell, not Marauders controller's)
        harness.assertLife(player1, 18);
    }

    @Test
    @DisplayName("Opponent's Marauders does not double your combat damage")
    void opponentsMaraudersDoesNotDoubleYourCombatDamage() {
        harness.setLife(player2, 20);
        harness.addToBattlefield(player2, new AngrathsMarauders());

        // Player1's creature attacks — should NOT be doubled by opponent's Marauders
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        bear.setSummoningSick(false);
        bear.setAttacking(true);

        // Defender has a creature (Marauders) so combat pauses for blockers
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        gs.declareBlockers(gd, player2, List.of()); // no blockers
        harness.passBothPriorities();

        // 2 combat damage — NOT doubled (opponent's Marauders, not yours)
        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("Two Angrath's Marauders quadruple spell damage")
    void twoMaraudersQuadrupleSpellDamage() {
        harness.addToBattlefield(player1, new AngrathsMarauders());
        harness.addToBattlefield(player1, new AngrathsMarauders());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.setLife(player2, 20);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        // 2 * 2 * 2 = 8 damage
        harness.assertLife(player2, 12);
    }

    @Test
    @DisplayName("Two Angrath's Marauders quadruple combat damage")
    void twoMaraudersQuadrupleCombatDamage() {
        harness.setLife(player2, 20);
        harness.addToBattlefield(player1, new AngrathsMarauders());
        harness.addToBattlefield(player1, new AngrathsMarauders());

        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        bear.setSummoningSick(false);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();

        gs.declareAttackers(gd, player1, List.of(2)); // bear at index 2 (two Marauders at 0, 1)

        // 2 combat damage * 4 = 8
        harness.assertLife(player2, 12);
    }

    @Test
    @DisplayName("Removing Angrath's Marauders from battlefield stops doubling")
    void removingStopsDoubling() {
        harness.addToBattlefield(player1, new AngrathsMarauders());
        harness.setLife(player2, 20);

        // Deal doubled damage first
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        // 2 * 2 = 4 damage
        harness.assertLife(player2, 16);

        // Remove Marauders from battlefield
        gd.playerBattlefields.get(player1.getId())
                .removeIf(p -> p.getCard().getName().equals("Angrath's Marauders"));

        // Deal damage again without Marauders
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        // 2 damage (not doubled), life goes from 16 to 14
        harness.assertLife(player2, 14);
    }

    @Test
    @DisplayName("Doubles X damage from Blaze to a player")
    void doublesXDamageFromSorcery() {
        harness.addToBattlefield(player1, new AngrathsMarauders());
        harness.setHand(player1, List.of(new Blaze()));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.setLife(player2, 20);

        harness.castSorcery(player1, 0, 3, player2.getId());
        harness.passBothPriorities();

        // 3 damage doubled to 6
        harness.assertLife(player2, 14);
    }

    @Test
    @DisplayName("Doubles damage to its controller as well as opponents")
    void doublesDamageToController() {
        harness.addToBattlefield(player1, new AngrathsMarauders());
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player1.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 16);
    }

    @Test
    @DisplayName("Doubles damage to friendly permanents including Marauders itself")
    void doublesDamageToItself() {
        Permanent marauders = harness.addToBattlefieldAndReturn(player1, new AngrathsMarauders());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, marauders.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Angrath's Marauders");
        harness.assertNotOnBattlefield(player1, "Angrath's Marauders");
    }

    @Test
    @DisplayName("Doubles its own combat damage")
    void doublesItsOwnCombatDamage() {
        harness.setLife(player2, 20);
        Permanent marauders = harness.addToBattlefieldAndReturn(player1, new AngrathsMarauders());
        marauders.setSummoningSick(false);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();

        gs.declareAttackers(gd, player1, List.of(0));

        harness.assertLife(player2, 12);
    }

    @Test
    @DisplayName("Losing all abilities stops doubling spell damage")
    void losingAbilitiesStopsSpellDoubling() {
        Permanent marauders = harness.addToBattlefieldAndReturn(player1, new AngrathsMarauders());
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new TurnToFrog(), new Shock()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, marauders.getId());
        harness.passBothPriorities();
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("Losing all abilities stops doubling combat damage")
    void losingAbilitiesStopsCombatDoubling() {
        Permanent marauders = harness.addToBattlefieldAndReturn(player1, new AngrathsMarauders());
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new TurnToFrog()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castInstant(player1, 0, marauders.getId());
        harness.passBothPriorities();

        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        bear.setSummoningSick(false);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();

        gs.declareAttackers(gd, player1, List.of(1));

        harness.assertLife(player2, 18);
    }
}

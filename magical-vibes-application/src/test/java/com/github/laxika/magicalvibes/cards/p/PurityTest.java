package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GoldmeadowDodger;
import com.github.laxika.magicalvibes.cards.t.Tarfire;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Purity.class, Tarfire.class, GoldmeadowDodger.class})
class PurityTest extends BaseCardTest {

    @Test
    @DisplayName("Noncombat damage to Purity's controller is prevented and they gain that much life")
    void preventsNoncombatDamageAndGainsLife() {
        harness.setLife(player2, 20);
        harness.addToBattlefield(player2, new Purity());

        harness.setHand(player1, List.of(new Tarfire()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, player2.getId());

        // 2 damage prevented (no life lost) + 2 life gained for the prevented damage.
        harness.assertLife(player2, 22);
    }

    @Test
    @DisplayName("Purity does not prevent combat damage to its controller")
    void doesNotPreventCombatDamage() {
        harness.setLife(player2, 20);
        harness.addToBattlefield(player2, new Purity());

        Permanent attacker = new Permanent(new GoldmeadowDodger());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(attacker);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        harness.assertLife(player2, 19);
    }

    @Test
    @DisplayName("Purity does not prevent noncombat damage dealt to an opponent")
    void doesNotPreventDamageToOpponent() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.addToBattlefield(player2, new Purity());

        harness.setHand(player1, List.of(new Tarfire()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, player1.getId());

        harness.assertLife(player1, 18);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Purity's prevention stops applying when it has lost its abilities")
    void abilityLossDisablesPrevention() {
        harness.setLife(player2, 20);
        Permanent purity = harness.addToBattlefieldAndReturn(player2, new Purity());
        purity.setLosesAllAbilitiesUntilEndOfTurn(true);

        harness.setHand(player1, List.of(new Tarfire()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, player2.getId());

        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("When Purity is put into the graveyard it enters first, then a triggered ability shuffles it into its library")
    void diesThenTriggerShufflesIntoLibrary() {
        harness.setLibrary(player2, new java.util.ArrayList<>());
        Permanent purity = harness.addToBattlefieldAndReturn(player2, new Purity());
        // Mark lethal damage (6/6) and let state-based actions destroy it.
        purity.setMarkedDamage(6);

        harness.runStateBasedActions();

        // Triggered ability (not a replacement): Purity actually enters the graveyard and its
        // "put into graveyard from anywhere" ability is waiting on the stack.
        harness.assertNotOnBattlefield(player2, "Purity");
        harness.assertInGraveyard(player2, "Purity");
        assertThat(gd.stack).isNotEmpty();

        harness.passBothPriorities();

        // After the trigger resolves, Purity is shuffled into its owner's library.
        harness.assertNotInGraveyard(player2, "Purity");
        assertThat(gd.playerDecks.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Purity"));
    }
}

package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

@CardUsed({UrzasArmor.class, Shock.class, HillGiant.class})
class UrzasArmorTest extends BaseCardTest {

    @Test
    @DisplayName("Prevents 1 of a noncombat damage source to the controller")
    void preventsOneNoncombatDamage() {
        harness.addToBattlefield(player1, new UrzasArmor());
        harness.setLife(player1, 20);
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castAndResolveInstant(player2, 0, player1.getId());

        // Shock deals 2; 1 is prevented, so player1 takes 1.
        harness.assertLife(player1, 19);
    }

    @Test
    @DisplayName("Two copies each prevent 1 (stacking)")
    void twoCopiesStack() {
        harness.addToBattlefield(player1, new UrzasArmor());
        harness.addToBattlefield(player1, new UrzasArmor());
        harness.setLife(player1, 20);
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castAndResolveInstant(player2, 0, player1.getId());

        // Shock deals 2; both copies prevent 1 each, so all of it is prevented.
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Only the controller is protected, not their opponent")
    void opponentDamageIsNotPrevented() {
        harness.addToBattlefield(player1, new UrzasArmor());
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, player2.getId());

        // player2 controls no Urza's Armor, so the full 2 damage lands.
        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("Prevents 1 of each attacker's combat damage to the controller")
    void preventsOneCombatDamage() {
        harness.addToBattlefield(player1, new UrzasArmor());
        harness.setLife(player1, 20);

        addCreatureReady(player2, new HillGiant());
        declareAttackers(player2, List.of(0));
        resolveCombat(player2);

        // Hill Giant deals 3; 1 is prevented, so player1 takes 2.
        harness.assertLife(player1, 18);
    }

    @Test
    @DisplayName("Prevents 1 separately from each combat damage source")
    void preventsOneFromEachCombatSource() {
        harness.addToBattlefield(player1, new UrzasArmor());
        harness.setLife(player1, 20);

        addCreatureReady(player2, new HillGiant());
        addCreatureReady(player2, new HillGiant());
        declareAttackers(player2, List.of(0, 1));
        resolveCombat(player2);

        // Each Hill Giant deals 3; 1 is prevented from each source, so player1 takes 4 total.
        harness.assertLife(player1, 16);
    }
}

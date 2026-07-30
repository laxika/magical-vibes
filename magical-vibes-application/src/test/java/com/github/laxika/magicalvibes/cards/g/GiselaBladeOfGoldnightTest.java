package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.b.Blaze;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class GiselaBladeOfGoldnightTest extends BaseCardTest {

    @Test
    @DisplayName("Doubles spell damage dealt to an opponent")
    void doublesSpellDamageToOpponent() {
        harness.addToBattlefield(player1, new GiselaBladeOfGoldnight());
        harness.setHand(player1, List.of(new Blaze()));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.setLife(player2, 20);

        harness.castSorcery(player1, 0, 3, player2.getId());
        harness.passBothPriorities();

        // 3 damage doubled to 6
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(14);
    }

    @Test
    @DisplayName("Doubles spell damage dealt to a permanent an opponent controls")
    void doublesSpellDamageToOpponentsPermanent() {
        harness.addToBattlefield(player1, new GiselaBladeOfGoldnight());
        harness.addToBattlefield(player2, new SerraAngel()); // 4/4
        harness.setHand(player1, List.of(new Blaze()));
        harness.addMana(player1, ManaColor.RED, 3);

        UUID serraId = harness.getPermanentId(player2, "Serra Angel");
        harness.castSorcery(player1, 0, 2, serraId);
        harness.passBothPriorities();

        // 2 damage doubled to 4 — exactly lethal for a 4/4
        harness.assertInGraveyard(player2, "Serra Angel");
    }

    @Test
    @DisplayName("Prevents half the damage dealt to Gisela's controller, rounded up")
    void preventsHalfDamageToController() {
        harness.addToBattlefield(player1, new GiselaBladeOfGoldnight());
        harness.setHand(player2, List.of(new Blaze()));
        harness.addMana(player2, ManaColor.RED, 6);
        harness.setLife(player1, 20);

        harness.forceActivePlayer(player2);
        harness.castSorcery(player2, 0, 5, player1.getId());
        harness.passBothPriorities();

        // 5 damage: 3 prevented (half rounded up), 2 dealt
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Prevents half the damage dealt to a permanent Gisela's controller controls")
    void preventsHalfDamageToOwnPermanent() {
        harness.addToBattlefield(player1, new GiselaBladeOfGoldnight());
        harness.addToBattlefield(player1, new SerraAngel()); // 4/4
        harness.setHand(player2, List.of(new Blaze()));
        harness.addMana(player2, ManaColor.RED, 8);

        UUID serraId = harness.getPermanentId(player1, "Serra Angel");
        harness.forceActivePlayer(player2);
        harness.castSorcery(player2, 0, 7, serraId);
        harness.passBothPriorities();

        // 7 damage: 4 prevented (half rounded up), 3 dealt — the 4/4 survives
        harness.assertOnBattlefield(player1, "Serra Angel");
    }

    @Test
    @DisplayName("Doubling and halving do not both apply to the same damage event")
    void doublingDoesNotApplyToControllersOwnDamage() {
        harness.addToBattlefield(player2, new GiselaBladeOfGoldnight());
        harness.setHand(player2, List.of(new Blaze()));
        harness.addMana(player2, ManaColor.RED, 5);
        harness.setLife(player2, 20);

        // Player 2 burns themselves: only their own prevention applies, never the doubling.
        harness.forceActivePlayer(player2);
        harness.castSorcery(player2, 0, 4, player2.getId());
        harness.passBothPriorities();

        // 4 damage: 2 prevented, 2 dealt
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Doubles combat damage dealt to an opponent")
    void doublesCombatDamageToOpponent() {
        harness.setLife(player2, 20);
        harness.addToBattlefield(player1, new GiselaBladeOfGoldnight());
        addCreatureReady(player1, new GrizzlyBears()); // 2/2

        declareAttackers(player1, List.of(1)); // Gisela at 0, bears at 1

        // 2 combat damage doubled to 4
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
    }

    @Test
    @DisplayName("Prevents half the combat damage dealt to Gisela's controller")
    void preventsHalfCombatDamageToController() {
        harness.setLife(player1, 20);
        harness.addToBattlefield(player1, new GiselaBladeOfGoldnight());
        GrizzlyBears bigBears = new GrizzlyBears();
        bigBears.setPower(5);
        bigBears.setToughness(5);
        addCreatureReady(player2, bigBears);

        declareAttackers(player2, List.of(0));
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();
        gs.declareBlockers(gd, player1, List.of());
        harness.passBothPriorities();

        // 5 combat damage: 3 prevented (half rounded up), 2 dealt
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("No doubling or prevention once Gisela leaves the battlefield")
    void effectStopsWhenGiselaLeaves() {
        harness.addToBattlefield(player1, new GiselaBladeOfGoldnight());
        harness.setLife(player2, 20);
        gd.playerBattlefields.get(player1.getId())
                .removeIf(p -> p.getCard().getName().equals("Gisela, Blade of Goldnight"));

        harness.setHand(player1, List.of(new Blaze()));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.castSorcery(player1, 0, 3, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
    }
}

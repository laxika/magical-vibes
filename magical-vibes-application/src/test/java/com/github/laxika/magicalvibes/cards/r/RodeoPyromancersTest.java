package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RodeoPyromancers.class, Shock.class})
class RodeoPyromancersTest extends BaseCardTest {

    @Test
    @DisplayName("Adds two red mana when you cast your first spell each turn")
    void addsManaForFirstSpellEachTurn() {
        harness.addToBattlefield(player1, new RodeoPyromancers());
        harness.setHand(player1, List.of(new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(4);
    }

    @Test
    @DisplayName("Does not add mana for a later spell in the same turn")
    void onlyTriggersForFirstSpellEachTurn() {
        harness.addToBattlefield(player1, new RodeoPyromancers());
        harness.setHand(player1, List.of(new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(3);
    }

    @Test
    @DisplayName("Mana is added when the trigger resolves, before the spell resolves")
    void manaTriggerUsesTheStack() {
        harness.addToBattlefield(player1, new RodeoPyromancers());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
        harness.assertLife(player2, 20);

        harness.passBothPriorities();

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(2);
        harness.assertLife(player2, 20);

        harness.passBothPriorities();
        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("Opponent spells neither trigger the ability nor consume your first spell")
    void opponentSpellDoesNotCountForController() {
        harness.addToBattlefield(player1, new RodeoPyromancers());
        harness.setHand(player2, List.of(new Shock()));
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player2, 0, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.RED)).isZero();
        harness.assertLife(player1, 18);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(2);
    }

    @Test
    @DisplayName("The first-spell count resets on the opponent's turn")
    void triggersAgainOnOpponentsTurn() {
        harness.addToBattlefield(player1, new RodeoPyromancers());
        harness.setHand(player1, List.of(new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.passUntilWithNoAttackers(player2, TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(2);
    }

    @Test
    @DisplayName("Casting Pyromancers first does not trigger itself or a later spell")
    void doesNotTriggerWhenEnteringAfterFirstSpellWasCast() {
        harness.setHand(player1, List.of(new RodeoPyromancers(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Rodeo Pyromancers");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
        harness.assertLife(player2, 18);
    }

    @Test
    @CardUsed({RodeoPyromancers.class})
    @DisplayName("Each Pyromancers triggers for the first creature spell")
    void multipleCopiesTriggerForCreatureSpell() {
        harness.addToBattlefield(player1, new RodeoPyromancers());
        harness.addToBattlefield(player1, new RodeoPyromancers());
        harness.setHand(player1, List.of(new RodeoPyromancers()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(4);

        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(3);
    }
}

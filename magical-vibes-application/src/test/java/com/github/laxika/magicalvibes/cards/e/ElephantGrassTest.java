package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.r.RiverBoa;
import com.github.laxika.magicalvibes.cards.u.UrborgMindsucker;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ElephantGrass.class, RiverBoa.class, UrborgMindsucker.class})
class ElephantGrassTest extends BaseCardTest {

    @Test
    @DisplayName("Cumulative upkeep sacrifices Elephant Grass when the cost is not paid")
    void cumulativeUpkeepSacrifices() {
        harness.addToBattlefield(player1, new ElephantGrass());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertNotOnBattlefield(player1, "Elephant Grass");
        harness.assertInGraveyard(player1, "Elephant Grass");
    }

    @Test
    @DisplayName("Paying cumulative upkeep keeps Elephant Grass")
    void payingCumulativeUpkeepKeepsGrass() {
        Permanent grass = harness.addToBattlefieldAndReturn(player1, new ElephantGrass());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(grass);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(0);
    }

    @Test
    @DisplayName("Cumulative upkeep costs one mana for each age counter")
    void cumulativeUpkeepCostScalesWithAgeCounters() {
        Permanent grass = harness.addToBattlefieldAndReturn(player1, new ElephantGrass());
        grass.setCounterCount(CounterType.AGE, 1);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(grass.getCounterCount(CounterType.AGE)).isEqualTo(2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(grass);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Black creatures can't attack the controller")
    void blackCreatureCantAttackController() {
        harness.addToBattlefield(player1, new ElephantGrass());
        addCreatureReady(player2, new UrborgMindsucker());
        // Enough mana that the attack tax is affordable — the hard deny must still reject.
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> declareAttackers(player2, List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
    }

    @Test
    @DisplayName("A creature can attack the controller's planeswalker without paying")
    void creatureCanAttackControllersPlaneswalker() {
        harness.addToBattlefield(player1, new ElephantGrass());
        Permanent planeswalker = addPlaneswalker(player1, 4);
        addCreatureReady(player2, new UrborgMindsucker());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        gs.declareAttackers(gd, player2, List.of(0), Map.of(0, planeswalker.getId()));

        assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isZero();
        assertThat(planeswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(2);
    }

    @Test
    @DisplayName("Nonblack creature can attack the controller by paying {2}")
    void nonblackPaysTwoToAttack() {
        harness.addToBattlefield(player1, new ElephantGrass());
        addCreatureReady(player2, new RiverBoa());
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        declareAttackers(player2, List.of(0));

        assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isEqualTo(0);
        assertThat(gd.getLife(player1.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Nonblack creatures pay {2} for each creature attacking the controller")
    void nonblackPaysTwoForEachAttacker() {
        harness.addToBattlefield(player1, new ElephantGrass());
        addCreatureReady(player2, new RiverBoa());
        addCreatureReady(player2, new RiverBoa());
        harness.addMana(player2, ManaColor.COLORLESS, 4);

        declareAttackers(player2, List.of(0, 1));

        assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isZero();
        assertThat(gd.getLife(player1.getId())).isEqualTo(16);
    }

    @Test
    @DisplayName("Nonblack creature cannot attack without paying {2}")
    void nonblackCannotAttackWithoutPayment() {
        harness.addToBattlefield(player1, new ElephantGrass());
        addCreatureReady(player2, new RiverBoa());
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> declareAttackers(player2, List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana to pay attack tax");
    }

    private Permanent addPlaneswalker(Player player, int loyalty) {
        Card card = new Card();
        card.setName("Test Planeswalker");
        card.setType(CardType.PLANESWALKER);
        card.setLoyalty(loyalty);

        Permanent planeswalker = new Permanent(card);
        planeswalker.setCounterCount(CounterType.LOYALTY, loyalty);
        gd.playerBattlefields.get(player.getId()).add(planeswalker);
        return planeswalker;
    }
}

package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ElephantGrassTest extends BaseCardTest {

    private void beginAttack(Player attacker) {
        harness.forceActivePlayer(attacker);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
    }

    private Permanent addReadyCreature(Player player, CardColor color) {
        Card card = new Card();
        card.setName("Test " + color + " Creature");
        card.setType(CardType.CREATURE);
        card.setColor(color);
        card.setColors(List.of(color));
        card.setPower(2);
        card.setToughness(2);
        return addCreatureReady(player, card);
    }

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
    @DisplayName("Black creatures can't attack the controller")
    void blackCreatureCantAttackController() {
        harness.addToBattlefield(player1, new ElephantGrass());
        addReadyCreature(player2, CardColor.BLACK);
        // Enough mana that the attack tax is affordable — the hard deny must still reject.
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        beginAttack(player2);

        assertThatThrownBy(() -> gs.declareAttackers(gd, player2, List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
    }

    @Test
    @DisplayName("Nonblack creature can attack the controller by paying {2}")
    void nonblackPaysTwoToAttack() {
        harness.addToBattlefield(player1, new ElephantGrass());
        addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        declareAttackers(player2, List.of(0));

        assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isEqualTo(0);
        assertThat(gd.getLife(player1.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Nonblack creature cannot attack without paying {2}")
    void nonblackCannotAttackWithoutPayment() {
        harness.addToBattlefield(player1, new ElephantGrass());
        addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> declareAttackers(player2, List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana to pay attack tax");
    }
}

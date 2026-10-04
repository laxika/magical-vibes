package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GoblinInfluxArray.class, GoblinInstigator.class, GreenwoodSentinel.class})
class GoblinInfluxArrayTest extends BaseCardTest {

    private static final Set<String> SPELLBOOK = Set.of(
            "Goblin Warchief", "Goblin Chieftain", "Skirk Prospector", "Brash Taunter",
            "Wily Goblin", "Goblin Trashmaster", "Ember Hauler", "Relic Robber",
            "Fanatical Firebrand", "Goblin Arsonist", "Reckless Ringleader", "Battle Cry Goblin",
            "Beetleback Chief", "Goblin Instigator", "Legion Warboss");

    @Test
    @DisplayName("Goblin spells lose a red mana from their cost")
    void reducesGoblinSpellCost() {
        harness.addToBattlefield(player1, new GoblinInfluxArray());
        harness.castFromHand(player1, new GoblinInstigator(), "{1}");

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Goblin Instigator");
    }

    @Test
    @DisplayName("At the controller's end step, the array conjures a spellbook card")
    void conjuresAtEndStep() {
        harness.forceActivePlayer(player1);
        harness.addToBattlefield(player1, new GoblinInfluxArray());
        harness.setHand(player1, List.of());

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId()))
                .hasSize(1)
                .extracting(Card::getName)
                .allMatch(SPELLBOOK::contains);
    }

    @Test
    void multipleArraysReduceRemainingGenericCost() {
        harness.addToBattlefield(player1, new GoblinInfluxArray());
        harness.addToBattlefield(player1, new GoblinInfluxArray());

        harness.castFromHand(player1, new GoblinInstigator(), "");

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    void doesNotReduceNonGoblinSpellCost() {
        harness.addToBattlefield(player1, new GoblinInfluxArray());
        harness.setHand(player1, List.of(new GreenwoodSentinel()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
        assertThat(gd.stack).isEmpty();
        harness.assertInHand(player1, "Greenwood Sentinel");
    }

    @Test
    void doesNotReduceOpponentsGoblinSpellCost() {
        harness.addToBattlefield(player1, new GoblinInfluxArray());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(new GoblinInstigator()));
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.ensurePriority(player2);

        assertThatThrownBy(() -> harness.castCreature(player2, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void doesNotConjureDuringOpponentsEndStep() {
        harness.addToBattlefield(player1, new GoblinInfluxArray());
        harness.setHand(player1, List.of());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);

        harness.passUntil(player2, TurnStep.END_STEP);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    void conjureResolvesAfterArrayLeavesBattlefield() {
        harness.forceActivePlayer(player1);
        var array = harness.addToBattlefieldAndReturn(player1, new GoblinInfluxArray());
        harness.setHand(player1, List.of());
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(TurnStep.END_STEP);
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, array));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1)
                .extracting(Card::getName).allMatch(SPELLBOOK::contains);
    }

    @Test
    void conjuredCreatureCountsAsNontokenWhenItDies() {
        harness.forceActivePlayer(player1);
        harness.addToBattlefield(player1, new GoblinInfluxArray());
        harness.setHand(player1, List.of());
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(TurnStep.END_STEP);
        harness.passBothPriorities();
        Card conjured = gd.playerHands.get(player1.getId()).removeFirst();
        var creature = harness.addToBattlefieldAndReturn(player1, conjured);

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .sacrificePermanentToGraveyard(gd, creature));

        assertThat(gd.nontokenCreatureDeathCountThisTurn.getOrDefault(player1.getId(), 0))
                .isEqualTo(1);
        harness.assertInGraveyard(player1, conjured.getName());
    }
}

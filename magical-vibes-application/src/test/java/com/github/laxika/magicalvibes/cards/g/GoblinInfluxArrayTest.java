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

@CardUsed({GoblinInfluxArray.class, GoblinInstigator.class, GrizzlyBears.class})
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
        harness.setHand(player1, List.of(new GoblinInstigator()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);

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
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId()))
                .hasSize(1)
                .allMatch(Card::isTokenCard)
                .extracting(Card::getName)
                .allMatch(SPELLBOOK::contains);
    }
}

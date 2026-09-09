package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ThingFromTheDeep.class, Island.class})
class ThingFromTheDeepTest extends BaseCardTest {

    private void attackWithThing() {
        Permanent thing = addCreatureReady(player1, new ThingFromTheDeep());
        int thingIndex = gd.playerBattlefields.get(player1.getId()).indexOf(thing);
        declareAttackers(List.of(thingIndex));
    }

    @Test
    @DisplayName("Auto-sacrifices when controller has no Island")
    void autoSacrificesWithoutIsland() {
        attackWithThing();
        harness.passBothPriorities(); // resolve attack trigger

        // No Island to pay with, so it's sacrificed automatically with no choice.
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertNotOnBattlefield(player1, "Thing from the Deep");
        harness.assertInGraveyard(player1, "Thing from the Deep");
    }

    @Test
    @DisplayName("Prompts a may ability when controller has an Island")
    void promptsMayAbilityWithIsland() {
        harness.addToBattlefield(player1, new Island());
        attackWithThing();
        harness.passBothPriorities(); // resolve attack trigger

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Accepting with exactly one Island sacrifices it and keeps the creature")
    void acceptWithOneIsland() {
        harness.addToBattlefield(player1, new Island());
        attackWithThing();
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(countPermanents(player1, "Island")).isEqualTo(0);
        harness.assertOnBattlefield(player1, "Thing from the Deep");
    }

    @Test
    @DisplayName("Accepting can sacrifice a tapped Island and keeps the creature")
    void acceptWithTappedIsland() {
        Permanent island = harness.addToBattlefieldAndReturn(player1, new Island());
        island.tap();
        attackWithThing();
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(countPermanents(player1, "Island")).isEqualTo(0);
        harness.assertOnBattlefield(player1, "Thing from the Deep");
    }

    @Test
    @DisplayName("Accepting with two Islands lets controller choose which one to sacrifice")
    void acceptWithTwoIslandsChoosesOne() {
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player1, new Island());
        attackWithThing();
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiPermanentChoice.class);

        harness.handleMultiplePermanentsChosen(player1,
                List.of(findPermanents(player1, "Island").getFirst().getId()));

        assertThat(countPermanents(player1, "Island")).isEqualTo(1);
        harness.assertOnBattlefield(player1, "Thing from the Deep");
    }

    @Test
    @DisplayName("Declining sacrifices the creature and keeps the Island")
    void declineSacrificesCreature() {
        harness.addToBattlefield(player1, new Island());
        attackWithThing();
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player1, false);

        harness.assertNotOnBattlefield(player1, "Thing from the Deep");
        harness.assertInGraveyard(player1, "Thing from the Deep");
        assertThat(countPermanents(player1, "Island")).isEqualTo(1);
    }

    @Test
    @DisplayName("Opponent's Island does not satisfy the requirement")
    void opponentIslandDoesNotCount() {
        harness.addToBattlefield(player2, new Island());
        attackWithThing();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertNotOnBattlefield(player1, "Thing from the Deep");
        harness.assertInGraveyard(player1, "Thing from the Deep");
        assertThat(countPermanents(player2, "Island")).isEqualTo(1);
    }
}

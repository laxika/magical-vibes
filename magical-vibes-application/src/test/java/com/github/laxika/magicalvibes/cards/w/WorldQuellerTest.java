package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class WorldQuellerTest extends BaseCardTest {

    @Test
    @DisplayName("Declining the upkeep ability leaves all permanents on the battlefield")
    void decliningUpkeepAbilityDoesNothing() {
        harness.addToBattlefield(player1, new WorldQueller());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertOnBattlefield(player1, "World Queller");
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Choosing creature lets each player choose a creature, then sacrifices them together")
    void choosingCreatureSacrificesOneCreaturePerPlayer() {
        harness.addToBattlefield(player1, new WorldQueller());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new LlanowarElves());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleListChoice(player1, "Creature");

        Permanent ownCreature = findPermanent(player1, "Grizzly Bears");
        Permanent opposingCreature = findPermanent(player2, "Grizzly Bears");
        harness.handleMultiplePermanentsChosen(player1, List.of(ownCreature.getId()));
        harness.handleMultiplePermanentsChosen(player2, List.of(opposingCreature.getId()));

        harness.assertOnBattlefield(player1, "World Queller");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Llanowar Elves");
    }

    @Test
    @DisplayName("The active player chooses first when the trigger resolves during player two's upkeep")
    void activePlayerChoosesFirst() {
        harness.addToBattlefield(player2, new WorldQueller());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new LlanowarElves());

        advanceToUpkeep(player2);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);
        harness.handleListChoice(player2, "Creature");

        Permanent playerTwoCreature = findPermanent(player2, "Grizzly Bears");
        Permanent playerOneCreature = findPermanent(player1, "Grizzly Bears");
        harness.handleMultiplePermanentsChosen(player2, List.of(playerTwoCreature.getId()));
        harness.handleMultiplePermanentsChosen(player1, List.of(playerOneCreature.getId()));

        harness.assertOnBattlefield(player2, "World Queller");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Llanowar Elves");
    }

    @Test
    @DisplayName("Choosing a nonpermanent card type sacrifices no permanents")
    void choosingInstantSacrificesNothing() {
        harness.addToBattlefield(player1, new WorldQueller());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleListChoice(player1, "Instant");

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertOnBattlefield(player1, "World Queller");
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Choosing land sacrifices one land from each player")
    void choosingLandSacrificesLands() {
        harness.addToBattlefield(player1, new WorldQueller());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player2, new Forest());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleListChoice(player1, "Land");

        harness.assertOnBattlefield(player1, "World Queller");
        harness.assertNotOnBattlefield(player1, "Forest");
        harness.assertNotOnBattlefield(player2, "Forest");
    }
}

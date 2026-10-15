package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.l.LuminarchAscension;
import com.github.laxika.magicalvibes.cards.n.NissaRevane;
import com.github.laxika.magicalvibes.cards.t.TrustyMachete;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WorldQueller.class, Forest.class, GrizzlyBears.class, LlanowarElves.class,
        TrustyMachete.class, LuminarchAscension.class, NissaRevane.class})
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
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new LlanowarElves());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleListChoice(player1, "Creature");

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
        Permanent playerTwoCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent playerOneCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new LlanowarElves());

        advanceToUpkeep(player2);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);
        harness.handleListChoice(player2, "Creature");

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

    @Test
    void doesNotTriggerDuringOpponentsUpkeep() {
        harness.addToBattlefield(player1, new WorldQueller());

        advanceToUpkeep(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertOnBattlefield(player1, "World Queller");
    }

    @Test
    void sacrificesWaitUntilBothPlayersHaveChosen() {
        Permanent chosen = harness.addToBattlefieldAndReturn(player1, new WorldQueller());
        harness.addToBattlefield(player1, new WorldQueller());
        Permanent opposing = harness.addToBattlefieldAndReturn(player2, new WorldQueller());
        harness.addToBattlefield(player2, new WorldQueller());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleListChoice(player1, "Creature");
        harness.handleMultiplePermanentsChosen(player1, List.of(chosen.getId()));

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2);
        assertThat(gd.playerBattlefields.get(player2.getId())).hasSize(2);
        harness.handleMultiplePermanentsChosen(player2, List.of(opposing.getId()));

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        assertThat(gd.playerBattlefields.get(player2.getId())).hasSize(1);
        harness.assertInGraveyard(player1, "World Queller");
        harness.assertInGraveyard(player2, "World Queller");
    }

    @Test
    void mustSacrificeItselfWhenItIsTheOnlyCreature() {
        harness.addToBattlefield(player1, new WorldQueller());
        harness.addToBattlefield(player2, new Forest());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleListChoice(player1, "Creature");

        harness.assertNotOnBattlefield(player1, "World Queller");
        harness.assertInGraveyard(player1, "World Queller");
        harness.assertOnBattlefield(player2, "Forest");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @ParameterizedTest
    @ValueSource(strings = {"Artifact", "Enchantment", "Planeswalker"})
    void sacrificesOnlyPermanentsOfTheChosenType(String type) {
        harness.addToBattlefield(player1, new WorldQueller());
        harness.addToBattlefield(player1, new TrustyMachete());
        harness.addToBattlefield(player2, new TrustyMachete());
        harness.addToBattlefield(player1, new LuminarchAscension());
        harness.addToBattlefield(player2, new LuminarchAscension());
        harness.addToBattlefield(player1, new NissaRevane());
        harness.addToBattlefield(player2, new NissaRevane());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleListChoice(player1, type);

        String sacrificedName = switch (type) {
            case "Artifact" -> "Trusty Machete";
            case "Enchantment" -> "Luminarch Ascension";
            default -> "Nissa Revane";
        };
        for (var player : List.of(player1, player2)) {
            harness.assertNotOnBattlefield(player, sacrificedName);
            harness.assertInGraveyard(player, sacrificedName);
            for (String name : List.of("Trusty Machete", "Luminarch Ascension", "Nissa Revane")) {
                if (!name.equals(sacrificedName)) {
                    harness.assertOnBattlefield(player, name);
                }
            }
        }
        harness.assertOnBattlefield(player1, "World Queller");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @ParameterizedTest
    @ValueSource(strings = {"Battle", "Kindred", "Sorcery", "Conspiracy", "Dungeon",
            "Phenomenon", "Plane", "Scheme", "Vanguard"})
    void mayChooseTypesThatHaveNoMatchingPermanents(String type) {
        harness.addToBattlefield(player1, new WorldQueller());
        harness.addToBattlefield(player2, new Forest());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleListChoice(player1, type);

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertOnBattlefield(player1, "World Queller");
        harness.assertOnBattlefield(player2, "Forest");
    }
}

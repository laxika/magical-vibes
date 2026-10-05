package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.p.PurifyTheGrave;
import com.github.laxika.magicalvibes.cards.w.WrathOfGod;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MoldgrafMonstrosity.class, GrizzlyBears.class, LlanowarElves.class, WrathOfGod.class})
class MoldgrafMonstrosityTest extends BaseCardTest {

    @Test
    @DisplayName("A stolen Monstrosity is exiled from its owner's graveyard and returns its controller's creatures")
    void stolenMonstrosityIsExiledFromOwnersGraveyard() {
        MoldgrafMonstrosity monstrosity = new MoldgrafMonstrosity();
        monstrosity.setOwnerId(player2.getId());
        harness.addToBattlefield(player1, monstrosity);
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new LlanowarElves()));
        harness.setGraveyard(player2, List.of(new GrizzlyBears()));

        harness.castFromHand(player1, new WrathOfGod(), "{2}{W}{W}");
        harness.passBothPriorities();
        harness.assertInGraveyard(player2, "Moldgraf Monstrosity");
        harness.passBothPriorities();

        harness.assertNotInGraveyard(player2, "Moldgraf Monstrosity");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getId().equals(monstrosity.getId()));
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Llanowar Elves");
        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
    }

    @Test
    @CardUsed({PurifyTheGrave.class})
    @DisplayName("The creatures still return when Monstrosity is exiled in response to its death trigger")
    void returnsCreaturesWhenSourceHasAlreadyLeftGraveyard() {
        MoldgrafMonstrosity monstrosity = new MoldgrafMonstrosity();
        harness.addToBattlefield(player1, monstrosity);
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new LlanowarElves()));

        harness.castFromHand(player1, new WrathOfGod(), "{2}{W}{W}");
        harness.passBothPriorities();
        harness.setHand(player1, List.of(new PurifyTheGrave()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castInstant(player1, 0, monstrosity.getId());
        harness.passBothPriorities();
        harness.assertNotInGraveyard(player1, "Moldgraf Monstrosity");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Llanowar Elves");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .filteredOn(card -> card.getId().equals(monstrosity.getId())).hasSize(1);
    }
    @Nested
    @DisplayName("Death trigger")
    @CardUsed({MoldgrafMonstrosity.class, GrizzlyBears.class, LlanowarElves.class, WrathOfGod.class})
    class DeathTriggerTests {

        @Test
        @DisplayName("When Moldgraf Monstrosity dies, it is exiled and two creature cards are returned from graveyard to battlefield")
        void deathTriggerExilesSelfAndReturnsTwoCreatures() {
            harness.addToBattlefield(player1, new MoldgrafMonstrosity());
            harness.setGraveyard(player1, List.of(new GrizzlyBears(), new LlanowarElves()));

            harness.castFromHand(player1, new WrathOfGod(), "{2}{W}{W}");
            harness.passBothPriorities(); // Resolve Wrath — Moldgraf Monstrosity dies

            // Death trigger should be on the stack
            assertThat(gd.stack).hasSize(1);
            assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);

            // Resolve the death trigger
            harness.passBothPriorities();

            // Moldgraf Monstrosity should be exiled, not in graveyard
            harness.assertNotInGraveyard(player1, "Moldgraf Monstrosity");
            assertThat(gd.getPlayerExiledCards(player1.getId()))
                    .anyMatch(c -> c.getName().equals("Moldgraf Monstrosity"));

            // Both creatures should be on the battlefield
            List<Permanent> creatures = gd.playerBattlefields.get(player1.getId()).stream()
                    .filter(p -> p.getCard().getName().equals("Grizzly Bears")
                            || p.getCard().getName().equals("Llanowar Elves"))
                    .toList();
            assertThat(creatures).hasSize(2);

            // Graveyard should only have Wrath of God
            harness.assertNotInGraveyard(player1, "Grizzly Bears");
            harness.assertNotInGraveyard(player1, "Llanowar Elves");
        }

        @Test
        @DisplayName("Moldgraf Monstrosity cannot return itself from graveyard")
        void cannotReturnItself() {
            harness.addToBattlefield(player1, new MoldgrafMonstrosity());
            // No other creatures in graveyard — only Moldgraf itself will be there after dying

            harness.castFromHand(player1, new WrathOfGod(), "{2}{W}{W}");
            harness.passBothPriorities(); // Resolve Wrath
            harness.passBothPriorities(); // Resolve death trigger

            // Moldgraf Monstrosity should be exiled
            assertThat(gd.getPlayerExiledCards(player1.getId()))
                    .anyMatch(c -> c.getName().equals("Moldgraf Monstrosity"));

            // No creatures on battlefield (it was exiled, so it can't return itself)
            assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        }

        @Test
        @DisplayName("Returns only one creature when graveyard has only one creature card")
        void returnsOneCreatureWhenOnlyOneAvailable() {
            harness.addToBattlefield(player1, new MoldgrafMonstrosity());
            harness.setGraveyard(player1, List.of(new GrizzlyBears()));

            harness.castFromHand(player1, new WrathOfGod(), "{2}{W}{W}");
            harness.passBothPriorities(); // Resolve Wrath
            harness.passBothPriorities(); // Resolve death trigger

            // Moldgraf Monstrosity should be exiled
            assertThat(gd.getPlayerExiledCards(player1.getId()))
                    .anyMatch(c -> c.getName().equals("Moldgraf Monstrosity"));

            // Grizzly Bears should be on the battlefield
            harness.assertOnBattlefield(player1, "Grizzly Bears");
        }

        @Test
        @DisplayName("Only returns creature cards, not non-creature cards")
        void onlyReturnsCreatureCards() {
            harness.addToBattlefield(player1, new MoldgrafMonstrosity());
            // Put a non-creature (Wrath of God) and a creature (Grizzly Bears) in graveyard
            harness.setGraveyard(player1, List.of(new WrathOfGod(), new GrizzlyBears()));

            harness.castFromHand(player1, new WrathOfGod(), "{2}{W}{W}");
            harness.passBothPriorities(); // Resolve Wrath
            harness.passBothPriorities(); // Resolve death trigger

            // Grizzly Bears should be on the battlefield
            harness.assertOnBattlefield(player1, "Grizzly Bears");

            // Wrath of God (non-creature) should still be in graveyard
            harness.assertInGraveyard(player1, "Wrath of God");
        }

        @Test
        @DisplayName("Returns two out of three when graveyard has three creature cards")
        void returnsTwoOutOfThreeCreatures() {
            harness.addToBattlefield(player1, new MoldgrafMonstrosity());
            harness.setGraveyard(player1, List.of(new GrizzlyBears(), new LlanowarElves(), new GrizzlyBears()));

            harness.castFromHand(player1, new WrathOfGod(), "{2}{W}{W}");
            harness.passBothPriorities(); // Resolve Wrath
            harness.passBothPriorities(); // Resolve death trigger

            // Two creatures should be on the battlefield
            long creaturesOnBattlefield = gd.playerBattlefields.get(player1.getId()).stream()
                    .filter(p -> p.getCard().getName().equals("Grizzly Bears")
                            || p.getCard().getName().equals("Llanowar Elves"))
                    .count();
            assertThat(creaturesOnBattlefield).isEqualTo(2);

            // One creature should remain in graveyard
            long creaturesInGraveyard = gd.playerGraveyards.get(player1.getId()).stream()
                    .filter(c -> c.getName().equals("Grizzly Bears")
                            || c.getName().equals("Llanowar Elves"))
                    .count();
            assertThat(creaturesInGraveyard).isEqualTo(1);
        }

        @Test
        @DisplayName("Does nothing when graveyard has no creature cards after exile")
        void doesNothingWithNoCreaturesAfterExile() {
            harness.addToBattlefield(player1, new MoldgrafMonstrosity());
            // Empty graveyard — only Moldgraf Monstrosity itself will be there after dying

            harness.castFromHand(player1, new WrathOfGod(), "{2}{W}{W}");
            harness.passBothPriorities(); // Resolve Wrath
            harness.passBothPriorities(); // Resolve death trigger

            // Moldgraf Monstrosity should be exiled
            assertThat(gd.getPlayerExiledCards(player1.getId()))
                    .anyMatch(c -> c.getName().equals("Moldgraf Monstrosity"));

            // No creatures on battlefield
            assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        }
    }
}

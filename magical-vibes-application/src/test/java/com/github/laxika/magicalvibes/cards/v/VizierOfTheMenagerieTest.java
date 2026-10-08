package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.d.DuneBeetle;
import com.github.laxika.magicalvibes.cards.h.HazeOfPollen;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({VizierOfTheMenagerie.class, GrizzlyBears.class, Shock.class, DuneBeetle.class, HazeOfPollen.class})
class VizierOfTheMenagerieTest extends BaseCardTest {

    @Nested
    @CardUsed({VizierOfTheMenagerie.class, GrizzlyBears.class})
    @DisplayName("Cast creature spells from the top of the library")
    class CastFromLibraryTop {

        @Test
        @DisplayName("Can cast a creature spell from the top of the library")
        void castsCreatureFromLibraryTop() {
            harness.addToBattlefield(player1, new VizierOfTheMenagerie());
            Card bears = new GrizzlyBears();
            harness.setLibrary(player1, List.of(bears));
            harness.addMana(player1, ManaColor.GREEN, 2);

            harness.castFromLibraryTop(player1);
            harness.passBothPriorities();

            harness.assertOnBattlefield(player1, "Grizzly Bears");
            assertThat(gd.playerDecks.get(player1.getId())).doesNotContain(bears);
        }

        @Test
        @DisplayName("Cannot cast a creature from the top without Vizier on the battlefield")
        void cannotCastFromTopWithoutVizier() {
            Card bears = new GrizzlyBears();
            harness.setLibrary(player1, List.of(bears));
            harness.addMana(player1, ManaColor.GREEN, 2);

            assertThatThrownBy(() -> harness.castFromLibraryTop(player1))
                    .isInstanceOf(IllegalStateException.class);
            assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(bears);
        }

        @Test
        @DisplayName("Can cast a creature from the top paying its colored cost with off-color mana")
        void castsCreatureFromTopWithOffColorMana() {
            harness.addToBattlefield(player1, new VizierOfTheMenagerie());
            Card bears = new GrizzlyBears();
            harness.setLibrary(player1, List.of(bears));
            harness.addMana(player1, ManaColor.WHITE, 2);

            harness.castFromLibraryTop(player1);
            harness.passBothPriorities();

            harness.assertOnBattlefield(player1, "Grizzly Bears");
            assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        }
    }

    @Nested
    @CardUsed({VizierOfTheMenagerie.class, GrizzlyBears.class, Shock.class})
    @DisplayName("Spend mana of any type to cast creature spells")
    class SpendAnyManaType {

        @Test
        @DisplayName("A green creature is castable with only white mana")
        void greenCreaturePlayableWithWhiteMana() {
            harness.addToBattlefield(player1, new VizierOfTheMenagerie());
            Card bears = new GrizzlyBears();
            harness.setHand(player1, List.of(bears));
            harness.addMana(player1, ManaColor.WHITE, 2);

            assertThat(harness.getGameActionAvailabilityService()
                    .isCardPlayable(gd, player1.getId(), bears, gd.playerManaPools.get(player1.getId()), 0))
                    .isTrue();
        }

        @Test
        @DisplayName("A green creature is not castable with only white mana without Vizier")
        void greenCreatureNotPlayableWithoutVizier() {
            Card bears = new GrizzlyBears();
            harness.setHand(player1, List.of(bears));
            harness.addMana(player1, ManaColor.WHITE, 2);

            assertThat(harness.getGameActionAvailabilityService()
                    .isCardPlayable(gd, player1.getId(), bears, gd.playerManaPools.get(player1.getId()), 0))
                    .isFalse();
        }

        @Test
        @DisplayName("Casting a green creature with white mana consumes the mana and resolves it")
        void castsGreenCreatureWithWhiteMana() {
            harness.addToBattlefield(player1, new VizierOfTheMenagerie());
            harness.setHand(player1, List.of(new GrizzlyBears()));
            harness.addMana(player1, ManaColor.WHITE, 2);

            harness.castCreature(player1, 0);

            assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isZero();

            harness.passBothPriorities();

            harness.assertOnBattlefield(player1, "Grizzly Bears");
        }

        @Test
        @DisplayName("The grant does not apply to noncreature spells")
        void doesNotApplyToNoncreatureSpells() {
            harness.addToBattlefield(player1, new VizierOfTheMenagerie());
            Card shock = new Shock();
            harness.setHand(player1, List.of(shock));
            harness.addMana(player1, ManaColor.WHITE, 1);

            // Shock costs {R}; with only white mana and a creature-only grant it stays unaffordable.
            assertThat(harness.getGameActionAvailabilityService()
                    .isCardPlayable(gd, player1.getId(), shock, gd.playerManaPools.get(player1.getId()), 0))
                    .isFalse();
        }
    }

    @Test
    void topCardIsPrivateEvenWhenItIsNotACreature() {
        harness.addToBattlefield(player1, new VizierOfTheMenagerie());
        Card top = new HazeOfPollen();
        harness.setLibrary(player1, List.of(top));
        harness.clearMessages();

        harness.publishState();

        assertThat(harness.getConn1().getSentMessages()).anyMatch(message ->
                message.contains("\"revealedLibraryTopCards\":[[{") && message.contains(top.getId().toString()));
        assertThat(harness.getConn2().getSentMessages()).noneMatch(message ->
                message.contains(top.getId().toString()));
    }

    @Test
    void vizierInLibraryDoesNotRevealItself() {
        Card top = new VizierOfTheMenagerie();
        harness.setLibrary(player1, List.of(top));
        harness.clearMessages();

        harness.publishState();

        assertThat(harness.getConn1().getSentMessages()).noneMatch(message ->
                message.contains(top.getId().toString()));
    }

    @Test
    void cannotCastNoncreatureFromLibraryTop() {
        harness.addToBattlefield(player1, new VizierOfTheMenagerie());
        Card top = new HazeOfPollen();
        harness.setLibrary(player1, List.of(top));
        harness.addMana(player1, ManaColor.GREEN, 2);

        assertThatThrownBy(() -> harness.castFromLibraryTop(player1))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(top);
    }

    @Test
    void libraryPermissionDoesNotGrantFlash() {
        harness.addToBattlefield(player1, new VizierOfTheMenagerie());
        Card top = new DuneBeetle();
        harness.setLibrary(player1, List.of(top));
        harness.addMana(player1, ManaColor.BLACK, 2);
        gd.currentStep = TurnStep.UPKEEP;

        assertThatThrownBy(() -> harness.castFromLibraryTop(player1))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(top);
    }

    @Test
    void castsSuccessiveCreaturesWithColorlessMana() {
        harness.addToBattlefield(player1, new VizierOfTheMenagerie());
        harness.setLibrary(player1, List.of(new DuneBeetle(), new DuneBeetle()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castFromLibraryTop(player1);
        harness.passBothPriorities();
        harness.castFromLibraryTop(player1);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Dune Beetle")).hasSize(2);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    void lostAbilitiesRemoveManaPermission() {
        var vizier = harness.addToBattlefieldAndReturn(player1, new VizierOfTheMenagerie());
        vizier.setLosesAllAbilitiesUntilEndOfTurn(true);
        harness.setHand(player1, List.of(new DuneBeetle()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    void opposingVizierDoesNotGrantManaPermission() {
        harness.addToBattlefield(player2, new VizierOfTheMenagerie());
        harness.setHand(player1, List.of(new DuneBeetle()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }
}

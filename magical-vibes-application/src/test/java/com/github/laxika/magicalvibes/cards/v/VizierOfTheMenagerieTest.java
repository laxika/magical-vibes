package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class VizierOfTheMenagerieTest extends BaseCardTest {

    // ===== Cast creature spells from the top of the library =====

    @Nested
    @DisplayName("Cast creature spells from the top of the library")
    class CastFromLibraryTop {

        @Test
        @DisplayName("Can cast a creature spell from the top of the library")
        void castsCreatureFromLibraryTop() {
            harness.addToBattlefield(player1, new VizierOfTheMenagerie());
            Card bears = new GrizzlyBears();
            gd.playerDecks.get(player1.getId()).addFirst(bears);
            harness.addMana(player1, ManaColor.GREEN, 2);

            harness.castFromLibraryTop(player1);
            harness.passBothPriorities();

            assertThat(gd.playerBattlefields.get(player1.getId()))
                    .anyMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
            assertThat(gd.playerDecks.get(player1.getId())).doesNotContain(bears);
        }

        @Test
        @DisplayName("Cannot cast a creature from the top without Vizier on the battlefield")
        void cannotCastFromTopWithoutVizier() {
            Card bears = new GrizzlyBears();
            gd.playerDecks.get(player1.getId()).addFirst(bears);
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
            gd.playerDecks.get(player1.getId()).addFirst(bears);
            harness.addMana(player1, ManaColor.WHITE, 2);

            harness.castFromLibraryTop(player1);
            harness.passBothPriorities();

            assertThat(gd.playerBattlefields.get(player1.getId()))
                    .anyMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
            assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        }
    }

    // ===== Spend mana of any type to cast creature spells =====

    @Nested
    @DisplayName("Spend mana of any type to cast creature spells")
    class SpendAnyManaType {

        @Test
        @DisplayName("A green creature is castable with only white mana")
        void greenCreaturePlayableWithWhiteMana() {
            harness.addToBattlefield(player1, new VizierOfTheMenagerie());
            Card bears = new GrizzlyBears();
            harness.setHand(player1, List.of(bears));
            harness.addMana(player1, ManaColor.WHITE, 2);

            assertThat(harness.getGameBroadcastService()
                    .isCardPlayable(gd, player1.getId(), bears, gd.playerManaPools.get(player1.getId()), 0))
                    .isTrue();
        }

        @Test
        @DisplayName("A green creature is not castable with only white mana without Vizier")
        void greenCreatureNotPlayableWithoutVizier() {
            Card bears = new GrizzlyBears();
            harness.setHand(player1, List.of(bears));
            harness.addMana(player1, ManaColor.WHITE, 2);

            assertThat(harness.getGameBroadcastService()
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

            assertThat(gd.playerBattlefields.get(player1.getId()))
                    .anyMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        }

        @Test
        @DisplayName("The grant does not apply to noncreature spells")
        void doesNotApplyToNoncreatureSpells() {
            harness.addToBattlefield(player1, new VizierOfTheMenagerie());
            Card shock = new Shock();
            harness.setHand(player1, List.of(shock));
            harness.addMana(player1, ManaColor.WHITE, 1);

            // Shock costs {R}; with only white mana and a creature-only grant it stays unaffordable.
            assertThat(harness.getGameBroadcastService()
                    .isCardPlayable(gd, player1.getId(), shock, gd.playerManaPools.get(player1.getId()), 0))
                    .isFalse();
        }
    }
}

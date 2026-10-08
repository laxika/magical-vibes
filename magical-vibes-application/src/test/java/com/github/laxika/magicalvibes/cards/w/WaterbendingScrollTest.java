package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WaterbendingScroll.class, Island.class, Forest.class})
class WaterbendingScrollTest extends BaseCardTest {

    @Test
    @DisplayName("Draws a card with its activation cost reduced by Islands you control")
    void drawsCardWithReducedCost() {
        Permanent scroll = harness.addToBattlefieldAndReturn(player1, new WaterbendingScroll());
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player1, new Island());
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertInHand(player1, "Forest");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(scroll.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Cannot activate without enough mana after the Island reduction")
    void cannotActivateWithoutEnoughMana() {
        harness.addToBattlefield(player1, new WaterbendingScroll());
        harness.addToBattlefield(player1, new Island());
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("Does not count Islands controlled by an opponent")
    void doesNotCountOpponentsIslands() {
        harness.addToBattlefield(player1, new WaterbendingScroll());
        harness.addToBattlefield(player2, new Island());
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("Pays the full cost without Islands and draws only on resolution")
    void paysFullCostWithoutIslands() {
        Permanent scroll = harness.addToBattlefieldAndReturn(player1, new WaterbendingScroll());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest(), new Island()));
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.activateAbility(player1, 0, null, null);

        assertThat(scroll.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();

        harness.passBothPriorities();

        harness.assertInHand(player1, "Forest");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @ParameterizedTest
    @ValueSource(ints = {6, 7})
    @DisplayName("Six or more Islands reduce the mana cost to zero but still require tapping")
    void activatesWithoutManaWithEnoughIslands(int islandCount) {
        Permanent scroll = harness.addToBattlefieldAndReturn(player1, new WaterbendingScroll());
        for (int i = 0; i < islandCount; i++) {
            harness.addToBattlefield(player1, new Island());
        }
        harness.setLibrary(player1, List.of(new Forest()));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertInHand(player1, "Forest");
        assertThat(scroll.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
    }

    @Test
    @DisplayName("Tapped Islands still reduce the activation cost")
    void countsTappedIslands() {
        harness.addToBattlefield(player1, new WaterbendingScroll());
        harness.addToBattlefieldAndReturn(player1, new Island()).setTapped(true);
        harness.addToBattlefieldAndReturn(player1, new Island()).setTapped(true);
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertInHand(player1, "Forest");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Other land types do not reduce the activation cost")
    void doesNotCountNonIslandLands() {
        Permanent scroll = harness.addToBattlefieldAndReturn(player1, new WaterbendingScroll());
        harness.addToBattlefield(player1, new Forest());
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");

        assertThat(scroll.isTapped()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(5);
    }
}

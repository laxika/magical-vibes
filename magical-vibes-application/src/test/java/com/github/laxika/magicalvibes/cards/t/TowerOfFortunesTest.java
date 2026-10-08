package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.Shatter;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TowerOfFortunes.class, Forest.class, Island.class, Mountain.class, Plains.class, Shatter.class})
class TowerOfFortunesTest extends BaseCardTest {

    @Test
    @DisplayName("Paying {8} and tapping Tower of Fortunes draws four cards")
    void drawsFourCards() {
        Permanent tower = harness.addToBattlefieldAndReturn(player1, new TowerOfFortunes());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest(), new Island(), new Mountain(), new Plains()));
        harness.forceActivePlayer(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 8);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(4);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
        assertThat(tower.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Cannot activate Tower of Fortunes without enough mana")
    void requiresEightMana() {
        Permanent tower = harness.addToBattlefieldAndReturn(player1, new TowerOfFortunes());
        harness.forceActivePlayer(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 7);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(tower.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Cannot activate Tower of Fortunes while tapped")
    void cannotActivateWhileTapped() {
        Permanent tower = harness.addToBattlefieldAndReturn(player1, new TowerOfFortunes());
        harness.forceActivePlayer(player1);
        tower.tap();
        harness.addMana(player1, ManaColor.COLORLESS, 8);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
    }

    @Test
    @DisplayName("Tower of Fortunes can draw during the opponent's turn using colored mana")
    void activatesDuringOpponentsTurnWithColoredMana() {
        Permanent tower = harness.addToBattlefieldAndReturn(player1, new TowerOfFortunes());
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.setLibrary(player1, List.of(new Forest(), new Island(), new Mountain(), new Plains()));
        harness.forceActivePlayer(player2);
        harness.addMana(player1, ManaColor.GREEN, 8);

        harness.activateAbility(player1, 0, null, null);

        assertThat(tower.isTapped()).isTrue();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(4);
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Destroying Tower of Fortunes in response does not stop its four-card draw")
    void drawsAfterSourceIsDestroyed() {
        Permanent tower = harness.addToBattlefieldAndReturn(player1, new TowerOfFortunes());
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of(new Shatter()));
        harness.setLibrary(player1, List.of(new Forest(), new Island(), new Mountain(), new Plains()));
        harness.forceActivePlayer(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 8);
        harness.addMana(player2, ManaColor.RED, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.castInstant(player2, 0, tower.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Tower of Fortunes");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(4);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }
}

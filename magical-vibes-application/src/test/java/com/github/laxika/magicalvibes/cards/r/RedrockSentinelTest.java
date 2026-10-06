package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RedrockSentinel.class, Forest.class})
class RedrockSentinelTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrifices a land, draws a card, and creates a Treasure token")
    void sacrificesLandDrawsAndCreatesTreasure() {
        addCreatureReady(player1, new RedrockSentinel());
        harness.addToBattlefield(player1, new Forest());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest()));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getName).containsExactly("Forest");
        harness.assertInGraveyard(player1, "Forest");
        assertThat(countPermanents(player1, "Treasure")).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot activate without a land to sacrifice")
    void cannotActivateWithoutLand() {
        addCreatureReady(player1, new RedrockSentinel());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot activate without enough mana")
    void cannotActivateWithoutEnoughMana() {
        addCreatureReady(player1, new RedrockSentinel());
        harness.addToBattlefield(player1, new Forest());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("Pays tap and sacrifice costs before drawing and creating Treasure")
    void paysCostsBeforeResolution() {
        var sentinel = addCreatureReady(player1, new RedrockSentinel());
        harness.addToBattlefield(player1, new Forest());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest()));

        harness.activateAbility(player1, 0, null, null);

        assertThat(sentinel.isTapped()).isTrue();
        harness.assertNotOnBattlefield(player1, "Forest");
        harness.assertInGraveyard(player1, "Forest");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(countPermanents(player1, "Treasure")).isZero();

        harness.passBothPriorities();

        harness.assertInHand(player1, "Forest");
        assertThat(countPermanents(player1, "Treasure")).isEqualTo(1);
        assertThat(findPermanent(player1, "Treasure").isTapped()).isFalse();
    }

    @Test
    @DisplayName("Cannot activate while tapped")
    void cannotActivateWhileTapped() {
        var sentinel = addCreatureReady(player1, new RedrockSentinel());
        sentinel.setTapped(true);
        harness.addToBattlefield(player1, new Forest());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("tapped");
        harness.assertOnBattlefield(player1, "Forest");
    }

    @Test
    @DisplayName("Cannot activate with summoning sickness")
    void cannotActivateWithSummoningSickness() {
        harness.addToBattlefield(player1, new RedrockSentinel());
        harness.addToBattlefield(player1, new Forest());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");
        harness.assertOnBattlefield(player1, "Forest");
    }

    @Test
    @DisplayName("Cannot sacrifice an opponent's land to activate")
    void cannotSacrificeOpponentsLand() {
        addCreatureReady(player1, new RedrockSentinel());
        harness.addToBattlefield(player2, new Forest());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player2, "Forest");
    }

    @Test
    @DisplayName("Can choose a tapped land to sacrifice when controlling multiple lands")
    void choosesTappedLandToSacrifice() {
        addCreatureReady(player1, new RedrockSentinel());
        var keptLand = harness.addToBattlefieldAndReturn(player1, new Forest());
        var sacrificedLand = harness.addToBattlefieldAndReturn(player1, new Forest());
        sacrificedLand.setTapped(true);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest()));

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, sacrificedLand.getId());

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(keptLand).doesNotContain(sacrificedLand);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(sacrificedLand.getCard());

        harness.passBothPriorities();

        harness.assertInHand(player1, "Forest");
        assertThat(countPermanents(player1, "Treasure")).isEqualTo(1);
    }
}

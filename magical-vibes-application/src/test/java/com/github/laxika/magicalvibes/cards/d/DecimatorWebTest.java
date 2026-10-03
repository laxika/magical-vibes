package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DecimatorWeb.class})
class DecimatorWebTest extends BaseCardTest {

    @Test
    @DisplayName("Activation pays four mana and taps the source before effects resolve")
    void activationPaysCostsBeforeResolution() {
        var web = harness.addToBattlefieldAndReturn(player1, new DecimatorWeb());
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.setLife(player2, 20);
        int deckBefore = gd.playerDecks.get(player2.getId()).size();

        harness.activateAbility(player1, 0, null, player2.getId());

        assertThat(web.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        harness.assertLife(player2, 20);
        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isZero();
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(deckBefore);

        harness.passBothPriorities();
        harness.assertLife(player2, 18);
        assertThat(gd.playerPoisonCounters.get(player2.getId())).isEqualTo(1);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(deckBefore - 6);
    }

    @Test
    @DisplayName("Activating ability causes opponent to lose 2 life, get a poison counter, and mill 6 cards")
    void activateAllEffectsResolve() {
        harness.addToBattlefield(player1, new DecimatorWeb());
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.setLife(player2, 20);

        // Ensure player2 has enough cards in library
        int deckBefore = gd.playerDecks.get(player2.getId()).size();

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        // Opponent loses 2 life
        harness.assertLife(player2, 18);
        // Opponent gets a poison counter
        assertThat(gd.playerPoisonCounters.get(player2.getId())).isEqualTo(1);
        // Opponent mills 6 cards
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(deckBefore - 6);
    }

    @Test
    @DisplayName("Cannot target yourself with the ability")
    void cannotTargetSelf() {
        harness.addToBattlefield(player1, new DecimatorWeb());
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player1.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot activate without enough mana")
    void cannotActivateWithoutMana() {
        harness.addToBattlefield(player1, new DecimatorWeb());
        harness.addMana(player1, ManaColor.COLORLESS, 3); // need 4

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot activate while tapped")
    void cannotActivateWhileTapped() {
        harness.addToBattlefield(player1, new DecimatorWeb());
        harness.addMana(player1, ManaColor.COLORLESS, 8);

        // First activation taps it
        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        // Cannot activate again while tapped
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Activating multiple times accumulates all effects")
    void multipleActivationsAccumulate() {
        harness.addToBattlefield(player1, new DecimatorWeb());
        harness.setLife(player2, 20);

        // First activation
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        // Untap for second activation
        findPermanent(player1, "Decimator Web").untap();

        // Second activation
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 16);
        assertThat(gd.playerPoisonCounters.get(player2.getId())).isEqualTo(2);
    }

    @Test
    @DisplayName("A short library mills all remaining cards while life loss and poison still apply")
    void millsShortLibrary() {
        harness.addToBattlefield(player1, new DecimatorWeb());
        var cards = List.of(new DecimatorWeb(), new DecimatorWeb(), new DecimatorWeb());
        harness.setLibrary(player2, cards);
        harness.setGraveyard(player2, List.of());
        harness.setLife(player2, 20);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 18);
        assertThat(gd.playerPoisonCounters.get(player2.getId())).isEqualTo(1);
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactlyElementsOf(cards);
        harness.assertLife(player1, 20);
        assertThat(gd.playerPoisonCounters.getOrDefault(player1.getId(), 0)).isZero();
    }

    @Test
    @DisplayName("An empty library does not prevent life loss or poison")
    void resolvesWithEmptyLibrary() {
        harness.addToBattlefield(player1, new DecimatorWeb());
        harness.setLibrary(player2, List.of());
        harness.setGraveyard(player2, List.of());
        harness.setLife(player2, 20);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 18);
        assertThat(gd.playerPoisonCounters.get(player2.getId())).isEqualTo(1);
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("An activated ability resolves after its source leaves the battlefield")
    void resolvesAfterSourceLeavesBattlefield() {
        harness.addToBattlefield(player1, new DecimatorWeb());
        harness.setLife(player2, 20);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        int deckBefore = gd.playerDecks.get(player2.getId()).size();

        harness.activateAbility(player1, 0, null, player2.getId());
        gd.playerBattlefields.get(player1.getId()).clear();
        harness.passBothPriorities();

        harness.assertLife(player2, 18);
        assertThat(gd.playerPoisonCounters.get(player2.getId())).isEqualTo(1);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(deckBefore - 6);
    }
}

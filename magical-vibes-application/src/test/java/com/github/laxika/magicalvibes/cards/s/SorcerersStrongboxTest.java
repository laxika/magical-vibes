package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.e.EdgarKingOfFigaro;
import com.github.laxika.magicalvibes.cards.n.Naturalize;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SorcerersStrongbox.class, EdgarKingOfFigaro.class, Naturalize.class})
class SorcerersStrongboxTest extends BaseCardTest {

    @Test
    @DisplayName("Activating the ability flips a coin and result is consistent")
    void activatingAbilityFlipsCoin() {
        harness.addToBattlefield(player1, new SorcerersStrongbox());
        int handBefore = gd.playerHands.get(player1.getId()).size();
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        boolean onBattlefield = countPermanents(player1, "Sorcerer's Strongbox") > 0;
        boolean inGraveyard = gd.playerGraveyards.get(player1.getId()).stream()
                .anyMatch(c -> c.getName().equals("Sorcerer's Strongbox"));

        // Exactly one of the two outcomes must be true
        assertThat(onBattlefield != inGraveyard)
                .as("Sorcerer's Strongbox must be on battlefield (loss) or in graveyard (win)")
                .isTrue();

        if (inGraveyard) {
            // Won the flip: sacrificed and drew 3
            assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 3);
            assertThat(gameLogContains("wins the coin flip")).isTrue();
            assertThat(gameLogContains("is sacrificed")).isTrue();
        } else {
            // Lost the flip: nothing happens, hand unchanged
            assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);
            assertThat(gameLogContains("loses the coin flip")).isTrue();
        }
    }

    @Test
    @DisplayName("Ability requires tap - cannot activate when tapped")
    void cannotActivateWhenTapped() {
        harness.addToBattlefield(player1, new SorcerersStrongbox());
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        // First activation should work
        harness.activateAbility(player1, 0, null, null);

        assertThat(findPermanent(player1, "Sorcerer's Strongbox").isTapped()).isTrue();
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("tapped");
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Coin flip is logged to game log")
    void coinFlipIsLogged() {
        harness.addToBattlefield(player1, new SorcerersStrongbox());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gameLogContains("coin flip for Sorcerer's Strongbox")).isTrue();
    }

    @Test
    @DisplayName("Activation pays two mana and taps before the coin flip resolves")
    void costsArePaidBeforeResolution() {
        harness.addToBattlefield(player1, new SorcerersStrongbox());
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        int handBefore = gd.playerHands.get(player1.getId()).size();

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(1);
        assertThat(findPermanent(player1, "Sorcerer's Strongbox").isTapped()).isTrue();
        harness.assertNotInGraveyard(player1, "Sorcerer's Strongbox");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);
        assertThat(gameLogContains("coin flip for Sorcerer's Strongbox")).isFalse();
    }

    @Test
    @DisplayName("Activation is rejected without two mana")
    void cannotActivateWithoutEnoughMana() {
        harness.addToBattlefield(player1, new SorcerersStrongbox());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
        assertThat(gd.stack).isEmpty();
        assertThat(findPermanent(player1, "Sorcerer's Strongbox").isTapped()).isFalse();
    }

    @Test
    @CardUsed({SorcerersStrongbox.class, EdgarKingOfFigaro.class})
    @DisplayName("Winning the flip sacrifices Strongbox and draws exactly three cards")
    void winningFlipSacrificesAndDrawsThreeCards() {
        harness.addToBattlefield(player1, new EdgarKingOfFigaro());
        harness.addToBattlefield(player1, new SorcerersStrongbox());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        int handBefore = gd.playerHands.get(player1.getId()).size();

        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();

        assertThat(gameLogContains("wins the coin flip for Sorcerer's Strongbox")).isTrue();
        harness.assertNotOnBattlefield(player1, "Sorcerer's Strongbox");
        harness.assertInGraveyard(player1, "Sorcerer's Strongbox");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 3);
    }

    @Test
    @CardUsed({SorcerersStrongbox.class, EdgarKingOfFigaro.class, Naturalize.class})
    @DisplayName("Winning the flip draws even when Strongbox was destroyed in response")
    void winningFlipDrawsEvenWhenSourceHasLeftBattlefield() {
        harness.addToBattlefield(player1, new EdgarKingOfFigaro());
        var strongbox = harness.addToBattlefieldAndReturn(player1, new SorcerersStrongbox());
        harness.setHand(player2, java.util.List.of(new Naturalize()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player2, ManaColor.GREEN, 2);
        int handBefore = gd.playerHands.get(player1.getId()).size();

        harness.activateAbility(player1, 1, null, null);
        harness.castAndResolveInstant(player2, 0, strongbox.getId());
        harness.assertNotOnBattlefield(player1, "Sorcerer's Strongbox");
        harness.assertInGraveyard(player1, "Sorcerer's Strongbox");
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(gameLogContains("wins the coin flip for Sorcerer's Strongbox")).isTrue();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 3);
    }
}

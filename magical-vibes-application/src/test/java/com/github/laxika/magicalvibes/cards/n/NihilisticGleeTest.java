package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.g.GuardianOfTheGuildpact;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({NihilisticGlee.class, GuardianOfTheGuildpact.class})
class NihilisticGleeTest extends BaseCardTest {

    @Test
    @DisplayName("Discarding a card drains a target opponent")
    void discardAbilityDrainsTargetOpponent() {
        harness.addToBattlefield(player1, new NihilisticGlee());
        harness.setHand(player1, List.of(new GuardianOfTheGuildpact()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Guardian of the Guildpact");
        harness.assertLife(player1, 21);
        harness.assertLife(player2, 19);
    }

    @Test
    @DisplayName("Discard ability cannot target its controller")
    void discardAbilityCannotTargetController() {
        harness.addToBattlefield(player1, new NihilisticGlee());
        harness.setHand(player1, List.of(new GuardianOfTheGuildpact()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player1.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("opponent");
    }

    @Test
    @DisplayName("Discard ability cannot activate without a card in hand")
    void discardAbilityRequiresCardInHand() {
        harness.addToBattlefield(player1, new NihilisticGlee());
        harness.setHand(player1, List.of());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("discard");
    }

    @Test
    @DisplayName("Hellbent ability pays life and draws with an empty hand")
    void hellbentAbilityPaysLifeAndDraws() {
        harness.addToBattlefield(player1, new NihilisticGlee());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new GuardianOfTheGuildpact()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.setLife(player1, 20);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        harness.assertLife(player1, 18);
        harness.assertInHand(player1, "Guardian of the Guildpact");
    }

    @Test
    @DisplayName("Hellbent ability cannot activate with cards in hand")
    void hellbentAbilityRequiresEmptyHand() {
        harness.addToBattlefield(player1, new NihilisticGlee());
        harness.setHand(player1, List.of(new GuardianOfTheGuildpact()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("no cards in hand");
    }

    @Test
    @DisplayName("Hellbent pays life before drawing on resolution")
    void hellbentPaysLifeBeforeResolution() {
        harness.addToBattlefield(player1, new NihilisticGlee());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new GuardianOfTheGuildpact()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.setLife(player1, 20);

        harness.activateAbility(player1, 0, 1, null, null);

        harness.assertLife(player1, 18);
        harness.assertNotInHand(player1, "Guardian of the Guildpact");

        harness.passBothPriorities();

        harness.assertInHand(player1, "Guardian of the Guildpact");
        harness.assertLife(player1, 18);
    }

    @Test
    @DisplayName("Hellbent cannot activate when its life cost cannot be paid")
    void hellbentRequiresEnoughLife() {
        harness.addToBattlefield(player1, new NihilisticGlee());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new GuardianOfTheGuildpact()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.setLife(player1, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertLife(player1, 1);
        harness.assertNotInHand(player1, "Guardian of the Guildpact");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Hellbent activations still draw after an earlier activation fills the hand")
    void stackedHellbentActivationsDoNotRecheckHandOnResolution() {
        harness.addToBattlefield(player1, new NihilisticGlee());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new GuardianOfTheGuildpact(), new GuardianOfTheGuildpact()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.setLife(player1, 20);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.activateAbility(player1, 0, 1, null, null);
        harness.assertLife(player1, 16);

        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        harness.assertLife(player1, 16);
    }
}

package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.GiveTargetPlayerPoisonCountersEffect;
import com.github.laxika.magicalvibes.model.effect.MillTargetPlayerEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerLosesLifeEffect;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DecimatorWebTest extends BaseCardTest {

    // ===== Card structure =====

    @Test
    @DisplayName("Has activated ability with tap, {4} mana, three effects, and opponent-only target filter")
    void hasActivatedAbility() {
        DecimatorWeb card = new DecimatorWeb();

        assertThat(card.getActivatedAbilities()).hasSize(1);
        var ability = card.getActivatedAbilities().getFirst();
        assertThat(ability.isRequiresTap()).isTrue();
        assertThat(ability.getManaCost()).isEqualTo("{4}");
        assertThat(ability.getTargetFilter()).isInstanceOf(PlayerPredicateTargetFilter.class);
        assertThat(ability.getEffects())
                .hasSize(3)
                .anyMatch(e -> e instanceof TargetPlayerLosesLifeEffect tp
                        && tp.amount() == 2)
                .anyMatch(e -> e instanceof GiveTargetPlayerPoisonCountersEffect gp
                        && gp.amount() == 1)
                .anyMatch(e -> e instanceof MillTargetPlayerEffect mp
                        && mp.count() == 6);
    }

    // ===== Activation: all three effects resolve =====

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

    // ===== Targeting restriction: opponents only =====

    @Test
    @DisplayName("Cannot target yourself with the ability")
    void cannotTargetSelf() {
        harness.addToBattlefield(player1, new DecimatorWeb());
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player1.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    // ===== Costs: tap and mana =====

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

    // ===== Multiple activations across turns =====

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
        gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Decimator Web"))
                .findFirst().orElseThrow().untap();

        // Second activation
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 16);
        assertThat(gd.playerPoisonCounters.get(player2.getId())).isEqualTo(2);
    }
}

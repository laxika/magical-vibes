package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.e.Exploration;
import com.github.laxika.magicalvibes.cards.h.Humble;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ArgothianEnchantress.class, Exploration.class, Humble.class})
class ArgothianEnchantressTest extends BaseCardTest {

    @Test
    @DisplayName("Draws a card when the controller casts an enchantment spell")
    void drawsWhenControllerCastsEnchantment() {
        harness.addToBattlefield(player1, new ArgothianEnchantress());
        harness.setHand(player1, List.of(new Exploration()));
        harness.setLibrary(player1, List.of(new ArgothianEnchantress()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId()))
                .extracting(card -> card.getName())
                .containsExactly("Argothian Enchantress");
    }

    @Test
    @DisplayName("Does not trigger for a non-enchantment spell")
    void doesNotTriggerForNonEnchantmentSpell() {
        harness.addToBattlefield(player1, new ArgothianEnchantress());
        harness.setHand(player1, List.of(new ArgothianEnchantress()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);

        assertThat(gd.stack)
                .noneMatch(entry -> entry.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                        && entry.getCard().getName().equals("Argothian Enchantress"));
    }

    @Test
    @DisplayName("Does not trigger when an opponent casts an enchantment spell")
    void doesNotTriggerForOpponentsEnchantmentSpell() {
        harness.addToBattlefield(player1, new ArgothianEnchantress());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new ArgothianEnchantress()));
        harness.setHand(player2, List.of(new Exploration()));
        harness.addMana(player2, ManaColor.GREEN, 1);

        harness.castEnchantment(player2, 0);
        assertThat(gd.stack)
                .noneMatch(entry -> entry.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                        && entry.getCard().getName().equals("Argothian Enchantress"));

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Cannot be targeted because it has shroud")
    void cannotBeTargetedBecauseOfShroud() {
        Permanent enchantress = harness.addToBattlefieldAndReturn(player1, new ArgothianEnchantress());
        harness.setHand(player1, List.of(new Humble()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, enchantress.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("shroud");
    }
}

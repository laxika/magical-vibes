package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.a.AkkiRaider;
import com.github.laxika.magicalvibes.cards.m.MendingHands;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DisruptingShoal.class, AkkiRaider.class, MendingHands.class})
class DisruptingShoalTest extends BaseCardTest {

    @Test
    @DisplayName("Counters the target spell when its mana value equals X")
    void countersSpellWithMatchingManaValue() {
        AkkiRaider raider = new AkkiRaider(); // MV 2
        harness.setHand(player1, List.of(raider));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.setHand(player2, List.of(new DisruptingShoal()));
        harness.addMana(player2, ManaColor.BLUE, 4);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, 2, raider.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Akki Raider");
        harness.assertNotOnBattlefield(player1, "Akki Raider");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The spell resolves when its mana value differs from X")
    void doesNotCounterOnManaValueMismatch() {
        MendingHands mendingHands = new MendingHands(); // MV 1
        harness.setHand(player1, List.of(mendingHands));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.setHand(player2, List.of(new DisruptingShoal()));
        harness.addMana(player2, ManaColor.BLUE, 5);

        harness.castInstant(player1, 0, player2.getId());
        harness.passPriority(player1);
        harness.castInstant(player2, 0, 3, mendingHands.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Mending Hands");
        harness.assertInGraveyard(player2, "Disrupting Shoal");
        assertThat(gd.playerDamagePreventionShields.getOrDefault(player2.getId(), 0)).isEqualTo(4);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Exiling a blue card with mana value X pays the alternative cost")
    void alternativeCostExilesBlueCardWithManaValueX() {
        AkkiRaider raider = new AkkiRaider(); // MV 2
        harness.setHand(player1, List.of(raider));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        // Disrupting Shoal's own mana value is 2, so exiling it pays for X = 2 with no mana spent.
        harness.setHand(player2, List.of(new DisruptingShoal(), new DisruptingShoal()));

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstantWithAlternateExileFromHand(player2, 0, 2, raider.getId(), 1);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Akki Raider");
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .extracting(Card::getName)
                .containsExactly("Disrupting Shoal");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The exiled card's mana value must equal the chosen X")
    void alternativeCostRejectsMismatchedManaValue() {
        AkkiRaider raider = new AkkiRaider();
        harness.setHand(player1, List.of(raider));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.setHand(player2, List.of(new DisruptingShoal(), new DisruptingShoal()));

        harness.castCreature(player1, 0);
        harness.passPriority(player1);

        assertThatThrownBy(() ->
                harness.castInstantWithAlternateExileFromHand(player2, 0, 3, raider.getId(), 1))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(2);
    }

    @Test
    @DisplayName("The alternative cost rejects a nonblue card with matching mana value")
    void alternativeCostRejectsNonBlueCardWithMatchingManaValue() {
        AkkiRaider raider = new AkkiRaider();
        harness.setHand(player1, List.of(raider));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.setHand(player2, List.of(new DisruptingShoal(), new AkkiRaider()));

        harness.castCreature(player1, 0);
        harness.passPriority(player1);

        assertThatThrownBy(() ->
                harness.castInstantWithAlternateExileFromHand(player2, 0, 2, raider.getId(), 1))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(2);
    }
}

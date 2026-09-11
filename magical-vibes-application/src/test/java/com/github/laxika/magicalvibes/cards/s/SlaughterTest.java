package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.Carnophage;
import com.github.laxika.magicalvibes.cards.r.RagingGoblin;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Slaughter.class, RagingGoblin.class, Carnophage.class, Spellbook.class, SonicBurst.class})
class SlaughterTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys a target nonblack creature and cannot be regenerated")
    void destroysNonblackCreatureWithoutRegeneration() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new RagingGoblin());
        target.setRegenerationShield(1);
        prepareCast();

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Raging Goblin");
    }

    @Test
    @DisplayName("Paying buyback loses 4 life and returns Slaughter to hand")
    void buybackPaysLifeAndReturnsToHand() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new RagingGoblin());
        harness.setLife(player1, 20);
        prepareCast();

        harness.castInstantWithBuyback(player1, 0, target.getId());

        assertThat(gd.getLife(player1.getId())).isEqualTo(16);
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Raging Goblin");
        assertThat(gd.playerHands.get(player1.getId()))
                .extracting(Card::getName)
                .containsExactly("Slaughter");
        harness.assertNotInGraveyard(player1, "Slaughter");
    }

    @Test
    @DisplayName("Without buyback Slaughter goes to the graveyard")
    void withoutBuybackGoesToGraveyard() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new RagingGoblin());
        prepareCast();

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Slaughter");
        harness.assertNotInHand(player1, "Slaughter");
    }

    @Test
    @DisplayName("Cannot target a black creature")
    void cannotTargetBlackCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Carnophage());
        prepareCast();

        assertThatThrownBy(() -> harness.castInstant(
                player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("nonblack creature");
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreaturePermanent() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Spellbook());
        prepareCast();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("nonblack creature");
    }

    @Test
    @DisplayName("A fizzled buyback spell goes to the graveyard")
    void buybackFizzleGoesToGraveyard() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new RagingGoblin());
        harness.setLife(player1, 20);
        prepareCast();

        harness.castInstantWithBuyback(player1, 0, target.getId());

        harness.setHand(player2, List.of(new SonicBurst(), new RagingGoblin()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castInstant(player2, 0, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Slaughter");
        harness.assertNotInHand(player1, "Slaughter");
    }

    @Test
    @DisplayName("Cannot pay buyback with less than 4 life")
    void cannotPayBuybackWithInsufficientLife() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new RagingGoblin());
        harness.setLife(player1, 3);
        prepareCast();

        assertThatThrownBy(() -> harness.castInstantWithBuyback(
                player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough life");
        assertThat(gd.getLife(player1.getId())).isEqualTo(3);
        assertThat(gd.playerHands.get(player1.getId()))
                .extracting(Card::getName)
                .containsExactly("Slaughter");
    }

    private void prepareCast() {
        harness.setHand(player1, List.of(new Slaughter()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }
}

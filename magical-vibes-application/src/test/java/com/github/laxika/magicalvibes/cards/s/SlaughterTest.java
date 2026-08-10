package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MassOfGhouls;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SlaughterTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys a target nonblack creature and cannot be regenerated")
    void destroysNonblackCreatureWithoutRegeneration() {
        Permanent target = new Permanent(new GrizzlyBears());
        target.setRegenerationShield(1);
        gd.playerBattlefields.get(player2.getId()).add(target);
        prepareCast();

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Paying buyback loses 4 life and returns Slaughter to hand")
    void buybackPaysLifeAndReturnsToHand() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setLife(player1, 20);
        prepareCast();

        harness.castInstantWithBuyback(player1, 0, harness.getPermanentId(player2, "Grizzly Bears"));

        assertThat(gd.getLife(player1.getId())).isEqualTo(16);
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.playerHands.get(player1.getId()))
                .extracting(Card::getName)
                .containsExactly("Slaughter");
        harness.assertNotInGraveyard(player1, "Slaughter");
    }

    @Test
    @DisplayName("Cannot target a black creature")
    void cannotTargetBlackCreature() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new MassOfGhouls());
        prepareCast();

        assertThatThrownBy(() -> harness.castInstant(
                player1, 0, harness.getPermanentId(player2, "Mass of Ghouls")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("nonblack creature");
    }

    @Test
    @DisplayName("Cannot pay buyback with less than 4 life")
    void cannotPayBuybackWithInsufficientLife() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setLife(player1, 3);
        prepareCast();

        assertThatThrownBy(() -> harness.castInstantWithBuyback(
                player1, 0, harness.getPermanentId(player2, "Grizzly Bears")))
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

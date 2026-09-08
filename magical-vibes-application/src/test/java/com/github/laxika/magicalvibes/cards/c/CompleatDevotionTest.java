package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CompleatDevotionTest extends BaseCardTest {

    @Test
    @DisplayName("Pumps a toxic creature and draws a card")
    void pumpsToxicCreatureAndDrawsCard() {
        Permanent mite = createToxicMite();
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.setHand(player1, List.of(new CompleatDevotion()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castInstant(player1, 0, mite.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, mite)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, mite)).isEqualTo(3);
        assertThat(gd.playerHands.get(player1.getId()))
                .extracting(card -> card.getName())
                .containsExactly("Grizzly Bears");
    }

    @Test
    @DisplayName("Pumps a non-toxic creature without drawing")
    void pumpsNonToxicCreatureWithoutDrawing() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.setHand(player1, List.of(new CompleatDevotion()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castInstant(player1, 0, bears.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(4);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Can target only a creature you control")
    void cannotTargetOpponentCreature() {
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new CompleatDevotion()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, opponentCreature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent createToxicMite() {
        harness.setHand(player1, List.of(new ChargeOfTheMites()));
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.castInstant(player1, 0, 1, null);
        harness.passBothPriorities();
        return findPermanent(player1, "Mite");
    }
}

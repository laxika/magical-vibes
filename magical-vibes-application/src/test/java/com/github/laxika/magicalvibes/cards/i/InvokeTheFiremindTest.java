package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({InvokeTheFiremind.class, GrizzlyBears.class, Forest.class})
class InvokeTheFiremindTest extends BaseCardTest {

    @Test
    @DisplayName("Draws X cards in draw mode")
    void drawsXCards() {
        harness.setHand(player1, List.of(new InvokeTheFiremind()));
        harness.setLibrary(player1, List.of(
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        addMana(3);

        harness.castModalSorceryWithModesForX(player1, 0, 1, new int[]{0}, 3, List.of());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Deals X damage to any target in damage mode")
    void dealsXDamageToCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new InvokeTheFiremind()));
        addMana(1);

        harness.castModalSorceryWithModesForX(player1, 0, 1, new int[]{1}, 1, target.getId(), List.of());
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("Damage mode cannot target a card in hand")
    void damageModeRejectsCardInHand() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setHand(player1, List.of(new InvokeTheFiremind()));
        addMana(1);

        assertThatThrownBy(() -> harness.castModalSorceryWithModesForX(
                player1, 0, 1, new int[]{1}, 1, target.getId(), List.of()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void addMana(int xValue) {
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, xValue);
    }
}

package com.github.laxika.magicalvibes.cards.t;

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

@CardUsed({TymorasInvoker.class, GrizzlyBears.class})
class TymorasInvokerTest extends BaseCardTest {

    @Test
    @DisplayName("Paying eight mana with Tymora's Invoker draws two cards")
    void abilityDrawsTwoCards() {
        addInvoker();
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.COLORLESS, 8);
        int handBefore = gd.playerHands.get(player1.getId()).size();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 2);
        assertThat(gd.playerHands.get(player1.getId()))
                .filteredOn(card -> card instanceof GrizzlyBears)
                .hasSize(2);
    }

    @Test
    @DisplayName("Tymora's Invoker cannot activate without eight mana")
    void abilityRequiresEightMana() {
        addInvoker();
        harness.addMana(player1, ManaColor.COLORLESS, 7);
        int handBefore = gd.playerHands.get(player1.getId()).size();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);
    }

    private Permanent addInvoker() {
        return addCreatureReady(player1, new TymorasInvoker());
    }
}

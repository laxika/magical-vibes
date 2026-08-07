package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FoolsTomeTest extends BaseCardTest {

    @Test
    @DisplayName("Draws a card and taps when the controller has no cards in hand")
    void drawsWhenHandEmpty() {
        Permanent tome = addTome();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(tome.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Ability cannot activate while the controller holds a card")
    void cannotActivateWithCardsInHand() {
        Permanent tome = addTome();
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("no cards in hand");

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(tome.isTapped()).isFalse();
    }

    private Permanent addTome() {
        Permanent perm = new Permanent(new FoolsTome());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(perm);
        return perm;
    }
}

package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.a.AetherShockwave;
import com.github.laxika.magicalvibes.cards.g.GazeOfAdamaro;
import com.github.laxika.magicalvibes.cards.l.LongForgottenGohei;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Evermind.class, GazeOfAdamaro.class, AetherShockwave.class})
class EvermindTest extends BaseCardTest {

    @Test
    @DisplayName("Splices onto an Arcane spell, draws a card, and stays in hand")
    void splicesOntoArcaneSpellAndDraws() {
        GazeOfAdamaro arcaneSpell = new GazeOfAdamaro();
        Evermind evermind = new Evermind();
        AetherShockwave drawnCard = new AetherShockwave();
        harness.setHand(player1, List.of(arcaneSpell, evermind));
        harness.setHand(player2, List.of(new AetherShockwave()));
        harness.setLibrary(player1, List.of(drawnCard));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castWithSplice(player1, 0, player2.getId(), List.of(1));
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(19);
        assertThat(gd.playerHands.get(player1.getId()))
                .containsExactlyInAnyOrder(evermind, drawnCard);
    }

    @Test
    @DisplayName("Cannot splice onto a non-Arcane spell")
    void rejectsNonArcaneHost() {
        harness.setHand(player1, List.of(new AetherShockwave(), new Evermind()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.castWithSplice(player1, 0, null, List.of(1)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("cannot be spliced");
    }

    @Test
    @DisplayName("Cannot cast directly because it has no mana cost")
    void cannotCastDirectly() {
        harness.setHand(player1, List.of(new Evermind()));

        assertThatThrownBy(() -> harness.castInstant(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Two different copies can both be spliced onto the same spell")
    void splicesTwoCopies() {
        Evermind first = new Evermind();
        Evermind second = new Evermind();
        AetherShockwave firstDraw = new AetherShockwave();
        AetherShockwave secondDraw = new AetherShockwave();
        harness.setHand(player1, List.of(new GazeOfAdamaro(), first, second));
        harness.setHand(player2, List.of(new AetherShockwave()));
        harness.setLibrary(player1, List.of(firstDraw, secondDraw));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castWithSplice(player1, 0, player2.getId(), List.of(1, 2));
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(19);
        assertThat(gd.playerHands.get(player1.getId()))
                .containsExactlyInAnyOrder(first, second, firstDraw, secondDraw);
    }

    @Test
    @DisplayName("Cannot pay the splice cost without blue mana")
    void requiresBlueManaForSplice() {
        harness.setHand(player1, List.of(new GazeOfAdamaro(), new Evermind()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.castWithSplice(player1, 0, player2.getId(), List.of(1)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana to pay splice cost");
    }

    @Test
    @CardUsed({LongForgottenGohei.class})
    @DisplayName("Arcane cost reductions also reduce the generic portion of splice costs")
    void reducesTotalCostIncludingSplice() {
        harness.addToBattlefield(player1, new LongForgottenGohei());
        harness.addToBattlefield(player1, new LongForgottenGohei());
        harness.addToBattlefield(player1, new LongForgottenGohei());
        Evermind evermind = new Evermind();
        AetherShockwave drawnCard = new AetherShockwave();
        harness.setHand(player1, List.of(new GazeOfAdamaro(), evermind));
        harness.setHand(player2, List.of(new AetherShockwave()));
        harness.setLibrary(player1, List.of(drawnCard));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castWithSplice(player1, 0, player2.getId(), List.of(1));
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(19);
        assertThat(gd.playerHands.get(player1.getId()))
                .containsExactlyInAnyOrder(evermind, drawnCard);
    }
}

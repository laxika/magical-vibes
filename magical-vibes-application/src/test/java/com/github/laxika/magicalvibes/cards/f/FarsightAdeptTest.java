package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.i.IntoTheRoil;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FarsightAdept.class, Forest.class, IntoTheRoil.class})
class FarsightAdeptTest extends BaseCardTest {

    @Test
    @DisplayName("ETB makes its controller and target opponent each draw a card")
    void eachDrawsACard() {
        harness.setHand(player1, List.of(new FarsightAdept()));
        harness.setHand(player2, List.of());
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setLibrary(player2, List.of(new Forest()));
        addMana();

        harness.castCreature(player1, 0, 0, player2.getId());
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getName)
                .containsExactly("Forest");
        assertThat(gd.playerHands.get(player2.getId())).extracting(Card::getName)
                .containsExactly("Forest");
    }

    @Test
    @DisplayName("ETB cannot target its controller")
    void cannotTargetController() {
        harness.setHand(player1, List.of(new FarsightAdept()));
        addMana();

        assertThatThrownBy(() -> harness.castCreature(player1, 0, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("opponent");
    }

    @Test
    @DisplayName("The active controller draws before the targeted opponent")
    void activeControllerDrawsFirst() {
        harness.setHand(player1, List.of(new FarsightAdept()));
        harness.setHand(player2, List.of());
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setLibrary(player2, List.of(new Forest()));
        addMana();

        harness.castCreature(player1, 0, player2.getId());
        resolveAllTriggers();

        assertThat(gd.gameLog.stream().map(entry -> entry.plainText())
                .filter(text -> text.endsWith(" draws a card.")).toList())
                .containsExactly(gd.playerIdToName.get(player1.getId()) + " draws a card.",
                        gd.playerIdToName.get(player2.getId()) + " draws a card.");
    }

    @Test
    @DisplayName("The draw trigger resolves after Adept leaves the battlefield")
    void drawsAfterSourceLeavesBattlefield() {
        harness.setHand(player1, List.of(new FarsightAdept()));
        harness.setHand(player2, List.of(new IntoTheRoil()));
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setLibrary(player2, List.of(new Forest()));
        addMana();
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.castAndResolveInstant(player2, 0,
                harness.getPermanentId(player1, "Farsight Adept"));
        harness.assertNotOnBattlefield(player1, "Farsight Adept");
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getName)
                .containsExactly("Farsight Adept", "Forest");
        assertThat(gd.playerHands.get(player2.getId())).extracting(Card::getName)
                .containsExactly("Forest");
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }
}

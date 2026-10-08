package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.s.SpectralSailor;
import com.github.laxika.magicalvibes.cards.g.GreenwoodSentinel;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WingedWords.class, SpectralSailor.class, GreenwoodSentinel.class})
class WingedWordsTest extends BaseCardTest {

    @Test
    @DisplayName("Costs {1}{U} when you control a creature with flying")
    void costsOneLessWithFlyingCreature() {
        harness.addToBattlefield(player1, new SpectralSailor());
        harness.setHand(player1, List.of(new WingedWords()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castSorcery(player1, 0, 0);

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Cannot use the reduced cost without a creature with flying")
    void doesNotGetReductionWithoutFlyingCreature() {
        harness.setHand(player1, List.of(new WingedWords()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Resolving Winged Words draws two cards")
    void resolvingDrawsTwoCards() {
        int deckSizeBefore = gd.playerDecks.get(player1.getId()).size();

        harness.setHand(player1, List.of(new WingedWords()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckSizeBefore - 2);
    }

    @Test
    @DisplayName("An opponent's flying creature does not reduce the cost")
    void opponentFlyingCreatureDoesNotReduceCost() {
        harness.addToBattlefield(player2, new SpectralSailor());
        harness.setHand(player1, List.of(new WingedWords()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Multiple flying creatures still reduce the cost by only one")
    void multipleFlyersDoNotIncreaseReduction() {
        harness.addToBattlefield(player1, new SpectralSailor());
        harness.addToBattlefield(player1, new SpectralSailor());
        harness.setHand(player1, List.of(new WingedWords()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The reduction does not remove the blue mana requirement")
    void reductionDoesNotRemoveBlueRequirement() {
        harness.addToBattlefield(player1, new SpectralSailor());
        harness.setHand(player1, List.of(new WingedWords()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("A flying creature in hand does not reduce the cost")
    void flyingCreatureInHandDoesNotReduceCost() {
        harness.setHand(player1, List.of(new WingedWords(), new SpectralSailor()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("A creature without flying does not reduce the cost")
    void nonFlyingCreatureDoesNotReduceCost() {
        harness.addToBattlefield(player1, new GreenwoodSentinel());
        harness.setHand(player1, List.of(new WingedWords()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }
}

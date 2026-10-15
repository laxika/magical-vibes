package com.github.laxika.magicalvibes.cards.m;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.github.laxika.magicalvibes.cards.c.Counterspell;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

@CardUsed({MysticSpeculation.class, Forest.class, GrizzlyBears.class, Mountain.class, Counterspell.class})
class MysticSpeculationTest extends BaseCardTest {

    @Test
    @DisplayName("Scry 3 reorders the top cards and puts Mystic Speculation in the graveyard")
    void scriesThreeWithoutBuyback() {
        Card first = new Forest();
        Card second = new GrizzlyBears();
        Card third = new Mountain();
        harness.setLibrary(player1, List.of(first, second, third));
        harness.setHand(player1, List.of(new MysticSpeculation()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castAndResolveSorcery(player1, 0, (java.util.UUID) null);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.Scry.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards())
                .containsExactly(first, second, third);

        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(1, 0), List.of(2)));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(second, first, third);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(Card::getName)
                .containsExactly("Mystic Speculation");
    }

    @Test
    @DisplayName("Buyback returns Mystic Speculation to its owner's hand after scrying")
    void buybackReturnsToHand() {
        Card first = new Forest();
        Card second = new GrizzlyBears();
        Card third = new Mountain();
        harness.setLibrary(player1, List.of(first, second, third));
        MysticSpeculation speculation = new MysticSpeculation();
        harness.setHand(player1, List.of(speculation));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castSorceryWithBuyback(player1, 0, null);
        assertThat(gd.stack.getFirst().isBuyback()).isTrue();
        harness.passBothPriorities();

        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(0, 1, 2), List.of()));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(speculation);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("A countered buyback spell goes to its owner's graveyard")
    void counteredBuybackGoesToGraveyard() {
        MysticSpeculation speculation = new MysticSpeculation();
        Counterspell counterspell = new Counterspell();
        harness.setHand(player1, List.of(speculation));
        harness.setHand(player2, List.of(counterspell));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castSorceryWithBuyback(player1, 0, null);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, speculation.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(speculation);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(speculation);
    }

    @Test
    @DisplayName("Buyback requires both additional generic mana")
    void buybackRequiresAdditionalMana() {
        MysticSpeculation speculation = new MysticSpeculation();
        harness.setHand(player1, List.of(speculation));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castSorceryWithBuyback(player1, 0, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(speculation);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isEqualTo(2);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Scry can put all three cards on the bottom in any order")
    void putsAllThreeOnBottom() {
        Card first = new Forest();
        Card second = new GrizzlyBears();
        Card third = new Mountain();
        Card fourth = new Forest();
        MysticSpeculation speculation = new MysticSpeculation();
        harness.setLibrary(player1, List.of(first, second, third, fourth));
        harness.setHand(player1, List.of(speculation));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castAndResolveSorcery(player1, 0, (java.util.UUID) null);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards())
                .containsExactly(first, second, third);
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(), List.of(2, 0, 1)));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(fourth, third, first, second);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(speculation);
    }

    @Test
    @DisplayName("Scry 3 uses the available cards when the library has fewer than three")
    void scriesShortLibrary() {
        Card first = new Forest();
        Card second = new Mountain();
        MysticSpeculation speculation = new MysticSpeculation();
        harness.setLibrary(player1, List.of(first, second));
        harness.setHand(player1, List.of(speculation));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castSorceryWithBuyback(player1, 0, null);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards())
                .containsExactly(first, second);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(speculation);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(speculation);
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(1), List.of(0)));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(second, first);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(speculation);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Buyback returns the spell even when the library is empty")
    void buybackWithEmptyLibrary() {
        MysticSpeculation speculation = new MysticSpeculation();
        harness.setLibrary(player1, List.of());
        harness.setHand(player1, List.of(speculation));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castSorceryWithBuyback(player1, 0, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(speculation);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }
}

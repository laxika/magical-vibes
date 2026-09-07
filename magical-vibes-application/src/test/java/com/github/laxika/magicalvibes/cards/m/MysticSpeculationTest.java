package com.github.laxika.magicalvibes.cards.m;

import static org.assertj.core.api.Assertions.assertThat;

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
import java.util.UUID;

@CardUsed({MysticSpeculation.class, Forest.class, GrizzlyBears.class, Mountain.class})
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

        harness.castSorcery(player1, 0, (UUID) null);
        harness.passBothPriorities();

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

        harness.castSorceryWithBuyback(player1, 0, (UUID) null);
        assertThat(gd.stack.getFirst().isBuyback()).isTrue();
        harness.passBothPriorities();

        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(0, 1, 2), List.of()));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(speculation);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }
}

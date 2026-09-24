package com.github.laxika.magicalvibes.cards.y;

import com.github.laxika.magicalvibes.cards.e.EdgarKingOfFigaro;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({YusriFortunesFlame.class, EdgarKingOfFigaro.class, Forest.class, Shock.class})
class YusriFortunesFlameTest extends BaseCardTest {

    @Test
    @DisplayName("Five won flips draw five cards and grant free casts from hand this turn")
    void fiveWonFlipsDrawAndGrantFreeCast() {
        addCreatureReady(player1, new YusriFortunesFlame());
        addCreatureReady(player1, new EdgarKingOfFigaro());
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest(), new Forest(), new Forest()));
        harness.setHand(player1, List.of(new Shock()));

        declareAttackers(List.of(0));
        resolveAllTriggers();

        PendingInteraction.ColorChoice choice = gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(choice.options()).containsExactly("1", "2", "3", "4", "5");
        harness.handleListChoice(player1, "5");
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(6);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card.getName().equals("Shock"));
    }

    @Test
    @DisplayName("Each flip draws on a win and damages Yusri's controller on a loss")
    void eachFlipResolvesItsResult() {
        addCreatureReady(player1, new YusriFortunesFlame());
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest()));
        harness.setHand(player1, List.of());

        declareAttackers(List.of(0));
        resolveAllTriggers();
        harness.handleListChoice(player1, "3");
        resolveAllTriggers();

        long wins = gd.gameLog.stream()
                .filter(log -> log.plainText().contains("wins the coin flip for Yusri, Fortune's Flame"))
                .count();
        long losses = gd.gameLog.stream()
                .filter(log -> log.plainText().contains("loses the coin flip for Yusri, Fortune's Flame"))
                .count();

        assertThat(wins + losses).isEqualTo(3);
        assertThat(gd.playerHands.get(player1.getId())).hasSize((int) wins);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20 - (2 * (int) losses));
    }
}

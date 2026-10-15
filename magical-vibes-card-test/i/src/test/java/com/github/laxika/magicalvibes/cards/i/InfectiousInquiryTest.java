package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.c.ContagiousVorrac;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({InfectiousInquiry.class, ContagiousVorrac.class})
class InfectiousInquiryTest extends BaseCardTest {

    @Test
    @DisplayName("Draws two cards, loses 2 life, and gives each opponent a poison counter")
    void resolvesAllEffects() {
        harness.setHand(player1, List.of(new InfectiousInquiry()));
        harness.setLibrary(player1, List.of(new ContagiousVorrac(), new ContagiousVorrac()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveSorcery(player1, 0, List.of());

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
        assertThat(gd.playerPoisonCounters.getOrDefault(player1.getId(), 0)).isZero();
        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isEqualTo(1);
    }

    @Test
    @DisplayName("Adds to existing poison counters without poisoning its controller")
    void addsToExistingPoisonCounters() {
        harness.setHand(player1, List.of(new InfectiousInquiry()));
        harness.setLibrary(player1, List.of(new ContagiousVorrac(), new ContagiousVorrac()));
        gd.playerPoisonCounters.put(player1.getId(), 3);
        gd.playerPoisonCounters.put(player2.getId(), 4);
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castAndResolveSorcery(player1, 0, List.of());

        assertThat(gd.playerPoisonCounters.get(player1.getId())).isEqualTo(3);
        assertThat(gd.playerPoisonCounters.get(player2.getId())).isEqualTo(5);
        harness.assertLife(player1, 18);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Completes the poison effect even when the life loss is lethal")
    void completesEffectsAfterLethalLifeLoss() {
        harness.setHand(player1, List.of(new InfectiousInquiry()));
        harness.setLibrary(player1, List.of(new ContagiousVorrac(), new ContagiousVorrac()));
        harness.setLife(player1, 2);
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castAndResolveSorcery(player1, 0, List.of());

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        harness.assertLife(player1, 0);
        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isEqualTo(1);
        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
    }

    @Test
    @DisplayName("Completes life loss and poison after attempting to draw from a short library")
    void completesEffectsWithOnlyOneCardInLibrary() {
        harness.setHand(player1, List.of(new InfectiousInquiry()));
        harness.setLibrary(player1, List.of(new ContagiousVorrac()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castAndResolveSorcery(player1, 0, List.of());

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        harness.assertLife(player1, 18);
        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isEqualTo(1);
        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
    }
}

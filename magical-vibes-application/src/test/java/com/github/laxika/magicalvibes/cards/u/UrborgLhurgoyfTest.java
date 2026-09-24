package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({UrborgLhurgoyf.class, GrizzlyBears.class, Forest.class})
class UrborgLhurgoyfTest extends BaseCardTest {

    @Test
    @DisplayName("Power counts creature cards in its controller's graveyard and toughness is one greater")
    void powerAndToughnessCountCreatureCardsInOwnGraveyard() {
        Permanent lhurgoyf = new Permanent(new UrborgLhurgoyf());
        gd.playerBattlefields.get(player1.getId()).add(lhurgoyf);
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new GrizzlyBears(), new Forest()));
        harness.setGraveyard(player2, List.of(new GrizzlyBears()));

        assertThat(gqs.getEffectivePower(gd, lhurgoyf)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, lhurgoyf)).isEqualTo(3);
    }

    @Test
    @DisplayName("Without kicker, it does not mill when it enters")
    void doesNotMillWithoutKicker() {
        castAndResolve(List.of(), List.of(new Forest(), new Forest(), new Forest()));

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(3);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Each kicker mills three cards, including when both are paid")
    void millsThreeCardsPerKicker() {
        castAndResolve(List.of("{U}", "{B}"), List.of(
                new Forest(), new Forest(), new Forest(), new Forest(), new Forest(), new Forest()));

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(Card::getName)
                .containsExactly("Forest", "Forest", "Forest", "Forest", "Forest", "Forest");
    }

    private void castAndResolve(List<String> kickerPayments, List<Card> library) {
        harness.setHand(player1, List.of(new UrborgLhurgoyf()));
        harness.setGraveyard(player1, List.of());
        harness.setLibrary(player1, library);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        if (kickerPayments.contains("{U}")) {
            harness.addMana(player1, ManaColor.BLUE, 1);
        }
        if (kickerPayments.contains("{B}")) {
            harness.addMana(player1, ManaColor.BLACK, 1);
        }

        gs.playCard(gd, player1, 0, 0, null, null, List.of(), List.of(), false,
                null, null, null, null, null, false, null, null, null, null,
                kickerPayments, false);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}

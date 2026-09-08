package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Pacifism;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class WhispersOfEmrakulTest extends BaseCardTest {

    @Test
    @DisplayName("Target opponent discards one card at random without delirium")
    void targetOpponentDiscardsOneCardWithoutDelirium() {
        harness.setHand(player1, List.of(new WhispersOfEmrakul()));
        harness.setHand(player2, List.of(new GrizzlyBears(), new Shock(), new Pacifism()));
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new Shock(), new Pacifism()));
        addManaForSpell();

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).hasSize(2);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Target opponent discards two cards at random with delirium")
    void targetOpponentDiscardsTwoCardsWithDelirium() {
        harness.setHand(player1, List.of(new WhispersOfEmrakul()));
        harness.setHand(player2, List.of(new GrizzlyBears(), new Shock(), new Pacifism()));
        harness.setGraveyard(player1, List.of(
                new GrizzlyBears(), new Shock(), new Pacifism(), new Forest()));
        addManaForSpell();

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Cannot target yourself")
    void cannotTargetYourself() {
        harness.setHand(player1, List.of(new WhispersOfEmrakul()));
        addManaForSpell();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void addManaForSpell() {
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }
}

package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SeizeTheSecrets.class, Shock.class, GrizzlyBears.class})
class SeizeTheSecretsTest extends BaseCardTest {

    @Test
    @DisplayName("Costs {1}{U} after committing a crime this turn")
    void costsOneLessAfterCommittingCrime() {
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new SeizeTheSecrets()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castSorcery(player1, 0, List.of());

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Cannot use the reduced cost without committing a crime")
    void doesNotGetReductionWithoutCrime() {
        harness.setHand(player1, List.of(new SeizeTheSecrets()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, List.of()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Resolving Seize the Secrets draws two cards")
    void resolvingDrawsTwoCards() {
        Card firstCard = new GrizzlyBears();
        Card secondCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(firstCard, secondCard));
        harness.setHand(player1, List.of(new SeizeTheSecrets()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castSorcery(player1, 0, List.of());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(firstCard, secondCard);
    }
}

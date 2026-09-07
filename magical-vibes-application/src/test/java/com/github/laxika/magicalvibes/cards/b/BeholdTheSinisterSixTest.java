package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BeholdTheSinisterSix.class, GrizzlyBears.class, HillGiant.class, LlanowarElves.class})
class BeholdTheSinisterSixTest extends BaseCardTest {

    @Test
    @DisplayName("Returns up to six differently named creature cards")
    void returnsDifferentlyNamedCreatureCards() {
        Card bears = new GrizzlyBears();
        Card elves = new LlanowarElves();
        Card hillGiant = new HillGiant();
        harness.setGraveyard(player1, List.of(bears, elves, hillGiant));
        harness.setHand(player1, List.of(new BeholdTheSinisterSix()));
        addMana();

        harness.castSorcery(player1, 0, 0);

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.maxCount()).isEqualTo(3);
        assertThat(choice.validCardIds()).containsExactlyInAnyOrder(
                bears.getId(), elves.getId(), hillGiant.getId());

        harness.handleMultipleCardsChosen(player1, List.of(bears.getId(), elves.getId(), hillGiant.getId()));
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Grizzly Bears")).hasSize(1);
        assertThat(findPermanents(player1, "Llanowar Elves")).hasSize(1);
        assertThat(findPermanents(player1, "Hill Giant")).hasSize(1);
    }

    @Test
    @DisplayName("Rejects creature cards with duplicate names")
    void rejectsDuplicateNames() {
        Card firstBears = new GrizzlyBears();
        Card secondBears = new GrizzlyBears();
        Card elves = new LlanowarElves();
        harness.setGraveyard(player1, List.of(firstBears, secondBears, elves));
        harness.setHand(player1, List.of(new BeholdTheSinisterSix()));
        addMana();

        harness.castSorcery(player1, 0, 0);

        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(
                player1, List.of(firstBears.getId(), secondBears.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("different names");
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 6);
    }
}

package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DemandAnswers.class, Forest.class, Ornithopter.class})
class DemandAnswersTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing an artifact lets you draw two cards")
    void sacrificesArtifactAndDrawsTwoCards() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new Ornithopter());
        harness.setHand(player1, List.of(new DemandAnswers()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstantWithSacrifice(player1, 0, null, artifact.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Ornithopter");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Discarding a card lets you draw two cards")
    void discardsCardAndDrawsTwoCards() {
        harness.setHand(player1, List.of(new DemandAnswers(), new Forest()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstantWithDiscard(player1, 0, null, 1);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Forest");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Cannot be cast without an artifact or another card to discard")
    void cannotCastWithoutPaymentOption() {
        harness.setHand(player1, List.of(new DemandAnswers()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }
}

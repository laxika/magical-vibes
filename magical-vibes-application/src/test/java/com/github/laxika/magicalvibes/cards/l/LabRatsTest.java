package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LabRats.class})
class LabRatsTest extends BaseCardTest {

    @Test
    @DisplayName("Lab Rats creates a black Rat token")
    void createsRatToken() {
        harness.castFromHand(player1, new LabRats(), "{B}");
        harness.passBothPriorities();

        Permanent rat = findPermanent(player1, "Rat");
        assertThat(rat.getCard().isToken()).isTrue();
        assertThat(rat.getCard().getPower()).isEqualTo(1);
        assertThat(rat.getCard().getToughness()).isEqualTo(1);
        assertThat(rat.getCard().getColor()).isEqualTo(CardColor.BLACK);
        assertThat(rat.getCard().getSubtypes()).containsExactly(CardSubtype.RAT);
        harness.assertInGraveyard(player1, "Lab Rats");
    }

    @Test
    @DisplayName("Paying buyback returns Lab Rats to its owner's hand")
    void buybackReturnsToHand() {
        harness.setHand(player1, List.of(new LabRats()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castSorceryWithBuyback(player1, 0, null);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Rat"))
                .hasSize(1)
                .allMatch(permanent -> permanent.getCard().isToken());
        assertThat(gd.playerHands.get(player1.getId()))
                .extracting(card -> card.getName())
                .containsExactly("Lab Rats");
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(card -> card.getName())
                .doesNotContain("Lab Rats");
    }

    @Test
    @DisplayName("Insufficient mana to pay buyback leaves Lab Rats in hand")
    void buybackWithoutEnoughManaLeavesCardInHand() {
        harness.setHand(player1, List.of(new LabRats()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castSorceryWithBuyback(player1, 0, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInHand(player1, "Lab Rats");
        harness.assertNotInGraveyard(player1, "Lab Rats");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isEqualTo(4);
    }
}

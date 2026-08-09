package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class LabRatsTest extends BaseCardTest {

    @Test
    @DisplayName("Lab Rats creates a black Rat token")
    void createsRatToken() {
        harness.setHand(player1, List.of(new LabRats()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castSorcery(player1, 0, 0);
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

        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().isToken() && p.getCard().getName().equals("Rat")))
                .hasSize(1);
        assertThat(gd.playerHands.get(player1.getId()))
                .extracting(card -> card.getName())
                .containsExactly("Lab Rats");
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(card -> card.getName())
                .doesNotContain("Lab Rats");
    }
}

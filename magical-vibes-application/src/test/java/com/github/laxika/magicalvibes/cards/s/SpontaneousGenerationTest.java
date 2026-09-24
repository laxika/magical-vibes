package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SpontaneousGeneration.class, SpidersilkArmor.class})
class SpontaneousGenerationTest extends BaseCardTest {

    @Test
    @DisplayName("Creates one Saproling for each card in hand")
    void createsOneSaprolingPerCardInHand() {
        harness.setHand(player1, List.of(
                new SpontaneousGeneration(), new SpidersilkArmor(), new SpidersilkArmor(), new SpidersilkArmor()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Saproling")).isEqualTo(3);
    }

    @Test
    @DisplayName("Creates no Saprolings when no cards remain in hand")
    void createsNoSaprolingsWithEmptyHand() {
        harness.setHand(player1, List.of(new SpontaneousGeneration()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Saproling")).isZero();
    }

    @Test
    @DisplayName("Counts only cards in the spell controller's hand")
    void countsOnlyCardsInControllerHand() {
        harness.setHand(player1, List.of(new SpontaneousGeneration()));
        harness.setHand(player2, List.of(new SpidersilkArmor(), new SpidersilkArmor()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Saproling")).isZero();
        assertThat(countPermanents(player2, "Saproling")).isZero();
    }

    @Test
    @DisplayName("Saprolings are 1/1 green creatures")
    void createsOneOneGreenSaprolings() {
        harness.setHand(player1, List.of(new SpontaneousGeneration(), new SpidersilkArmor()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        Permanent token = findPermanent(player1, "Saproling");
        assertThat(token.getCard().getPower()).isEqualTo(1);
        assertThat(token.getCard().getToughness()).isEqualTo(1);
        assertThat(token.getCard().getColor()).isEqualTo(com.github.laxika.magicalvibes.model.CardColor.GREEN);
    }
}

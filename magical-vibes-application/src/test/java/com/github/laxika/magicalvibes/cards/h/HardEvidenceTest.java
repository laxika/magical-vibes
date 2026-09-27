package com.github.laxika.magicalvibes.cards.h;

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

@CardUsed(HardEvidence.class)
class HardEvidenceTest extends BaseCardTest {

    @Test
    @DisplayName("Creates a blue 0/3 Crab and investigates")
    void createsCrabAndClue() {
        harness.setHand(player1, List.of(new HardEvidence()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        Permanent crab = findPermanent(player1, "Crab");
        assertThat(crab.getCard().getPower()).isEqualTo(0);
        assertThat(crab.getCard().getToughness()).isEqualTo(3);
        assertThat(crab.getCard().getColor()).isEqualTo(CardColor.BLUE);
        assertThat(crab.getCard().getSubtypes()).containsExactly(CardSubtype.CRAB);
        assertThat(findPermanents(player1, "Clue")).hasSize(1);
    }
}

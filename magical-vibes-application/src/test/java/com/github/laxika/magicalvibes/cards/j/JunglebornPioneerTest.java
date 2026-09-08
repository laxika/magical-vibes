package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class JunglebornPioneerTest extends BaseCardTest {

    @Test
    @DisplayName("Entering the battlefield creates a 1/1 blue Merfolk token with hexproof")
    void etbCreatesHexproofMerfolkToken() {
        harness.setHand(player1, List.of(new JunglebornPioneer()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent merfolk = findPermanent(player1, "Merfolk");
        assertThat(merfolk.getCard().isToken()).isTrue();
        assertThat(merfolk.getCard().getPower()).isEqualTo(1);
        assertThat(merfolk.getCard().getToughness()).isEqualTo(1);
        assertThat(merfolk.getCard().getColor()).isEqualTo(CardColor.BLUE);
        assertThat(merfolk.getCard().getSubtypes()).contains(CardSubtype.MERFOLK);
        assertThat(gqs.hasKeyword(gd, merfolk, Keyword.HEXPROOF)).isTrue();
    }
}

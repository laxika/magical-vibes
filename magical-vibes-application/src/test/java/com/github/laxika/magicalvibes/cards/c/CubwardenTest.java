package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(Cubwarden.class)
class CubwardenTest extends BaseCardTest {

    @Test
    @DisplayName("Mutating creates two 1/1 white Cat tokens with lifelink")
    void mutatingCreatesTwoLifelinkCats() {
        Permanent cubwarden = addCreatureReady(player1, new Cubwarden());

        harness.inMutationScope(() -> harness.getTriggerCollectionService().checkMutateTriggers(
                gd, cubwarden, List.of(cubwarden.getCard()), player1.getId()));
        resolveAllTriggers();

        List<Permanent> cats = findPermanents(player1, "Cat");
        assertThat(cats).hasSize(2);
        assertThat(cats).allSatisfy(cat -> {
            assertThat(cat.getCard().getPower()).isEqualTo(1);
            assertThat(cat.getCard().getToughness()).isEqualTo(1);
            assertThat(cat.getCard().getColor()).isEqualTo(CardColor.WHITE);
            assertThat(cat.getCard().getSubtypes()).containsExactly(CardSubtype.CAT);
            assertThat(cat.getCard().getKeywords()).contains(Keyword.LIFELINK);
            assertThat(cat.getCard().isToken()).isTrue();
        });
    }
}

package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BenevolentOffering.class, GrizzlyBears.class})
class BenevolentOfferingTest extends BaseCardTest {

    @Test
    @DisplayName("Creates three flying Spirits for each player and gains life for all creatures")
    void createsSpiritsAndGainsLifeForCreatures() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        castBenevolentOffering();

        assertThat(findPermanents(player1, "Spirit")).hasSize(3);
        assertThat(findPermanents(player2, "Spirit")).hasSize(3);
        assertThat(findPermanents(player1, "Spirit")).allSatisfy(this::assertSpirit);
        assertThat(findPermanents(player2, "Spirit")).allSatisfy(this::assertSpirit);
        harness.assertLife(player1, 28);
        harness.assertLife(player2, 28);
    }

    @Test
    @DisplayName("Counts the Spirits created by the spell when determining life gained")
    void countsCreatedSpirits() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        castBenevolentOffering();

        harness.assertLife(player1, 26);
        harness.assertLife(player2, 26);
    }

    private void castBenevolentOffering() {
        harness.castFromHand(player1, new BenevolentOffering(), "{3}{W}");
        harness.passBothPriorities();
    }

    private void assertSpirit(Permanent spirit) {
        assertThat(spirit.getCard().getColor()).isEqualTo(CardColor.WHITE);
        assertThat(spirit.getCard().getSubtypes()).contains(CardSubtype.SPIRIT);
        assertThat(spirit.getEffectivePower()).isEqualTo(1);
        assertThat(spirit.getEffectiveToughness()).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, spirit, Keyword.FLYING)).isTrue();
    }
}

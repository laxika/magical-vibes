package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(BirthingBoughs.class)
class BirthingBoughsTest extends BaseCardTest {

    @Test
    @DisplayName("Paying {4} and tapping Birthing Boughs creates a 2/2 colorless Shapeshifter with changeling")
    void createsChangelingToken() {
        harness.addToBattlefield(player1, new BirthingBoughs());
        Permanent boughs = findPermanent(player1, "Birthing Boughs");
        boughs.setSummoningSick(false);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        Permanent token = findPermanent(player1, "Shapeshifter");
        assertThat(boughs.isTapped()).isTrue();
        assertThat(token.getCard().getPower()).isEqualTo(2);
        assertThat(token.getCard().getToughness()).isEqualTo(2);
        assertThat(token.getCard().getColor()).isNull();
        assertThat(token.getCard().getSubtypes()).containsExactly(CardSubtype.SHAPESHIFTER);
        assertThat(token.getCard().getKeywords()).contains(Keyword.CHANGELING);
    }
}

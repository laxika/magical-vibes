package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ErrandOfDuty.class})
class ErrandOfDutyTest extends BaseCardTest {

    @Test
    @DisplayName("Cast creates a single 1/1 Knight token with banding")
    void createsKnightTokenWithBanding() {
        harness.castFromHand(player1, new ErrandOfDuty(), "{1}{W}");
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        List<Permanent> knights = findPermanents(player1, "Knight");
        assertThat(knights).hasSize(1);
        Permanent knight = knights.getFirst();
        assertThat(knight.getEffectivePower()).isEqualTo(1);
        assertThat(knight.getEffectiveToughness()).isEqualTo(1);
        assertThat(knight.hasKeyword(Keyword.BANDING)).isTrue();
    }

    @Test
    @DisplayName("Creates a white Knight creature token")
    void createsWhiteKnightCreatureToken() {
        harness.castFromHand(player1, new ErrandOfDuty(), "{1}{W}");
        harness.passBothPriorities();

        Permanent knight = findPermanent(player1, "Knight");
        assertThat(knight.getCard().getColor()).isEqualTo(CardColor.WHITE);
        assertThat(knight.getCard().getType()).isEqualTo(CardType.CREATURE);
        assertThat(knight.getCard().getSubtypes()).containsExactly(CardSubtype.KNIGHT);
        assertThat(knight.getCard().isToken()).isTrue();
    }
}

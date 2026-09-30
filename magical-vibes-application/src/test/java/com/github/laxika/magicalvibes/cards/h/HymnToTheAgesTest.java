package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HymnToTheAges.class, Forest.class})
class HymnToTheAgesTest extends BaseCardTest {

    @Test
    void drawsItsStartingIntensityAndIntensifiesOwnedChorusCards() {
        HymnToTheAges hymn = new HymnToTheAges();
        HymnToTheAges otherHymn = new HymnToTheAges();
        harness.setLibrary(player1, List.of(new Forest(), otherHymn));
        harness.setHand(player1, List.of(hymn));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId()))
                .hasSize(1)
                .first()
                .isInstanceOf(Forest.class);
        assertThat(gd.getCardIntensity(hymn.getId())).isEqualTo(2);
        assertThat(gd.getCardIntensity(otherHymn.getId())).isEqualTo(1);
    }
}

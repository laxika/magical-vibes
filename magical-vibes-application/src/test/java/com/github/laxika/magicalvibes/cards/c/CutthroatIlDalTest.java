package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.b.BlindPhantasm;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CutthroatIlDal.class, BlindPhantasm.class})
class CutthroatIlDalTest extends BaseCardTest {

    @Test
    @DisplayName("Has shadow while its controller has no cards in hand")
    void hasShadowWithEmptyControllerHand() {
        harness.setHand(player1, List.of());
        Permanent cutthroat = harness.addToBattlefieldAndReturn(player1, new CutthroatIlDal());

        assertThat(gqs.hasKeyword(gd, cutthroat, Keyword.SHADOW)).isTrue();
    }

    @Test
    @DisplayName("Does not have shadow while its controller has a card in hand")
    void noShadowWithNonEmptyControllerHand() {
        harness.setHand(player1, List.of(new BlindPhantasm()));
        Permanent cutthroat = harness.addToBattlefieldAndReturn(player1, new CutthroatIlDal());

        assertThat(gqs.hasKeyword(gd, cutthroat, Keyword.SHADOW)).isFalse();
    }

    @Test
    @DisplayName("Shadow changes as its controller's hand changes")
    void shadowTracksControllerHand() {
        harness.setHand(player1, List.of());
        Permanent cutthroat = harness.addToBattlefieldAndReturn(player1, new CutthroatIlDal());

        assertThat(gqs.hasKeyword(gd, cutthroat, Keyword.SHADOW)).isTrue();

        harness.setHand(player1, List.of(new BlindPhantasm()));
        assertThat(gqs.hasKeyword(gd, cutthroat, Keyword.SHADOW)).isFalse();

        harness.setHand(player1, List.of());
        assertThat(gqs.hasKeyword(gd, cutthroat, Keyword.SHADOW)).isTrue();
    }

    @Test
    @DisplayName("Uses the creature controller's hand, not an opponent's hand")
    void usesCreatureControllersHand() {
        harness.setHand(player1, List.of(new BlindPhantasm()));
        harness.setHand(player2, List.of());
        Permanent cutthroat = harness.addToBattlefieldAndReturn(player2, new CutthroatIlDal());

        assertThat(gqs.hasKeyword(gd, cutthroat, Keyword.SHADOW)).isTrue();

        harness.setHand(player2, List.of(new BlindPhantasm()));
        assertThat(gqs.hasKeyword(gd, cutthroat, Keyword.SHADOW)).isFalse();
    }
}

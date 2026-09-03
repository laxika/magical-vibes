package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WaitingInTheWeeds.class, Forest.class})
class WaitingInTheWeedsTest extends BaseCardTest {

    @Test
    @DisplayName("Each player creates a Cat for each untapped Forest they control")
    void eachPlayerCreatesCatsForOwnUntappedForests() {
        // Player 1: three Forests, one tapped -> two untapped.
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Forest());
        Permanent tappedForest = harness.addToBattlefieldAndReturn(player1, new Forest());
        tappedForest.tap();

        // Player 2: one untapped Forest.
        harness.addToBattlefield(player2, new Forest());

        harness.castFromHand(player1, new WaitingInTheWeeds(), "{1}{G}{G}");
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Cat")).isEqualTo(2);
        assertThat(countPermanents(player2, "Cat")).isEqualTo(1);

        Permanent cat = findPermanent(player1, "Cat");
        assertThat(cat.getCard().getPower()).isEqualTo(1);
        assertThat(cat.getCard().getToughness()).isEqualTo(1);
    }

    @Test
    @DisplayName("A player with only tapped Forests creates no Cats")
    void noUntappedForestsCreatesNoCats() {
        Permanent tapped = harness.addToBattlefieldAndReturn(player1, new Forest());
        tapped.tap();

        harness.castFromHand(player1, new WaitingInTheWeeds(), "{1}{G}{G}");
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Cat")).isZero();
        assertThat(countPermanents(player2, "Cat")).isZero();
    }

    @Test
    @DisplayName("Creates green Cat creature tokens")
    void createdTokensHaveCatCharacteristics() {
        harness.addToBattlefield(player1, new Forest());

        harness.castFromHand(player1, new WaitingInTheWeeds(), "{1}{G}{G}");
        harness.passBothPriorities();

        Permanent cat = findPermanent(player1, "Cat");
        assertThat(cat.getCard().isToken()).isTrue();
        assertThat(cat.getCard().getType()).isEqualTo(CardType.CREATURE);
        assertThat(cat.getCard().getColor()).isEqualTo(CardColor.GREEN);
        assertThat(cat.getCard().getSubtypes()).containsExactly(CardSubtype.CAT);
    }

    @Test
    @DisplayName("Counts untapped Forests when the spell resolves")
    void countsForestsAtResolution() {
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());

        harness.castFromHand(player1, new WaitingInTheWeeds(), "{1}{G}{G}");
        forest.tap();
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Cat")).isZero();
    }
}

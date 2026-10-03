package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.w.WrathOfGod;
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

@CardUsed({BroodWeaver.class, WrathOfGod.class})
class BroodWeaverTest extends BaseCardTest {

    @Test
    @DisplayName("When Brood Weaver dies, its controller creates a 1/2 green Spider with reach")
    void deathCreatesSpiderToken() {
        harness.addToBattlefield(player1, new BroodWeaver());

        harness.forceActivePlayer(player2);
        harness.castFromHand(player2, new WrathOfGod(), "{2}{W}{W}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Brood Weaver");
        List<Permanent> spiders = findPermanents(player1, "Spider");
        assertThat(spiders).singleElement().satisfies(spider -> {
            assertThat(spider.getCard().getColor()).isEqualTo(CardColor.GREEN);
            assertThat(spider.getCard().getPower()).isEqualTo(1);
            assertThat(spider.getCard().getToughness()).isEqualTo(2);
            assertThat(spider.getCard().getSubtypes()).containsExactly(CardSubtype.SPIDER);
            assertThat(spider.getCard().getKeywords()).contains(Keyword.REACH);
        });
    }

    @Test
    @DisplayName("Entering the battlefield does not create a Spider")
    void enteringDoesNotCreateToken() {
        harness.castFromHand(player1, new BroodWeaver(), "{3}{G}");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Brood Weaver");
        assertThat(findPermanents(player1, "Spider")).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Simultaneous deaths create one Spider per Weaver for each controller")
    void simultaneousDeathsCreateTokensForEachController() {
        harness.addToBattlefield(player1, new BroodWeaver());
        harness.addToBattlefield(player1, new BroodWeaver());
        harness.addToBattlefield(player2, new BroodWeaver());
        harness.castFromHand(player1, new WrathOfGod(), "{2}{W}{W}");
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Brood Weaver")).isEmpty();
        assertThat(findPermanents(player2, "Brood Weaver")).isEmpty();
        assertThat(findPermanents(player1, "Spider")).isEmpty();
        assertThat(findPermanents(player2, "Spider")).isEmpty();

        resolveAllTriggers();

        assertThat(findPermanents(player1, "Spider")).hasSize(2);
        assertThat(findPermanents(player2, "Spider")).hasSize(1);
    }
}

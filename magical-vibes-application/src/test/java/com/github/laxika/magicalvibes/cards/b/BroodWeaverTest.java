package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.w.WrathOfGod;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
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

        harness.setHand(player2, List.of(new WrathOfGod()));
        harness.addMana(player2, ManaColor.WHITE, 4);
        harness.forceActivePlayer(player2);
        harness.getGameService().playCard(harness.getGameData(), player2, 0, 0, null, null);
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
}

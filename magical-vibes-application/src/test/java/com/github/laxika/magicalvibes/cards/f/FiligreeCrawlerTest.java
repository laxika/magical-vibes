package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.w.WrathOfGod;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FiligreeCrawler.class, WrathOfGod.class, Shock.class})
class FiligreeCrawlerTest extends BaseCardTest {

    @Test
    void whenFiligreeCrawlerDiesCreateThopterToken() {
        harness.addToBattlefield(player1, new FiligreeCrawler());

        harness.setHand(player1, List.of(new WrathOfGod()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        List<Permanent> tokens = findPermanents(player1, "Thopter");
        assertThat(tokens).hasSize(1);

        Permanent thopter = tokens.getFirst();
        assertThat(thopter.getCard().getPower()).isEqualTo(1);
        assertThat(thopter.getCard().getToughness()).isEqualTo(1);
        assertThat(thopter.getCard().getColors()).isEmpty();
        assertThat(thopter.getCard().getSubtypes()).contains(CardSubtype.THOPTER);
        assertThat(thopter.getCard().hasType(CardType.ARTIFACT)).isTrue();
        assertThat(thopter.hasKeyword(Keyword.FLYING)).isTrue();
        assertThat(thopter.getCard().isToken()).isTrue();
    }

    @Test
    void lethalDamageCreatesTokenForTheDyingCreaturesController() {
        harness.addToBattlefield(player2, new FiligreeCrawler());
        Permanent crawler = findPermanent(player2, "Filigree Crawler");
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, crawler.getId());

        assertThat(findPermanents(player2, "Filigree Crawler")).isEmpty();
        assertThat(findPermanents(player2, "Thopter")).isEmpty();
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(findPermanents(player2, "Thopter")).hasSize(1);
        assertThat(findPermanents(player1, "Thopter")).isEmpty();
        assertThat(gd.stack).isEmpty();
    }
}

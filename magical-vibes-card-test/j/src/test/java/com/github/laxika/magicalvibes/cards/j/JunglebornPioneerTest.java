package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({JunglebornPioneer.class})
class JunglebornPioneerTest extends BaseCardTest {

    @Test
    @DisplayName("Entering the battlefield creates a 1/1 blue Merfolk token with hexproof")
    void etbCreatesHexproofMerfolkToken() {
        harness.castFromHand(player1, new JunglebornPioneer(), "{2}{G}");
        resolveAllTriggers();

        Permanent merfolk = findPermanent(player1, "Merfolk");
        assertThat(merfolk.getCard().isToken()).isTrue();
        assertThat(merfolk.getCard().getPower()).isEqualTo(1);
        assertThat(merfolk.getCard().getToughness()).isEqualTo(1);
        assertThat(merfolk.getCard().getColor()).isEqualTo(CardColor.BLUE);
        assertThat(merfolk.getCard().getSubtypes()).contains(CardSubtype.MERFOLK);
        assertThat(gqs.hasKeyword(gd, merfolk, Keyword.HEXPROOF)).isTrue();
    }

    @Test
    @DisplayName("The token is created even if Pioneer leaves before its trigger resolves")
    void tokenIsCreatedAfterPioneerLeaves() {
        harness.castFromHand(player2, new JunglebornPioneer(), "{2}{G}");
        harness.passBothPriorities();

        Permanent pioneer = findPermanent(player2, "Jungleborn Pioneer");
        assertThat(gd.playerBattlefields.get(player2.getId())).hasSize(1);
        assertThat(gd.stack).hasSize(1);
        gd.playerBattlefields.get(player2.getId()).remove(pioneer);
        gd.playerGraveyards.get(player2.getId()).add(pioneer.getCard());

        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player2.getId())).singleElement().satisfies(token -> {
            assertThat(token.getCard().isToken()).isTrue();
            assertThat(token.getCard().getType()).isEqualTo(CardType.CREATURE);
            assertThat(token.isTapped()).isFalse();
            assertThat(gqs.hasKeyword(gd, token, Keyword.HEXPROOF)).isTrue();
        });
        assertThat(gd.stack).isEmpty();
    }
}

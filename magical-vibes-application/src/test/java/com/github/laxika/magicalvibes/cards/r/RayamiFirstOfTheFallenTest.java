package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RayamiFirstOfTheFallen.class, SerraAngel.class})
class RayamiFirstOfTheFallenTest extends BaseCardTest {

    @Test
    void exilesNontokenCreaturesWithBloodCountersAndGainsTheirKeywords() {
        Permanent rayami = harness.addToBattlefieldAndReturn(player1, new RayamiFirstOfTheFallen());
        Permanent angel = harness.addToBattlefieldAndReturn(player2, new SerraAngel());

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, angel));

        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(angel.getCard());
        assertThat(gd.exiledCardsWithBloodCounters).contains(angel.getCard().getId());
        assertThat(gd.getCardsExiledByPermanent(rayami.getId())).contains(angel.getCard());
        assertThat(gqs.hasKeyword(gd, rayami, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, rayami, Keyword.VIGILANCE)).isTrue();
        harness.assertNotInGraveyard(player2, "Serra Angel");
    }

    @Test
    void doesNotReplaceTokenCreatureDeaths() {
        Permanent rayami = harness.addToBattlefieldAndReturn(player1, new RayamiFirstOfTheFallen());
        Card tokenCard = new Card();
        tokenCard.setName("Flying Token");
        tokenCard.setType(CardType.CREATURE);
        tokenCard.setColor(CardColor.WHITE);
        tokenCard.setSubtypes(List.of(CardSubtype.ANGEL));
        tokenCard.setKeywords(Set.of(Keyword.FLYING));
        tokenCard.setPower(1);
        tokenCard.setToughness(1);
        tokenCard.setToken(true);
        Permanent token = harness.addToBattlefieldAndReturn(player2, tokenCard);

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, token));

        assertThat(gd.getPlayerExiledCards(player2.getId())).doesNotContain(tokenCard);
        assertThat(gd.exiledCardsWithBloodCounters).doesNotContain(tokenCard.getId());
        assertThat(gd.getCardsExiledByPermanent(rayami.getId())).doesNotContain(tokenCard);
        assertThat(gqs.hasKeyword(gd, rayami, Keyword.FLYING)).isFalse();
    }
}

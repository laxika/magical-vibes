package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.d.DryadArbor;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WildgroveSummoner.class, Forest.class, DryadArbor.class, Island.class})
class WildgroveSummonerTest extends BaseCardTest {

    @Test
    void perpetuallyTurnsForestsInHandAndLibraryIntoTreefolk() {
        harness.setHand(player1, List.of(new Forest()));
        harness.setLibrary(player1, List.of(new DryadArbor(), new Island()));

        harness.enterBattlefieldAndReturn(player1, new WildgroveSummoner());
        resolveAllTriggers();

        Permanent handForest = harness.enterBattlefieldAndReturn(
                player1, gd.playerHands.get(player1.getId()).getFirst());
        Permanent libraryForest = harness.enterBattlefieldAndReturn(
                player1, gd.playerDecks.get(player1.getId()).getFirst());

        for (Permanent forest : List.of(handForest, libraryForest)) {
            assertThat(gqs.getEffectiveCardTypes(gd, forest)).contains(CardType.LAND, CardType.CREATURE);
            assertThat(forest.getCard().getSubtypes()).contains(CardSubtype.FOREST, CardSubtype.TREEFOLK);
            assertThat(gqs.getEffectivePower(gd, forest)).isEqualTo(3);
            assertThat(gqs.getEffectiveToughness(gd, forest)).isEqualTo(4);
            assertThat(gqs.hasKeyword(gd, forest, Keyword.REACH)).isTrue();
            assertThat(gqs.hasKeyword(gd, forest, Keyword.HASTE)).isTrue();
        }

        Card unchangedIsland = gd.playerDecks.get(player1.getId()).getLast();
        assertThat(unchangedIsland.getType()).isEqualTo(CardType.LAND);
        assertThat(unchangedIsland.getPower()).isNull();
    }

    @Test
    void deathTriggerSeeksUpToTwoForestsOntoTheBattlefield() {
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Island()));
        Permanent summoner = harness.addToBattlefieldAndReturn(player1, new WildgroveSummoner());

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, summoner));
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Forest")).hasSize(2);
        assertThat(gd.playerDecks.get(player1.getId()))
                .extracting(Card::getName)
                .containsExactly("Island");
    }
}

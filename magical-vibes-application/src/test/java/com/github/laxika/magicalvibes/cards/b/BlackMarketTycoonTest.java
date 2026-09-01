package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(BlackMarketTycoon.class)
class BlackMarketTycoonTest extends BaseCardTest {

    @Test
    @DisplayName("Creates a Treasure token when its ability resolves")
    void createsTreasureToken() {
        Permanent tycoon = addCreatureReady(player1, new BlackMarketTycoon());

        harness.activateAbility(player1, indexOf(tycoon), null, null);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Treasure")).hasSize(1);
    }

    @Test
    @DisplayName("Deals two damage for each Treasure controlled at upkeep")
    void dealsDamageForEachTreasureControlled() {
        addCreatureReady(player1, new BlackMarketTycoon());
        addTreasure(player1);
        addTreasure(player1);
        addTreasure(player2);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        harness.assertLife(player1, 16);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Counts Treasures when the upkeep ability resolves")
    void countsTreasuresAtResolution() {
        addCreatureReady(player1, new BlackMarketTycoon());

        advanceToUpkeep(player1);
        addTreasure(player1);
        harness.passBothPriorities();

        harness.assertLife(player1, 18);
    }

    private int indexOf(Permanent permanent) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(permanent);
    }

    private Permanent addTreasure(Player player) {
        Card card = new Card();
        card.setName("Treasure");
        card.setType(CardType.ARTIFACT);
        card.setSubtypes(List.of(CardSubtype.TREASURE));
        card.setToken(true);

        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}

package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.a.AncientDen;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.r.RestInPeace;
import com.github.laxika.magicalvibes.cards.s.SolRing;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LootTheKeyToEverything.class, AncientDen.class, Forest.class, GrizzlyBears.class,
        RestInPeace.class, SolRing.class})
class LootTheKeyToEverythingTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles one card per distinct type among other nonland permanents")
    void exilesPerDistinctOtherNonlandPermanentType() {
        harness.addToBattlefield(player1, new LootTheKeyToEverything());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new SolRing());
        harness.addToBattlefield(player1, new RestInPeace());
        List<Card> library = List.of(new Forest(), new Forest(), new Forest(), new Forest());
        harness.setLibrary(player1, library);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .containsExactlyElementsOf(library.subList(0, 3));
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(library.get(3));
        assertThat(gd.exilePlayPermissions).containsKeys(
                library.get(0).getId(), library.get(1).getId(), library.get(2).getId());
    }

    @Test
    @DisplayName("Excludes Loot itself and all lands, including artifact lands")
    void excludesSourceAndLands() {
        harness.addToBattlefield(player1, new LootTheKeyToEverything());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new AncientDen());
        Card top = new Forest();
        harness.setLibrary(player1, List.of(top));

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(top);
        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(top);
    }

    @Test
    @DisplayName("Counts permanents when the upkeep ability resolves")
    void countsAtResolution() {
        harness.addToBattlefield(player1, new LootTheKeyToEverything());
        List<Card> library = List.of(new Forest(), new Forest());
        harness.setLibrary(player1, library);

        advanceToUpkeep(player1);
        harness.addToBattlefield(player1, new SolRing());
        harness.addToBattlefield(player1, new RestInPeace());
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactlyElementsOf(library);
    }
}

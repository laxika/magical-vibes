package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.y.YavimayaHollow;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SowingSalt.class, YavimayaHollow.class, Plains.class})
class SowingSaltTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles the target nonbasic land and every same-name copy from graveyard, hand, and library")
    void exilesTargetAndAllCopies() {
        harness.addToBattlefield(player2, new YavimayaHollow());
        harness.setHand(player2, List.of(new YavimayaHollow()));
        harness.setGraveyard(player2, List.of(new YavimayaHollow()));
        harness.setHand(player1, List.of(new SowingSalt(), new YavimayaHollow()));

        harness.setLibrary(player2, List.of(new YavimayaHollow(), new Plains()));

        harness.addMana(player1, ManaColor.RED, 4);

        UUID targetId = harness.getPermanentId(player2, "Yavimaya Hollow");
        harness.castAndResolveSorcery(player1, 0, 0, targetId);

        harness.assertNotOnBattlefield(player2, "Yavimaya Hollow");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .filteredOn(c -> c.getName().equals("Yavimaya Hollow"))
                .hasSize(4);
        harness.assertNotInHand(player2, "Yavimaya Hollow");
        harness.assertNotInGraveyard(player2, "Yavimaya Hollow");
        assertThat(gd.playerDecks.get(player2.getId()))
                .noneMatch(c -> c.getName().equals("Yavimaya Hollow"));
        assertThat(gd.playerDecks.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Plains"));
        harness.assertInHand(player1, "Yavimaya Hollow");
    }

    @Test
    @DisplayName("Does not search if the target land leaves before resolution")
    void fizzlesIfTargetRemoved() {
        harness.addToBattlefield(player2, new YavimayaHollow());
        harness.setHand(player2, List.of(new YavimayaHollow()));
        harness.setHand(player1, List.of(new SowingSalt()));
        harness.addMana(player1, ManaColor.RED, 4);

        UUID targetId = harness.getPermanentId(player2, "Yavimaya Hollow");
        harness.castSorcery(player1, 0, 0, targetId);
        gd.playerBattlefields.get(player2.getId()).clear();
        harness.passBothPriorities();

        harness.assertInHand(player2, "Yavimaya Hollow");
        harness.assertInGraveyard(player1, "Sowing Salt");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .noneMatch(c -> c.getName().equals("Yavimaya Hollow"));
    }

    @Test
    @DisplayName("Cannot target a basic land")
    void cannotTargetBasicLand() {
        harness.addToBattlefield(player2, new Plains());
        harness.setHand(player1, List.of(new SowingSalt()));
        harness.addMana(player1, ManaColor.RED, 4);

        UUID targetId = harness.getPermanentId(player2, "Plains");
        assertThatThrownBy(() -> harness.castSorcery(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class);
    }
}

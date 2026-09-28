package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.t.TendoIceBridge;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SowingSalt.class, TendoIceBridge.class, Plains.class, Shuko.class})
class SowingSaltTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles the target nonbasic land and every same-name copy from graveyard, hand, and library")
    void exilesTargetAndAllCopies() {
        harness.addToBattlefield(player2, new TendoIceBridge());
        harness.setHand(player2, List.of(new TendoIceBridge()));
        harness.setGraveyard(player2, List.of(new TendoIceBridge()));
        harness.setHand(player1, List.of(new SowingSalt(), new TendoIceBridge()));

        harness.setLibrary(player2, List.of(new TendoIceBridge(), new Plains()));

        harness.addMana(player1, ManaColor.RED, 4);

        UUID targetId = harness.getPermanentId(player2, "Tendo Ice Bridge");
        harness.castAndResolveSorcery(player1, 0, 0, targetId);

        harness.assertNotOnBattlefield(player2, "Tendo Ice Bridge");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .filteredOn(c -> c.getName().equals("Tendo Ice Bridge"))
                .hasSize(4);
        harness.assertNotInHand(player2, "Tendo Ice Bridge");
        harness.assertNotInGraveyard(player2, "Tendo Ice Bridge");
        assertThat(gd.playerDecks.get(player2.getId()))
                .noneMatch(c -> c.getName().equals("Tendo Ice Bridge"));
        assertThat(gd.playerDecks.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Plains"));
        harness.assertInHand(player1, "Tendo Ice Bridge");
    }

    @Test
    @DisplayName("Does not search if the target land leaves before resolution")
    void fizzlesIfTargetRemoved() {
        harness.addToBattlefield(player2, new TendoIceBridge());
        harness.setHand(player2, List.of(new TendoIceBridge()));
        harness.setHand(player1, List.of(new SowingSalt()));
        harness.addMana(player1, ManaColor.RED, 4);

        UUID targetId = harness.getPermanentId(player2, "Tendo Ice Bridge");
        harness.castSorcery(player1, 0, 0, targetId);
        gd.playerBattlefields.get(player2.getId()).clear();
        harness.passBothPriorities();

        harness.assertInHand(player2, "Tendo Ice Bridge");
        harness.assertInGraveyard(player1, "Sowing Salt");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .noneMatch(c -> c.getName().equals("Tendo Ice Bridge"));
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

    @Test
    @DisplayName("Cannot target a nonland permanent")
    void cannotTargetNonlandPermanent() {
        harness.addToBattlefield(player2, new Shuko());
        harness.setHand(player1, List.of(new SowingSalt()));
        harness.addMana(player1, ManaColor.RED, 4);

        UUID targetId = harness.getPermanentId(player2, "Shuko");
        assertThatThrownBy(() -> harness.castSorcery(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class);
    }
}

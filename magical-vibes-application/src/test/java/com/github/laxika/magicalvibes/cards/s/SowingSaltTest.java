package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.w.Wasteland;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SowingSaltTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles the target nonbasic land and every same-name copy from graveyard, hand, and library")
    void exilesTargetAndAllCopies() {
        harness.addToBattlefield(player2, new Wasteland());
        harness.setHand(player2, List.of(new Wasteland()));
        harness.setGraveyard(player2, new ArrayList<>(List.of(new Wasteland())));

        GameData gd = harness.getGameData();
        gd.playerDecks.get(player2.getId()).clear();
        gd.playerDecks.get(player2.getId()).add(new Wasteland());
        gd.playerDecks.get(player2.getId()).add(new Plains());

        harness.setHand(player1, List.of(new SowingSalt()));
        harness.addMana(player1, ManaColor.RED, 4);

        UUID targetId = harness.getPermanentId(player2, "Wasteland");
        harness.castSorcery(player1, 0, targetId);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Wasteland");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .filteredOn(c -> c.getName().equals("Wasteland"))
                .hasSize(4);
        harness.assertNotInHand(player2, "Wasteland");
        harness.assertNotInGraveyard(player2, "Wasteland");
        assertThat(gd.playerDecks.get(player2.getId()))
                .noneMatch(c -> c.getName().equals("Wasteland"));
        assertThat(gd.playerDecks.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Plains"));
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

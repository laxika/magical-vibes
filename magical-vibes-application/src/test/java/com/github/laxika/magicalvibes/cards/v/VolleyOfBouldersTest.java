package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class VolleyOfBouldersTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 6 damage divided among creatures and players")
    void dividesDamageAmongCreaturesAndPlayers() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new VolleyOfBoulders()));
        harness.addMana(player1, ManaColor.RED, 9);
        int lifeBefore = gd.getLife(player2.getId());

        harness.castSorcery(player1, 0, Map.of(bears.getId(), 2, player2.getId(), 4));
        harness.passBothPriorities();

        GameData gameData = harness.getGameData();
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        assertThat(gameData.getLife(player2.getId())).isEqualTo(lifeBefore - 4);
    }

    @Test
    @DisplayName("Assignments must sum to 6 damage")
    void assignmentsMustSumToSix() {
        Permanent giant = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        harness.setHand(player1, List.of(new VolleyOfBoulders()));
        harness.addMana(player1, ManaColor.RED, 9);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, Map.of(giant.getId(), 5)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Flashback repeats the damage and exiles the spell")
    void flashbackDealsDamageAndExilesSpell() {
        harness.setGraveyard(player1, List.of(new VolleyOfBoulders()));
        harness.addMana(player1, ManaColor.RED, 6);
        int lifeBefore = gd.getLife(player2.getId());

        harness.castFlashback(player1, 0, Map.of(player2.getId(), 6));
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBefore - 6);
        harness.assertNotInGraveyard(player1, "Volley of Boulders");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Volley of Boulders"));
    }
}

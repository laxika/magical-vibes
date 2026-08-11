package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class AngerOfTheGodsTest extends BaseCardTest {

    private void castAnger() {
        harness.setHand(player1, List.of(new AngerOfTheGods()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Creatures killed by the damage are exiled instead of going to the graveyard")
    void killedCreaturesAreExiled() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());

        castAnger();

        GameData gd = harness.getGameData();
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
        harness.assertNotInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Creatures that survive the damage remain on the battlefield")
    void survivingCreaturesRemain() {
        Permanent survivor = harness.addToBattlefieldAndReturn(player2, new SerraAngel());

        castAnger();

        harness.assertOnBattlefield(player2, "Serra Angel");
        assertThat(survivor.getMarkedDamage()).isEqualTo(3);
        assertThat(harness.getGameData().getPlayerExiledCards(player2.getId()))
                .noneMatch(c -> c.getName().equals("Serra Angel"));
    }

    @Test
    @DisplayName("Players are not dealt damage")
    void playersAreNotDamaged() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        castAnger();

        GameData gd = harness.getGameData();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }
}

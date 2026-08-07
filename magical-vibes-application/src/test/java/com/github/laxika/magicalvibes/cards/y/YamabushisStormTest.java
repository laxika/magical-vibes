package com.github.laxika.magicalvibes.cards.y;

import com.github.laxika.magicalvibes.cards.f.FugitiveWizard;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.PaladinEnVec;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class YamabushisStormTest extends BaseCardTest {

    private void castStorm() {
        harness.setHand(player1, List.of(new YamabushisStorm()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Creatures killed by the damage are exiled instead of going to the graveyard")
    void killedCreaturesAreExiled() {
        harness.addToBattlefield(player1, new FugitiveWizard());
        harness.addToBattlefield(player2, new FugitiveWizard());

        castStorm();

        GameData gd = harness.getGameData();
        harness.assertNotOnBattlefield(player1, "Fugitive Wizard");
        harness.assertNotOnBattlefield(player2, "Fugitive Wizard");
        harness.assertNotInGraveyard(player1, "Fugitive Wizard");
        harness.assertNotInGraveyard(player2, "Fugitive Wizard");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Fugitive Wizard"));
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(c -> c.getName().equals("Fugitive Wizard"));
    }

    @Test
    @DisplayName("Creatures that survive the damage stay on the battlefield and are not exiled")
    void survivingCreaturesRemain() {
        harness.addToBattlefield(player2, new GrizzlyBears());

        castStorm();

        GameData gd = harness.getGameData();
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .noneMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("A creature with protection from red is not dealt damage")
    void protectedCreatureIsUntouched() {
        harness.addToBattlefield(player2, new PaladinEnVec());

        castStorm();

        harness.assertOnBattlefield(player2, "Paladin en-Vec");
    }

    @Test
    @DisplayName("Players are not dealt damage")
    void playersAreNotDamaged() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        castStorm();

        GameData gd = harness.getGameData();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }
}

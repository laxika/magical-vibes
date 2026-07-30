package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CryptCreeperTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing exiles a creature card from an opponent's graveyard")
    void exilesCreatureFromOpponentGraveyard() {
        Permanent creeper = addReadyCreeper(player1);
        Card bears = new GrizzlyBears();
        harness.setGraveyard(player2, new ArrayList<>(List.of(bears)));

        int index = gd.playerBattlefields.get(player1.getId()).indexOf(creeper);
        harness.activateAbility(player1, index, 0, null, bears.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();

        harness.assertNotInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Can exile a noncreature card from the controller's own graveyard")
    void exilesNoncreatureFromOwnGraveyard() {
        Permanent creeper = addReadyCreeper(player1);
        Card cancel = new com.github.laxika.magicalvibes.cards.c.Cancel();
        harness.setGraveyard(player1, new ArrayList<>(List.of(cancel)));

        int index = gd.playerBattlefields.get(player1.getId()).indexOf(creeper);
        harness.activateAbility(player1, index, 0, null, cancel.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();

        harness.assertNotInGraveyard(player1, "Cancel");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Cancel"));
    }

    @Test
    @DisplayName("Activating sacrifices Crypt Creeper")
    void activatingSacrificesCreeper() {
        Permanent creeper = addReadyCreeper(player1);
        Card bears = new GrizzlyBears();
        harness.setGraveyard(player2, new ArrayList<>(List.of(bears)));

        int index = gd.playerBattlefields.get(player1.getId()).indexOf(creeper);
        harness.activateAbility(player1, index, 0, null, bears.getId(), Zone.GRAVEYARD);

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(creeper);
        harness.assertInGraveyard(player1, "Crypt Creeper");
    }

    @Test
    @DisplayName("Rejects a target that is not in any graveyard")
    void rejectsTargetNotInGraveyard() {
        Permanent creeper = addReadyCreeper(player1);
        Card bears = new GrizzlyBears();

        int index = gd.playerBattlefields.get(player1.getId()).indexOf(creeper);

        assertThatThrownBy(() -> harness.activateAbility(player1, index, 0, null, bears.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Fizzles if the target leaves the graveyard before resolution")
    void fizzlesIfTargetRemoved() {
        Permanent creeper = addReadyCreeper(player1);
        Card bears = new GrizzlyBears();
        harness.setGraveyard(player2, new ArrayList<>(List.of(bears)));

        int index = gd.playerBattlefields.get(player1.getId()).indexOf(creeper);
        harness.activateAbility(player1, index, 0, null, bears.getId(), Zone.GRAVEYARD);

        gd.playerGraveyards.get(player2.getId()).clear();
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .noneMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Can activate while summoning sick (no tap cost)")
    void canActivateWithSummoningSickness() {
        CryptCreeper card = new CryptCreeper();
        Permanent creeper = new Permanent(card);
        creeper.setSummoningSick(true);
        gd.playerBattlefields.get(player1.getId()).add(creeper);

        Card bears = new GrizzlyBears();
        harness.setGraveyard(player2, new ArrayList<>(List.of(bears)));

        int index = gd.playerBattlefields.get(player1.getId()).indexOf(creeper);
        harness.activateAbility(player1, index, 0, null, bears.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    private Permanent addReadyCreeper(Player player) {
        CryptCreeper card = new CryptCreeper();
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}

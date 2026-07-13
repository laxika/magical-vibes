package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.c.Cancel;
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

class HeapDollTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrifices itself and exiles a card from controller's graveyard")
    void exilesFromOwnGraveyardAndSacrificesSelf() {
        Permanent doll = addReadyDoll(player1);
        Card bears = new GrizzlyBears();
        harness.setGraveyard(player1, new ArrayList<>(List.of(bears)));

        int dollIndex = gd.playerBattlefields.get(player1.getId()).indexOf(doll);
        harness.activateAbility(player1, dollIndex, 0, null, bears.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(doll);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(c -> c.getName().equals("Grizzly Bears"));
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Can exile a card from an opponent's graveyard")
    void exilesFromOpponentGraveyard() {
        Permanent doll = addReadyDoll(player1);
        Card bears = new GrizzlyBears();
        harness.setGraveyard(player2, new ArrayList<>(List.of(bears)));

        int dollIndex = gd.playerBattlefields.get(player1.getId()).indexOf(doll);
        harness.activateAbility(player1, dollIndex, 0, null, bears.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Can exile any card type, not just creatures")
    void exilesNonCreatureCard() {
        Permanent doll = addReadyDoll(player1);
        Card cancel = new Cancel();
        harness.setGraveyard(player1, new ArrayList<>(List.of(cancel)));

        int dollIndex = gd.playerBattlefields.get(player1.getId()).indexOf(doll);
        harness.activateAbility(player1, dollIndex, 0, null, cancel.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Cancel"));
    }

    @Test
    @DisplayName("Rejects a target not in any graveyard")
    void rejectsTargetNotInGraveyard() {
        Permanent doll = addReadyDoll(player1);
        Card bears = new GrizzlyBears();

        int dollIndex = gd.playerBattlefields.get(player1.getId()).indexOf(doll);

        assertThatThrownBy(() -> harness.activateAbility(player1, dollIndex, 0, null, bears.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addReadyDoll(Player player) {
        HeapDoll card = new HeapDoll();
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}

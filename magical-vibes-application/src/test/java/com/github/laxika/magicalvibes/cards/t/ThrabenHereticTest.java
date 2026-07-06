package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.c.Cancel;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.ExileTargetCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ThrabenHereticTest extends BaseCardTest {

    

    @Test
    @DisplayName("Exiles creature card from controller's graveyard")
    void exilesCreatureFromOwnGraveyard() {
        Permanent heretic = addReadyHeretic(player1);
        Card bears = new GrizzlyBears();
        harness.setGraveyard(player1, new ArrayList<>(List.of(bears)));

        int hereticIndex = gd.playerBattlefields.get(player1.getId()).indexOf(heretic);
        harness.activateAbility(player1, hereticIndex, 0, null, bears.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(c -> c.getName().equals("Grizzly Bears"));
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Can exile creature card from opponent's graveyard")
    void exilesCreatureFromOpponentGraveyard() {
        Permanent heretic = addReadyHeretic(player1);
        Card bears = new GrizzlyBears();
        harness.setGraveyard(player2, new ArrayList<>(List.of(bears)));

        int hereticIndex = gd.playerBattlefields.get(player1.getId()).indexOf(heretic);
        harness.activateAbility(player1, hereticIndex, 0, null, bears.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId()))
                .noneMatch(c -> c.getName().equals("Grizzly Bears"));
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Rejects non-creature card as target")
    void rejectsNonCreatureTarget() {
        Permanent heretic = addReadyHeretic(player1);
        Card cancel = new Cancel();
        harness.setGraveyard(player1, new ArrayList<>(List.of(cancel)));

        int hereticIndex = gd.playerBattlefields.get(player1.getId()).indexOf(heretic);

        assertThatThrownBy(() -> harness.activateAbility(player1, hereticIndex, 0, null, cancel.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Rejects target not in any graveyard")
    void rejectsTargetNotInGraveyard() {
        Permanent heretic = addReadyHeretic(player1);
        Card bears = new GrizzlyBears();

        int hereticIndex = gd.playerBattlefields.get(player1.getId()).indexOf(heretic);

        assertThatThrownBy(() -> harness.activateAbility(player1, hereticIndex, 0, null, bears.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not found");
    }

    @Test
    @DisplayName("Fizzles if target removed from graveyard before resolution")
    void fizzlesIfTargetRemoved() {
        Permanent heretic = addReadyHeretic(player1);
        Card bears = new GrizzlyBears();
        harness.setGraveyard(player1, new ArrayList<>(List.of(bears)));

        int hereticIndex = gd.playerBattlefields.get(player1.getId()).indexOf(heretic);
        harness.activateAbility(player1, hereticIndex, 0, null, bears.getId(), Zone.GRAVEYARD);

        gd.playerGraveyards.get(player1.getId()).clear();
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .noneMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Activating ability taps Thraben Heretic")
    void activatingTapsHeretic() {
        Permanent heretic = addReadyHeretic(player1);
        Card bears = new GrizzlyBears();
        harness.setGraveyard(player1, new ArrayList<>(List.of(bears)));

        assertThat(heretic.isTapped()).isFalse();

        int hereticIndex = gd.playerBattlefields.get(player1.getId()).indexOf(heretic);
        harness.activateAbility(player1, hereticIndex, 0, null, bears.getId(), Zone.GRAVEYARD);

        assertThat(heretic.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Cannot activate when already tapped")
    void cannotActivateWhenTapped() {
        Permanent heretic = addReadyHeretic(player1);
        heretic.tap();
        Card bears = new GrizzlyBears();
        harness.setGraveyard(player1, new ArrayList<>(List.of(bears)));

        int hereticIndex = gd.playerBattlefields.get(player1.getId()).indexOf(heretic);

        assertThatThrownBy(() -> harness.activateAbility(player1, hereticIndex, 0, null, bears.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot activate with summoning sickness")
    void cannotActivateWithSummoningSickness() {
        ThrabenHeretic card = new ThrabenHeretic();
        Permanent heretic = new Permanent(card);
        heretic.setSummoningSick(true);
        gd.playerBattlefields.get(player1.getId()).add(heretic);

        Card bears = new GrizzlyBears();
        harness.setGraveyard(player1, new ArrayList<>(List.of(bears)));

        int hereticIndex = gd.playerBattlefields.get(player1.getId()).indexOf(heretic);

        assertThatThrownBy(() -> harness.activateAbility(player1, hereticIndex, 0, null, bears.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addReadyHeretic(Player player) {
        ThrabenHeretic card = new ThrabenHeretic();
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}

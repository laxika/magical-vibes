package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
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

class CreakwoodGhoulTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles target card from a graveyard and controller gains 1 life")
    void exilesCardAndGainsLife() {
        Permanent ghoul = addReadyGhoul(player1);
        Card bears = new GrizzlyBears();
        harness.setGraveyard(player2, new ArrayList<>(List.of(bears)));
        harness.addMana(player1, ManaColor.BLACK, 2);
        int startLife = gd.playerLifeTotals.get(player1.getId());

        int index = gd.playerBattlefields.get(player1.getId()).indexOf(ghoul);
        harness.activateAbility(player1, index, 0, null, bears.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId()))
                .noneMatch(c -> c.getName().equals("Grizzly Bears"));
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(startLife + 1);
    }

    @Test
    @DisplayName("Can exile a card from controller's own graveyard")
    void exilesFromOwnGraveyard() {
        Permanent ghoul = addReadyGhoul(player1);
        Card bears = new GrizzlyBears();
        harness.setGraveyard(player1, new ArrayList<>(List.of(bears)));
        harness.addMana(player1, ManaColor.GREEN, 2);

        int index = gd.playerBattlefields.get(player1.getId()).indexOf(ghoul);
        harness.activateAbility(player1, index, 0, null, bears.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Cannot activate without enough mana")
    void cannotActivateWithoutMana() {
        Permanent ghoul = addReadyGhoul(player1);
        Card bears = new GrizzlyBears();
        harness.setGraveyard(player2, new ArrayList<>(List.of(bears)));
        harness.addMana(player1, ManaColor.BLACK, 1);

        int index = gd.playerBattlefields.get(player1.getId()).indexOf(ghoul);

        assertThatThrownBy(() -> harness.activateAbility(player1, index, 0, null, bears.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Rejects target not in any graveyard")
    void rejectsTargetNotInGraveyard() {
        Permanent ghoul = addReadyGhoul(player1);
        Card bears = new GrizzlyBears();
        harness.addMana(player1, ManaColor.BLACK, 2);

        int index = gd.playerBattlefields.get(player1.getId()).indexOf(ghoul);

        assertThatThrownBy(() -> harness.activateAbility(player1, index, 0, null, bears.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addReadyGhoul(Player player) {
        CreakwoodGhoul card = new CreakwoodGhoul();
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}

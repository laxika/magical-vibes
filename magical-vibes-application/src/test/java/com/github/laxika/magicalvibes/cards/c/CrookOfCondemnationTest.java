package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
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

class CrookOfCondemnationTest extends BaseCardTest {

    @Test
    @DisplayName("{1}, {T}: exiles target card from a graveyard")
    void tapExilesTargetGraveyardCard() {
        Permanent crook = addReadyCrook(player1);
        Card bears = new GrizzlyBears();
        harness.setGraveyard(player2, new ArrayList<>(List.of(bears)));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        int crookIndex = gd.playerBattlefields.get(player1.getId()).indexOf(crook);
        harness.activateAbility(player1, crookIndex, 0, null, bears.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(crook);
        assertThat(crook.isTapped()).isTrue();
    }

    @Test
    @DisplayName("{1}, {T}: can exile from own graveyard")
    void tapExilesFromOwnGraveyard() {
        Permanent crook = addReadyCrook(player1);
        Card shock = new Shock();
        harness.setGraveyard(player1, new ArrayList<>(List.of(shock)));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        int crookIndex = gd.playerBattlefields.get(player1.getId()).indexOf(crook);
        harness.activateAbility(player1, crookIndex, 0, null, shock.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Shock"));
    }

    @Test
    @DisplayName("{1}, {T}: rejects a target not in any graveyard")
    void rejectsTargetNotInGraveyard() {
        Permanent crook = addReadyCrook(player1);
        Card bears = new GrizzlyBears();
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        int crookIndex = gd.playerBattlefields.get(player1.getId()).indexOf(crook);

        assertThatThrownBy(() ->
                harness.activateAbility(player1, crookIndex, 0, null, bears.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("{1}, Exile self: exile all graveyards")
    void exileSelfExilesAllGraveyards() {
        Permanent crook = addReadyCrook(player1);
        harness.setGraveyard(player1, List.of(new Shock()));
        harness.setGraveyard(player2, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        int crookIndex = gd.playerBattlefields.get(player1.getId()).indexOf(crook);
        harness.activateAbility(player1, crookIndex, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(crook);
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Crook of Condemnation"));
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Shock"));
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    private Permanent addReadyCrook(Player player) {
        CrookOfCondemnation card = new CrookOfCondemnation();
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}

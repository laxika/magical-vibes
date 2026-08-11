package com.github.laxika.magicalvibes.cards.s;

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

class SteamclawTest extends BaseCardTest {

    @Test
    @DisplayName("{3}, {T}: exiles target card from a graveyard")
    void tapAbilityExilesTargetCard() {
        Permanent steamclaw = addReadySteamclaw(player1);
        Card bears = new GrizzlyBears();
        harness.setGraveyard(player2, new ArrayList<>(List.of(bears)));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        int steamclawIndex = gd.playerBattlefields.get(player1.getId()).indexOf(steamclaw);
        harness.activateAbility(player1, steamclawIndex, 0, null, bears.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(bears);
        assertThat(steamclaw.isTapped()).isTrue();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(steamclaw);
    }

    @Test
    @DisplayName("{1}, Sacrifice this artifact: exiles target card from a graveyard")
    void sacrificeAbilityExilesTargetCardAndSacrificesSteamclaw() {
        Permanent steamclaw = addReadySteamclaw(player1);
        Card bears = new GrizzlyBears();
        harness.setGraveyard(player1, new ArrayList<>(List.of(bears)));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        int steamclawIndex = gd.playerBattlefields.get(player1.getId()).indexOf(steamclaw);
        harness.activateAbility(player1, steamclawIndex, 1, null, bears.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(steamclaw);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(steamclaw.getCard());
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(bears);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(bears);
    }

    @Test
    @DisplayName("Both abilities reject a target that is not in a graveyard")
    void rejectsTargetNotInGraveyard() {
        Permanent steamclaw = addReadySteamclaw(player1);
        Card bears = new GrizzlyBears();
        int steamclawIndex = gd.playerBattlefields.get(player1.getId()).indexOf(steamclaw);

        harness.addMana(player1, ManaColor.COLORLESS, 3);
        assertThatThrownBy(() ->
                harness.activateAbility(player1, steamclawIndex, 0, null, bears.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);

        harness.addMana(player1, ManaColor.COLORLESS, 1);
        assertThatThrownBy(() ->
                harness.activateAbility(player1, steamclawIndex, 1, null, bears.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addReadySteamclaw(Player player) {
        Steamclaw card = new Steamclaw();
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}

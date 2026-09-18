package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.CoffinPurge;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Steamclaw.class, CoffinPurge.class})
class SteamclawTest extends BaseCardTest {

    @Test
    @DisplayName("{3}, {T}: exiles target card from a graveyard")
    void tapAbilityExilesTargetCard() {
        Permanent steamclaw = addReadySteamclaw(player1);
        Card target = new CoffinPurge();
        harness.setGraveyard(player2, List.of(target));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        int steamclawIndex = gd.playerBattlefields.get(player1.getId()).indexOf(steamclaw);
        harness.activateAbility(player1, steamclawIndex, 0, null, target.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(target);
        assertThat(steamclaw.isTapped()).isTrue();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(steamclaw);
    }

    @Test
    @DisplayName("{1}, Sacrifice this artifact: exiles target card from a graveyard")
    void sacrificeAbilityExilesTargetCardAndSacrificesSteamclaw() {
        Permanent steamclaw = addReadySteamclaw(player1);
        Card target = new CoffinPurge();
        harness.setGraveyard(player1, List.of(target));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        int steamclawIndex = gd.playerBattlefields.get(player1.getId()).indexOf(steamclaw);
        harness.activateAbility(player1, steamclawIndex, 1, null, target.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(steamclaw);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(steamclaw.getCard());
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(target);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(target);
    }

    @Test
    @DisplayName("Sacrifice ability does not require Steamclaw to be untapped")
    void sacrificeAbilityCanBeActivatedWhileTapped() {
        Permanent steamclaw = addReadySteamclaw(player1);
        steamclaw.tap();
        Card target = new CoffinPurge();
        harness.setGraveyard(player1, List.of(target));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        int steamclawIndex = gd.playerBattlefields.get(player1.getId()).indexOf(steamclaw);
        harness.activateAbility(player1, steamclawIndex, 1, null, target.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(steamclaw);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(target);
    }

    @Test
    @DisplayName("Tap ability does nothing when its target leaves the graveyard before resolution")
    void tapAbilityDoesNothingWhenTargetLeavesGraveyardBeforeResolution() {
        Permanent steamclaw = addReadySteamclaw(player1);
        Card target = new CoffinPurge();
        harness.setGraveyard(player2, List.of(target));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        int steamclawIndex = gd.playerBattlefields.get(player1.getId()).indexOf(steamclaw);
        harness.activateAbility(player1, steamclawIndex, 0, null, target.getId(), Zone.GRAVEYARD);
        harness.setGraveyard(player2, List.of());
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player2.getId())).doesNotContain(target);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(steamclaw);
        assertThat(steamclaw.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Both abilities reject a target that is not in a graveyard")
    void rejectsTargetNotInGraveyard() {
        Permanent steamclaw = addReadySteamclaw(player1);
        Card target = new CoffinPurge();
        int steamclawIndex = gd.playerBattlefields.get(player1.getId()).indexOf(steamclaw);

        harness.addMana(player1, ManaColor.COLORLESS, 3);
        assertThatThrownBy(() ->
                harness.activateAbility(player1, steamclawIndex, 0, null, target.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);
        assertThat(steamclaw.isTapped()).isFalse();

        harness.addMana(player1, ManaColor.COLORLESS, 1);
        assertThatThrownBy(() ->
                harness.activateAbility(player1, steamclawIndex, 1, null, target.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(steamclaw);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(steamclaw.getCard());
    }

    private Permanent addReadySteamclaw(Player player) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player, new Steamclaw());
        permanent.setSummoningSick(false);
        return permanent;
    }
}

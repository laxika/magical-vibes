package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GoblinHero;
import com.github.laxika.magicalvibes.cards.s.ScarwoodGoblins;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TormodsCrypt.class, GoblinHero.class, ScarwoodGoblins.class})
class TormodsCryptTest extends BaseCardTest {

    @Test
    @DisplayName("Activating the ability exiles target player's graveyard")
    void activateAbilityExilesGraveyard() {
        harness.addToBattlefield(player1, new TormodsCrypt());
        harness.setGraveyard(player2, List.of(new GoblinHero(), new ScarwoodGoblins()));

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player2.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Activating the ability sacrifices the crypt")
    void activateAbilitySacrificesCrypt() {
        harness.addToBattlefield(player1, new TormodsCrypt());
        harness.setGraveyard(player2, List.of(new GoblinHero()));

        harness.activateAbility(player1, 0, null, player2.getId());

        harness.assertNotOnBattlefield(player1, "Tormod's Crypt");
        harness.assertInGraveyard(player1, "Tormod's Crypt");
    }

    @Test
    @DisplayName("Can target its controller's own graveyard")
    void canTargetOwnGraveyard() {
        var crypt = harness.addToBattlefieldAndReturn(player1, new TormodsCrypt());
        var graveyardCard = new GoblinHero();
        harness.setGraveyard(player1, List.of(graveyardCard));

        harness.activateAbility(player1, 0, null, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .contains(graveyardCard, crypt.getCard());
    }

    @Test
    @DisplayName("Resolves harmlessly when the target's graveyard is empty")
    void worksOnEmptyGraveyard() {
        harness.addToBattlefield(player1, new TormodsCrypt());

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Cannot activate the ability while the crypt is tapped")
    void cannotActivateWhenTapped() {
        var crypt = harness.addToBattlefieldAndReturn(player1, new TormodsCrypt());
        crypt.tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
    }

    @Test
    @DisplayName("Exiles only the chosen player's graveyard")
    void exilesOnlyTargetPlayersGraveyard() {
        var crypt = harness.addToBattlefieldAndReturn(player1, new TormodsCrypt());
        var player1GraveyardCard = new GoblinHero();
        var player2GraveyardCard = new ScarwoodGoblins();
        harness.setGraveyard(player1, List.of(player1GraveyardCard));
        harness.setGraveyard(player2, List.of(player2GraveyardCard));

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .containsExactly(player1GraveyardCard, crypt.getCard());
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .containsExactly(player2GraveyardCard);
    }
}

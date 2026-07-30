package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TormodsCryptTest extends BaseCardTest {

    @Test
    @DisplayName("Activating the ability exiles target player's graveyard")
    void activateAbilityExilesGraveyard() {
        harness.addToBattlefield(player1, new TormodsCrypt());
        harness.setGraveyard(player2, List.of(new GrizzlyBears(), new Shock()));

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player2.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Activating the ability sacrifices the crypt")
    void activateAbilitySacrificesCrypt() {
        harness.addToBattlefield(player1, new TormodsCrypt());
        harness.setGraveyard(player2, List.of(new GrizzlyBears()));

        harness.activateAbility(player1, 0, null, player2.getId());

        harness.assertNotOnBattlefield(player1, "Tormod's Crypt");
        harness.assertInGraveyard(player1, "Tormod's Crypt");
    }

    @Test
    @DisplayName("Can target its controller's own graveyard")
    void canTargetOwnGraveyard() {
        harness.addToBattlefield(player1, new TormodsCrypt());
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));

        harness.activateAbility(player1, 0, null, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Tormod's Crypt"));
    }

    @Test
    @DisplayName("Resolves harmlessly when the target's graveyard is empty")
    void worksOnEmptyGraveyard() {
        harness.addToBattlefield(player1, new TormodsCrypt());

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }
}

package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RemorsefulClericTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing the Cleric exiles target player's graveyard")
    void exilesTargetPlayerGraveyard() {
        harness.addToBattlefield(player1, new RemorsefulCleric());
        harness.setGraveyard(player2, List.of(new GrizzlyBears(), new Shock()));

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player2.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Activating the ability sacrifices the Cleric")
    void activationSacrificesSelf() {
        harness.addToBattlefield(player1, new RemorsefulCleric());
        harness.setGraveyard(player2, List.of(new GrizzlyBears()));

        harness.activateAbility(player1, 0, null, player2.getId());

        harness.assertNotOnBattlefield(player1, "Remorseful Cleric");
        harness.assertInGraveyard(player1, "Remorseful Cleric");
    }

    @Test
    @DisplayName("Can target its controller's own graveyard, exiling itself too")
    void canTargetOwnGraveyard() {
        harness.addToBattlefield(player1, new RemorsefulCleric());
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));

        harness.activateAbility(player1, 0, null, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Remorseful Cleric"));
    }

    @Test
    @DisplayName("Resolves harmlessly against an empty graveyard")
    void worksOnEmptyGraveyard() {
        harness.addToBattlefield(player1, new RemorsefulCleric());

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
    }
}

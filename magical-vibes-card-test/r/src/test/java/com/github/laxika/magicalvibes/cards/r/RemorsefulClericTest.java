package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GreenwoodSentinel;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RemorsefulCleric.class, GreenwoodSentinel.class, Shock.class})
class RemorsefulClericTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing the Cleric exiles target player's graveyard")
    void exilesTargetPlayerGraveyard() {
        harness.addToBattlefield(player1, new RemorsefulCleric());
        harness.setGraveyard(player2, List.of(new GreenwoodSentinel(), new Shock()));

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player2.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Activating the ability sacrifices the Cleric")
    void activationSacrificesSelf() {
        harness.addToBattlefield(player1, new RemorsefulCleric());
        harness.setGraveyard(player2, List.of(new GreenwoodSentinel()));

        harness.activateAbility(player1, 0, null, player2.getId());

        harness.assertNotOnBattlefield(player1, "Remorseful Cleric");
        harness.assertInGraveyard(player1, "Remorseful Cleric");
    }

    @Test
    @DisplayName("Can target its controller's own graveyard, exiling itself too")
    void canTargetOwnGraveyard() {
        harness.addToBattlefield(player1, new RemorsefulCleric());
        harness.setGraveyard(player1, List.of(new GreenwoodSentinel()));

        harness.activateAbility(player1, 0, null, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Greenwood Sentinel"));
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

    @Test
    @DisplayName("Exiles cards entering the targeted graveyard in response, leaving the other graveyard alone")
    void exilesGraveyardContentsAtResolution() {
        harness.addToBattlefield(player1, new RemorsefulCleric());
        harness.setGraveyard(player1, List.of(new GreenwoodSentinel()));
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, player2.getId());

        harness.assertInGraveyard(player1, "Remorseful Cleric");
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        harness.castInstant(player2, 0, player1.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Shock");
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();

        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .extracting(c -> c.getName()).containsExactly("Shock");
        harness.assertInGraveyard(player1, "Greenwood Sentinel");
        harness.assertInGraveyard(player1, "Remorseful Cleric");
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("A player target is required before the Cleric can be sacrificed")
    void rejectsActivationWithoutTarget() {
        harness.addToBattlefield(player1, new RemorsefulCleric());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Remorseful Cleric");
        harness.assertNotInGraveyard(player1, "Remorseful Cleric");
        assertThat(gd.stack).isEmpty();
    }
}

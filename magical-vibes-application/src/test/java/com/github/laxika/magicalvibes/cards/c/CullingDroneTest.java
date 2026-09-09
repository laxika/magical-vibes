package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CullingDrone.class, GrizzlyBears.class})
class CullingDroneTest extends BaseCardTest {

    @Test
    @DisplayName("Combat damage exiles the top card of the damaged player's library")
    void combatDamageExilesTopCard() {
        Permanent drone = addAttackingDrone(player1);
        GrizzlyBears topCard = new GrizzlyBears();
        harness.setLibrary(player2, List.of(topCard));

        resolveCombatAndTrigger();

        ExiledCardEntry entry = gd.findExiledCard(topCard.getId());
        assertThat(entry).isNotNull();
        assertThat(entry.sourcePermanentId()).isNull();
        assertThat(gd.getCardsExiledByPermanent(drone.getId())).isEmpty();
    }

    @Test
    @DisplayName("No card is exiled when the damaged player's library is empty")
    void noExileWhenLibraryEmpty() {
        addAttackingDrone(player1);
        harness.setLibrary(player2, List.of());

        resolveCombatAndTrigger();

        assertThat(gd.exiledCards).isEmpty();
    }

    private Permanent addAttackingDrone(Player player) {
        Permanent drone = addCreatureReady(player, new CullingDrone());
        drone.setAttacking(true);
        return drone;
    }

    private void resolveCombatAndTrigger() {
        resolveCombat();
        harness.passBothPriorities();
    }
}

package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.k.KozileksChanneler;
import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CullingDrone.class, KozileksChanneler.class})
class CullingDroneTest extends BaseCardTest {

    @Test
    @DisplayName("Combat damage exiles the top card of the damaged player's library")
    void combatDamageExilesTopCard() {
        Permanent drone = addAttackingDrone(player1);
        KozileksChanneler topCard = new KozileksChanneler();
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

    @Test
    @DisplayName("Ingest exiles exactly one card, from the top of only the damaged player's library")
    void exilesOnlyTopCardOfDamagedPlayerLibrary() {
        addAttackingDrone(player1);
        CullingDrone topCard = new CullingDrone();
        CullingDrone nextCard = new CullingDrone();
        CullingDrone ownCard = new CullingDrone();
        harness.setLibrary(player2, List.of(topCard, nextCard));
        harness.setLibrary(player1, List.of(ownCard));

        resolveCombatAndTrigger();

        assertThat(gd.findExiledCard(topCard.getId())).isNotNull();
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(nextCard);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(ownCard);
        assertThat(gd.exiledCards).hasSize(1);
        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("Ingest uses the damaged player when the second player attacks")
    void secondPlayerAttackingExilesFirstPlayersCard() {
        addAttackingDrone(player2);
        CullingDrone topCard = new CullingDrone();
        harness.setLibrary(player1, List.of(topCard));

        resolveCombat(player2);
        resolveAllTriggers();

        assertThat(gd.findExiledCard(topCard.getId())).isNotNull();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        harness.assertLife(player1, 18);
    }

    @Test
    @DisplayName("A blocked Drone does not ingest when it damages only a creature")
    void blockedDroneDoesNotIngest() {
        addCreatureReady(player1, new CullingDrone());
        addCreatureReady(player2, new KozileksChanneler());
        CullingDrone topCard = new CullingDrone();
        harness.setLibrary(player2, List.of(topCard));

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombatAndTrigger();

        assertThat(gd.exiledCards).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(topCard);
        harness.assertLife(player2, 20);
        harness.assertInGraveyard(player1, "Culling Drone");
    }

    @Test
    @DisplayName("Ingest resolves after its source leaves the battlefield")
    void ingestResolvesWithoutSource() {
        gd.playerAutoStopSteps.put(player1.getId(), java.util.Set.of(TurnStep.COMBAT_DAMAGE));
        gd.playerAutoStopSteps.put(player2.getId(), java.util.Set.of(TurnStep.COMBAT_DAMAGE));
        Permanent drone = addAttackingDrone(player1);
        CullingDrone topCard = new CullingDrone();
        harness.setLibrary(player2, List.of(topCard));

        resolveCombat();
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(topCard);
        gd.playerBattlefields.get(player1.getId()).remove(drone);
        harness.setGraveyard(player1, List.of(drone.getCard()));
        resolveAllTriggers();

        assertThat(gd.findExiledCard(topCard.getId())).isNotNull();
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
    }

    private Permanent addAttackingDrone(Player player) {
        Permanent drone = addCreatureReady(player, new CullingDrone());
        drone.setAttacking(true);
        return drone;
    }

    private void resolveCombatAndTrigger() {
        resolveCombat();
        resolveAllTriggers();
    }
}

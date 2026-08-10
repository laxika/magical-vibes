package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.f.ForceOfNature;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PitSpawnTest extends BaseCardTest {

    @Test
    @DisplayName("Whenever Pit Spawn deals damage to a creature, it exiles that creature")
    void exilesCreatureItDamages() {
        harness.addToBattlefield(player1, new PitSpawn());
        harness.addToBattlefield(player2, new ForceOfNature());
        harness.setHand(player1, List.of(new PreyUpon()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castSorcery(player1, 0,
                List.of(harness.getPermanentId(player1, "Pit Spawn"),
                        harness.getPermanentId(player2, "Force of Nature")));
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(exiled -> exiled.getName().equals("Force of Nature"));
    }

    @Test
    @DisplayName("Paying Pit Spawn's upkeep cost keeps it on the battlefield")
    void payingUpkeepCostKeepsPitSpawn() {
        harness.addToBattlefield(player1, new PitSpawn());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.handleMayAbilityChosen(player1, true);

        harness.assertOnBattlefield(player1, "Pit Spawn");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Declining Pit Spawn's upkeep cost sacrifices it")
    void decliningUpkeepCostSacrificesPitSpawn() {
        harness.addToBattlefield(player1, new PitSpawn());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertInGraveyard(player1, "Pit Spawn");
    }
}

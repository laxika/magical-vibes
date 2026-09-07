package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.s.ShivanDragon;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ElderSpawn.class, Island.class, GrizzlyBears.class, ShivanDragon.class})
class ElderSpawnTest extends BaseCardTest {

    @Test
    @DisplayName("Without an Island, Elder Spawn is sacrificed and deals 6 damage")
    void noIslandSacrificesElderSpawnAndDealsDamage() {
        harness.addToBattlefield(player1, new ElderSpawn());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Elder Spawn");
        harness.assertInGraveyard(player1, "Elder Spawn");
        harness.assertLife(player1, 14);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Sacrificing an Island keeps Elder Spawn")
    void sacrificingIslandKeepsElderSpawn() {
        harness.addToBattlefield(player1, new ElderSpawn());
        harness.addToBattlefield(player1, new Island());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        harness.assertOnBattlefield(player1, "Elder Spawn");
        harness.assertNotOnBattlefield(player1, "Island");
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Declining to sacrifice an Island sacrifices Elder Spawn and deals 6 damage")
    void decliningIslandSacrificeSacrificesElderSpawn() {
        harness.addToBattlefield(player1, new ElderSpawn());
        harness.addToBattlefield(player1, new Island());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertNotOnBattlefield(player1, "Elder Spawn");
        harness.assertOnBattlefield(player1, "Island");
        harness.assertLife(player1, 14);
    }

    @Test
    @DisplayName("Elder Spawn can't be blocked by a red creature")
    void cannotBeBlockedByRedCreature() {
        Permanent elderSpawn = addCreatureReady(player1, new ElderSpawn());
        elderSpawn.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new ShivanDragon());

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(
                        gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                        gd.playerBattlefields.get(player1.getId()).indexOf(elderSpawn)))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Elder Spawn can be blocked by a nonred creature")
    void canBeBlockedByNonredCreature() {
        Permanent elderSpawn = addCreatureReady(player1, new ElderSpawn());
        elderSpawn.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                gd.playerBattlefields.get(player1.getId()).indexOf(elderSpawn))));

        assertThat(blocker.isBlocking()).isTrue();
    }

}

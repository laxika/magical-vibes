package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.event.GameEventFact;

import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GoblinArtillery.class, LlanowarElves.class})
class GoblinArtilleryTest extends BaseCardTest {

    @Test
    @DisplayName("Activating ability targeting player puts it on the stack")
    void activatingTargetingPlayerPutsOnStack() {
        addReadyArtillery(player1);

        harness.activateAbility(player1, 0, null, player2.getId());

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        assertThat(entry.getTargetId()).isEqualTo(player2.getId());
    }

    @Test
    @DisplayName("Activating ability taps Goblin Artillery")
    void activatingTapsArtillery() {
        Permanent artillery = addReadyArtillery(player1);

        harness.activateAbility(player1, 0, null, player2.getId());

        assertThat(artillery.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Deals 2 damage to target player and 3 damage to controller")
    void deals2DamageToPlayerAnd3ToController() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        addReadyArtillery(player1);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("Can target self — takes both 2 target damage and 3 controller damage")
    void canTargetSelf() {
        harness.setLife(player1, 20);
        addReadyArtillery(player1);

        harness.activateAbility(player1, 0, null, player1.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        // 20 - 2 (target damage) - 3 (controller damage) = 15
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(15);
    }

    @Test
    @DisplayName("Deals 2 damage to target creature, destroying a 1/1, and 3 damage to controller")
    void deals2DamageDestroying1ToughnessAnd3ToController() {
        harness.setLife(player1, 20);
        harness.addToBattlefield(player2, new LlanowarElves());

        addReadyArtillery(player1);

        UUID targetId = harness.getPermanentId(player2, "Llanowar Elves");
        harness.activateAbility(player1, 0, null, targetId);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        harness.assertNotOnBattlefield(player2, "Llanowar Elves");
        harness.assertInGraveyard(player2, "Llanowar Elves");
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("Deals 2 damage to target 1/3 creature, creature survives, controller takes 3 damage")
    void deals2DamageDoesNotKill3Toughness() {
        harness.setLife(player1, 20);
        // Use another Goblin Artillery as a 1/3 target
        Permanent targetArtillery = harness.addToBattlefieldAndReturn(player2, new GoblinArtillery());
        targetArtillery.setSummoningSick(false);

        addReadyArtillery(player1);

        harness.activateAbility(player1, 0, null, targetArtillery.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        harness.assertOnBattlefield(player2, "Goblin Artillery");
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("Cannot activate ability when already tapped")
    void cannotActivateWhenTapped() {
        Permanent artillery = addReadyArtillery(player1);
        artillery.tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
    }

    @Test
    @DisplayName("Cannot activate ability with summoning sickness")
    void cannotActivateWithSummoningSickness() {
        harness.addToBattlefield(player1, new GoblinArtillery());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sick");
    }

    @Test
    @DisplayName("Ability fizzles if target creature is removed — controller takes no damage")
    void fizzlesIfTargetCreatureRemoved() {
        harness.setLife(player1, 20);
        addReadyArtillery(player1);
        harness.addToBattlefield(player2, new LlanowarElves());

        UUID targetId = harness.getPermanentId(player2, "Llanowar Elves");
        harness.activateAbility(player1, 0, null, targetId);

        // Remove target before resolution
        harness.getGameData().playerBattlefields.get(player2.getId()).clear();

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("fizzles"));
        // Controller does NOT take damage when ability fizzles
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Ability still deals both amounts of damage after its source leaves the battlefield")
    void resolvesAfterSourceLeavesBattlefield() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        Permanent artillery = addReadyArtillery(player1);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.getGameData().playerBattlefields.get(player1.getId()).remove(artillery);
        harness.getGameData().playerGraveyards.get(player1.getId()).add(artillery.getCard());
        harness.passBothPriorities();

        harness.assertLife(player1, 17);
        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("Lethal damage to both players results in a draw after the whole ability resolves")
    void lethalDamageToBothPlayersDrawsGame() {
        harness.setLife(player1, 3);
        harness.setLife(player2, 2);
        addReadyArtillery(player1);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 0);
        harness.assertLife(player2, 0);
        assertThat(harness.getGameData().gameResult).isEqualTo(GameEventFact.GameResult.DRAW);
        assertThat(harness.getGameData().winnerPlayerId).isNull();
    }

    @Test
    @DisplayName("Can target itself as a creature and survives its own 2 damage")
    void canTargetItsOwnCreature() {
        harness.setLife(player1, 20);
        Permanent artillery = addReadyArtillery(player1);

        harness.activateAbility(player1, 0, null, artillery.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Goblin Artillery");
        assertThat(artillery.getMarkedDamage()).isEqualTo(2);
        harness.assertLife(player1, 17);
    }

    private Permanent addReadyArtillery(Player player) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new GoblinArtillery());
        perm.setSummoningSick(false);
        return perm;
    }
}

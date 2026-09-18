package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.d.DwarvenDriller;
import com.github.laxika.magicalvibes.cards.s.SuntailHawk;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({JeskaWarriorAdept.class, DwarvenDriller.class, SuntailHawk.class})
class JeskaWarriorAdeptTest extends BaseCardTest {

    @Test
    @DisplayName("Tap ability deals 1 damage to target player")
    void deals1DamageToPlayer() {
        harness.setLife(player2, 20);
        Permanent jeska = addReadyJeska(player1);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(jeska.isTapped()).isTrue();
        assertThat(harness.getGameData().playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Tap ability deals 1 damage to target creature")
    void deals1DamageToCreature() {
        addReadyJeska(player1);
        harness.addToBattlefield(player2, new SuntailHawk());

        harness.activateAbility(player1, 0, null, harness.getPermanentId(player2, "Suntail Hawk"));
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Suntail Hawk");
    }

    @Test
    @DisplayName("One damage does not destroy a 2/2 creature")
    void oneDamageDoesNotDestroyTwoToughnessCreature() {
        addReadyJeska(player1);
        harness.addToBattlefield(player2, new DwarvenDriller());

        harness.activateAbility(player1, 0, null, harness.getPermanentId(player2, "Dwarven Driller"));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Dwarven Driller");
    }

    @Test
    @DisplayName("First strike lets Jeska survive combat with a 2/2 blocker")
    void firstStrikeLetsJeskaSurviveCombat() {
        Permanent jeska = addReadyJeska(player1);
        harness.addToBattlefield(player2, new DwarvenDriller());

        declareAttackers(List.of(0));
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(jeska);
        harness.assertInGraveyard(player2, "Dwarven Driller");
    }

    @Test
    @DisplayName("Haste allows activating the tap ability immediately")
    void hasteAllowsImmediateActivation() {
        harness.addToBattlefield(player1, new JeskaWarriorAdept());

        harness.activateAbility(player1, 0, null, player2.getId());

        assertThat(harness.getGameData().stack).hasSize(1);
    }

    @Test
    @DisplayName("Cannot activate the tap ability when Jeska is already tapped")
    void cannotActivateWhenTapped() {
        Permanent jeska = addReadyJeska(player1);
        jeska.tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
    }

    private Permanent addReadyJeska(Player player) {
        return addCreatureReady(player, new JeskaWarriorAdept());
    }
}

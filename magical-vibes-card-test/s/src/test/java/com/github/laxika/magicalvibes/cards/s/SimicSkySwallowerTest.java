package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.b.BeaconHawk;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SimicSkySwallower.class, GrizzlyBears.class, Shock.class, BeaconHawk.class, SealOfFire.class})
class SimicSkySwallowerTest extends BaseCardTest {

    @Test
    @DisplayName("Flying prevents a non-flying creature from blocking")
    void flyingPreventsNonFlyingCreatureFromBlocking() {
        Permanent swallower = addCreatureReady(player1, new SimicSkySwallower());
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());

        declareAttackersAndPrepareBlockers(player1, List.of(gd.playerBattlefields.get(player1.getId()).indexOf(swallower)));

        int blockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(swallower);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(blockerIndex, attackerIndex))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("flying");
    }

    @Test
    @DisplayName("Shroud prevents the creature from being targeted by a spell")
    void shroudPreventsTargeting() {
        Permanent swallower = harness.addToBattlefieldAndReturn(player1, new SimicSkySwallower());

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, swallower.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("shroud");
    }

    @Test
    @DisplayName("Trample assigns excess combat damage to the defending player")
    void trampleAssignsExcessCombatDamageToDefendingPlayer() {
        harness.setLife(player2, 20);
        Permanent swallower = addCreatureReady(player1, new SimicSkySwallower());
        Permanent blocker = addCreatureReady(player2, new BeaconHawk());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        harness.handleCombatDamageAssigned(player1, 0, Map.of(
                blocker.getId(), 1,
                player2.getId(), 5));

        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(15);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(swallower);
    }

    @Test
    void shroudPreventsOpponentSpellTargeting() {
        Permanent swallower = harness.addToBattlefieldAndReturn(player1, new SimicSkySwallower());
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, swallower.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("shroud");
    }

    @Test
    void shroudPreventsActivatedAbilityTargeting() {
        Permanent swallower = harness.addToBattlefieldAndReturn(player1, new SimicSkySwallower());
        Permanent seal = harness.addToBattlefieldAndReturn(player2, new SealOfFire());

        assertThatThrownBy(() -> harness.activateAbility(player2, 0, null, swallower.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("shroud");
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(seal);
    }
}

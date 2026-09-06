package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GoblinRoughrider;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AirdropCondor.class, GoblinRoughrider.class, GrizzlyBears.class, Island.class})
class AirdropCondorTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrifices a Goblin and deals damage equal to its power to a player")
    void sacrificesGoblinAndDealsItsPowerToPlayer() {
        Permanent condor = addCreatureReady(player1, new AirdropCondor());
        Permanent goblin = addCreatureReady(player1, new GoblinRoughrider());
        harness.setLife(player2, 20);
        harness.addMana(player1, ManaColor.RED, 2);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(condor).doesNotContain(goblin);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(goblin.getCard());
    }

    @Test
    @DisplayName("Uses the Goblin's effective power when it is sacrificed")
    void usesEffectiveSacrificedPower() {
        addCreatureReady(player1, new AirdropCondor());
        Permanent goblin = addCreatureReady(player1, new GoblinRoughrider());
        goblin.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.setLife(player2, 20);
        harness.addMana(player1, ManaColor.RED, 2);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
    }

    @Test
    @DisplayName("Can deal the sacrificed Goblin's power to a creature")
    void dealsDamageToCreature() {
        addCreatureReady(player1, new AirdropCondor());
        addCreatureReady(player1, new GoblinRoughrider());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.RED, 2);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(target.getCard());
    }

    @Test
    @DisplayName("Cannot target a land")
    void cannotTargetLand() {
        addCreatureReady(player1, new AirdropCondor());
        addCreatureReady(player1, new GoblinRoughrider());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Island());
        harness.addMana(player1, ManaColor.RED, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, land.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}

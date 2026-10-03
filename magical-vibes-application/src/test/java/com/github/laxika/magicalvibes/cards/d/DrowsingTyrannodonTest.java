package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.o.OnakkeOgre;
import com.github.laxika.magicalvibes.cards.a.AlpineWatchdog;
import com.github.laxika.magicalvibes.cards.c.ConsulateDreadnought;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DrowsingTyrannodon.class, OnakkeOgre.class, AlpineWatchdog.class})
class DrowsingTyrannodonTest extends BaseCardTest {

    @Test
    @DisplayName("Cannot attack without a creature with power 4 or greater")
    void cannotAttackWithoutPowerFourCreature() {
        Permanent tyrannodon = addCreatureReady(player1, new DrowsingTyrannodon());
        harness.addToBattlefield(player2, new AlpineWatchdog());

        assertThatThrownBy(() -> declareAttackers(List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
        assertThat(tyrannodon.isAttacking()).isFalse();
    }

    @Test
    @DisplayName("Can attack while its controller controls a creature with power 4 or greater")
    void canAttackWithPowerFourCreature() {
        Permanent tyrannodon = addCreatureReady(player1, new DrowsingTyrannodon());
        addCreatureReady(player1, new OnakkeOgre());
        harness.addToBattlefield(player2, new AlpineWatchdog());

        declareAttackers(List.of(0));

        assertThat(tyrannodon.isAttacking()).isTrue();
    }

    @Test
    @DisplayName("An opponent's creature with power 4 or greater does not help")
    void opponentPowerFourCreatureDoesNotHelp() {
        Permanent tyrannodon = addCreatureReady(player1, new DrowsingTyrannodon());
        addCreatureReady(player2, new OnakkeOgre());

        assertThatThrownBy(() -> declareAttackers(List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
        assertThat(tyrannodon.isAttacking()).isFalse();
    }

    @Test
    @DisplayName("Cannot be declared attacking when the qualifying creature has left the battlefield")
    void cannotAttackWhenPowerFourCreatureLeavesBeforeDeclaration() {
        Permanent tyrannodon = addCreatureReady(player1, new DrowsingTyrannodon());
        Permanent wurm = addCreatureReady(player1, new OnakkeOgre());
        harness.addToBattlefield(player2, new AlpineWatchdog());
        gd.playerBattlefields.get(player1.getId()).remove(wurm);

        assertThatThrownBy(() -> declareAttackers(List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
        assertThat(tyrannodon.isAttacking()).isFalse();
    }

    @Test
    void canQualifyItselfWithPowerExactlyFour() {
        Permanent tyrannodon = addCreatureReady(player1, new DrowsingTyrannodon());
        tyrannodon.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.addToBattlefield(player2, new AlpineWatchdog());

        declareAttackers(List.of(0));

        assertThat(tyrannodon.isAttacking()).isTrue();
    }

    @Test
    void cannotAttackWhenQualifyingCreaturePowerDropsBelowFour() {
        Permanent tyrannodon = addCreatureReady(player1, new DrowsingTyrannodon());
        Permanent support = addCreatureReady(player1, new DrowsingTyrannodon());
        support.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        assertThat(als.canAttack(gd, tyrannodon, player1.getId())).isTrue();
        support.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 0);

        assertThatThrownBy(() -> declareAttackers(List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
        assertThat(tyrannodon.isAttacking()).isFalse();
    }

    @Test
    void remainsAttackingWhenQualifyingCreatureLeavesAfterDeclaration() {
        Permanent tyrannodon = addCreatureReady(player1, new DrowsingTyrannodon());
        Permanent support = addCreatureReady(player1, new OnakkeOgre());
        harness.addToBattlefield(player2, new AlpineWatchdog());
        declareAttackersAndPrepareBlockers(List.of(0));

        gd.playerBattlefields.get(player1.getId()).remove(support);

        assertThat(tyrannodon.isAttacking()).isTrue();
        gs.declareBlockers(gd, player2, java.util.Map.of());
        resolveCombat();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
    }

    @Test
    @CardUsed({ConsulateDreadnought.class})
    void uncrewedVehicleDoesNotAllowAttacking() {
        Permanent tyrannodon = addCreatureReady(player1, new DrowsingTyrannodon());
        harness.addToBattlefield(player1, new ConsulateDreadnought());
        harness.addToBattlefield(player2, new AlpineWatchdog());

        assertThatThrownBy(() -> declareAttackers(List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
        assertThat(tyrannodon.isAttacking()).isFalse();
    }
}

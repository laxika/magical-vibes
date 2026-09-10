package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.y.YoungWolf;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BejeweledWarg.class, YoungWolf.class})
class BejeweledWargTest extends BaseCardTest {

    @Test
    void combatDamagePutsCounterOnTargetWolfYouControl() {
        Permanent warg = addCreatureReady(player1, new BejeweledWarg());
        warg.setAttacking(true);
        Permanent wolf = addCreatureReady(player1, new YoungWolf());

        resolveCombatAndTrigger();
        harness.handleListChoice(player1, "Put a +1/+1 counter on target Wolf you control");
        harness.handlePermanentChosen(player1, wolf.getId());

        assertThat(wolf.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(findPermanents(player1, "Treasure")).isEmpty();
    }

    @Test
    void combatDamageCreatesTreasure() {
        Permanent warg = addCreatureReady(player1, new BejeweledWarg());
        warg.setAttacking(true);

        resolveCombatAndTrigger();
        harness.handleListChoice(player1, "Create a Treasure token");

        assertThat(findPermanents(player1, "Treasure")).hasSize(1);
    }

    @Test
    void counterModeCannotTargetOpponentWolf() {
        Permanent warg = addCreatureReady(player1, new BejeweledWarg());
        warg.setAttacking(true);
        Permanent wolf = addCreatureReady(player2, new YoungWolf());
        Permanent ownWolf = addCreatureReady(player1, new YoungWolf());

        resolveCombatAndTrigger();
        harness.handleListChoice(player1, "Put a +1/+1 counter on target Wolf you control");
        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, wolf.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.handlePermanentChosen(player1, ownWolf.getId());

        assertThat(wolf.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(ownWolf.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    private void resolveCombatAndTrigger() {
        resolveCombat();
        harness.passBothPriorities();
    }
}

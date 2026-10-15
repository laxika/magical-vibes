package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.b.BonebreakerGiant;
import com.github.laxika.magicalvibes.cards.c.ChandraTheFirebrand;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GoblinFireslinger.class, BonebreakerGiant.class, ChandraTheFirebrand.class})
class GoblinFireslingerTest extends BaseCardTest {

    @Test
    @DisplayName("Tap ability deals 1 damage to target player")
    void dealsDamageToPlayer() {
        harness.setLife(player2, 20);
        Permanent fireslinger = addReadyFireslinger(player1);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
        assertThat(fireslinger.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Tap ability can't target a creature")
    void cannotTargetCreature() {
        addReadyFireslinger(player1);
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new BonebreakerGiant());
        UUID creatureId = creature.getId();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creatureId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void dealsDamageToPlaneswalker() {
        addReadyFireslinger(player1);
        Permanent chandra = harness.addToBattlefieldAndReturn(player2, new ChandraTheFirebrand());
        chandra.setCounterCount(CounterType.LOYALTY, 3);
        harness.setLife(player2, 20);

        harness.activateAbility(player1, 0, null, chandra.getId());
        harness.passBothPriorities();

        assertThat(chandra.getCounterCount(CounterType.LOYALTY)).isEqualTo(2);
        harness.assertLife(player2, 20);
    }

    @Test
    void canTargetItsController() {
        addReadyFireslinger(player1);
        harness.setLife(player1, 20);

        harness.activateAbility(player1, 0, null, player1.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 19);
    }

    @Test
    void cannotActivateWhileSummoningSick() {
        Permanent fireslinger = harness.addToBattlefieldAndReturn(player1, new GoblinFireslinger());
        fireslinger.setSummoningSick(true);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(fireslinger.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotActivateAgainWhileTapped() {
        addReadyFireslinger(player1);
        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void abilityResolvesAfterSourceLeavesBattlefield() {
        Permanent fireslinger = addReadyFireslinger(player1);
        harness.setLife(player2, 20);
        harness.activateAbility(player1, 0, null, player2.getId());
        gd.playerBattlefields.get(player1.getId()).remove(fireslinger);
        gd.playerGraveyards.get(player1.getId()).add(fireslinger.getCard());

        harness.passBothPriorities();

        harness.assertLife(player2, 19);
        assertThat(gd.stack).isEmpty();
    }

    private Permanent addReadyFireslinger(Player player) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new GoblinFireslinger());
        perm.setSummoningSick(false);
        return perm;
    }
}

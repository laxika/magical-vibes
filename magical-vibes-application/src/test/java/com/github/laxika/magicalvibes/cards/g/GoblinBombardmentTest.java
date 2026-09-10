package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.a.ArchangelElspeth;
import com.github.laxika.magicalvibes.cards.a.AwakenedSkyclave;
import com.github.laxika.magicalvibes.cards.i.InvasionOfZendikar;
import com.github.laxika.magicalvibes.cards.m.MoggFanatic;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AwakenedSkyclave.class, GoblinBombardment.class, InvasionOfZendikar.class,
        ArchangelElspeth.class, MoggFanatic.class})
class GoblinBombardmentTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrifices a creature and deals 1 damage to a player with no mana cost")
    void dealsOneDamageToPlayer() {
        harness.addToBattlefield(player1, new GoblinBombardment());
        harness.addToBattlefield(player1, new MoggFanatic());
        harness.setLife(player2, 20);
        harness.forceActivePlayer(player1);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertLife(player2, 19);
        harness.assertInGraveyard(player1, "Mogg Fanatic");
    }

    @Test
    @DisplayName("Deals 1 damage to a creature, killing a 1-toughness one")
    void dealsOneDamageToCreature() {
        harness.addToBattlefield(player1, new GoblinBombardment());
        harness.addToBattlefield(player1, new MoggFanatic());
        harness.addToBattlefield(player2, new MoggFanatic());
        var fanatic = harness.getPermanentId(player2, "Mogg Fanatic");
        harness.forceActivePlayer(player1);

        harness.activateAbility(player1, 0, null, fanatic);
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Mogg Fanatic");
    }

    @Test
    @DisplayName("Deals 1 damage to a planeswalker")
    void dealsOneDamageToPlaneswalker() {
        harness.addToBattlefield(player1, new GoblinBombardment());
        harness.addToBattlefield(player1, new MoggFanatic());
        Permanent elspeth = harness.addToBattlefieldAndReturn(player2, new ArchangelElspeth());
        elspeth.setCounterCount(CounterType.LOYALTY, 4);
        harness.forceActivePlayer(player1);

        harness.activateAbility(player1, 0, null, elspeth.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(elspeth.getCounterCount(CounterType.LOYALTY)).isEqualTo(3);
    }

    @Test
    @DisplayName("Deals 1 damage to a battle")
    void dealsOneDamageToBattle() {
        harness.addToBattlefield(player1, new GoblinBombardment());
        harness.addToBattlefield(player1, new MoggFanatic());
        Permanent battle = harness.addToBattlefieldAndReturn(player2, new InvasionOfZendikar());
        battle.setCounterCount(CounterType.DEFENSE, 3);
        harness.forceActivePlayer(player1);

        harness.activateAbility(player1, 0, null, battle.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(battle.getCounterCount(CounterType.DEFENSE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Cannot activate without a creature to sacrifice")
    void requiresCreatureToSacrifice() {
        harness.addToBattlefield(player1, new GoblinBombardment());
        harness.forceActivePlayer(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}

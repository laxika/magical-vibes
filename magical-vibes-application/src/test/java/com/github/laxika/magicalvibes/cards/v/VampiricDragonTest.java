package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.a.Afflict;
import com.github.laxika.magicalvibes.cards.c.CephalidScout;
import com.github.laxika.magicalvibes.cards.c.CentaurGarden;
import com.github.laxika.magicalvibes.cards.f.FledglingImp;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({VampiricDragon.class, CephalidScout.class, FledglingImp.class,
        CentaurGarden.class, Afflict.class})
class VampiricDragonTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 1 damage to a creature and gets a +1/+1 counter when it dies")
    void dealsDamageAndGainsCounterWhenTargetDies() {
        Permanent dragon = addCreatureReady(player1, new VampiricDragon());
        Permanent target = addCreatureReady(player2, new CephalidScout());

        activateDamageAbility(target);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);
        assertThat(dragon.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Does not get a counter when the damaged creature survives")
    void noCounterWhenTargetSurvives() {
        Permanent dragon = addCreatureReady(player1, new VampiricDragon());
        Permanent target = addCreatureReady(player2, new FledglingImp());

        activateDamageAbility(target);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
        assertThat(target.getMarkedDamage()).isEqualTo(1);
        assertThat(dragon.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Gets a counter when the damaged creature dies later that turn")
    void gainsCounterWhenDamagedCreatureDiesLaterThatTurn() {
        Permanent dragon = addCreatureReady(player1, new VampiricDragon());
        Permanent target = addCreatureReady(player2, new FledglingImp());

        activateDamageAbility(target);
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(1);
        assertThat(dragon.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();

        harness.setHand(player1, List.of(new Afflict()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);
        assertThat(dragon.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Can activate its damage ability while summoning sick")
    void canActivateDamageAbilityWithoutHaste() {
        harness.addToBattlefieldAndReturn(player1, new VampiricDragon());
        Permanent target = addCreatureReady(player2, new FledglingImp());

        activateDamageAbility(target);
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot target a non-creature permanent")
    void cannotTargetLand() {
        addCreatureReady(player1, new VampiricDragon());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new CentaurGarden());

        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, land.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    private void activateDamageAbility(Permanent target) {
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, null, target.getId());
    }
}

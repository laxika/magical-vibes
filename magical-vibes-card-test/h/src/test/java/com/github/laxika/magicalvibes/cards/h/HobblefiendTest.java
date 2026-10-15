package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Hobblefiend.class, GrizzlyBears.class})
class HobblefiendTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing another creature puts a +1/+1 counter on Hobblefiend")
    void sacrificingAnotherCreaturePutsCounterOnHobblefiend() {
        Permanent hobblefiend = addHobblefiendReady(player1);
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(hobblefiend.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(bears.getCard());
    }

    @Test
    @DisplayName("Hobblefiend cannot sacrifice itself")
    void cannotSacrificeItself() {
        addHobblefiendReady(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Hobblefiend");
    }

    private Permanent addHobblefiendReady(Player player) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player, new Hobblefiend());
        permanent.setSummoningSick(false);
        return permanent;
    }

    @Test
    @DisplayName("A tapped, summoning-sick Hobblefiend can activate and pays sacrifice before resolution")
    void activatesWhileTappedAndSummoningSick() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new Hobblefiend());
        source.setSummoningSick(true);
        source.tap();
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new Hobblefiend());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(sacrifice.getCard());
        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(source);
        assertThat(source.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();

        harness.passBothPriorities();

        assertThat(source.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(source.isTapped()).isTrue();
    }

    @Test
    @DisplayName("An opponent's creature cannot pay the sacrifice cost")
    void cannotSacrificeOpponentsCreature() {
        addHobblefiendReady(player1);
        Permanent opponent = harness.addToBattlefieldAndReturn(player2, new Hobblefiend());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(opponent);
        harness.assertOnBattlefield(player1, "Hobblefiend");
    }

    @Test
    @DisplayName("Activation requires one mana even with another creature available")
    void cannotActivateWithoutMana() {
        Permanent source = addHobblefiendReady(player1);
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new Hobblefiend());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(source, sacrifice);
        assertThat(source.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }
}

package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.a.AcademyDrake;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ThroneOfMakindi.class, AcademyDrake.class})
class ThroneOfMakindiTest extends BaseCardTest {

    @Test
    void addsColorlessMana() {
        readyThrone();

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
    }

    @Test
    void putsChargeCounterOnItselfForOneMana() {
        Permanent throne = readyThrone();
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(throne.getCounterCount(CounterType.CHARGE)).isEqualTo(1);
    }

    @Test
    void addsTwoManaOfOneChosenColorWithChargeCounter() {
        Permanent throne = readyThrone();
        throne.setCounterCount(CounterType.CHARGE, 1);

        harness.activateAbility(player1, 0, 2, null, null);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        harness.handleListChoice(player1, ManaColor.RED.name());

        assertThat(gd.playerManaPools.get(player1.getId()).getKickedOnlyMana(ManaColor.RED)).isEqualTo(2);
        assertThat(throne.getCounterCount(CounterType.CHARGE)).isZero();
    }

    @Test
    void kickedOnlyManaCannotPayUnkickedSpellButPaysKickedSpell() {
        readyThrone().setCounterCount(CounterType.CHARGE, 1);
        harness.activateAbility(player1, 0, 2, null, null);
        harness.handleListChoice(player1, ManaColor.RED.name());
        harness.setHand(player1, List.of(new AcademyDrake()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);

        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castKickedCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() instanceof AcademyDrake);
        assertThat(gd.playerManaPools.get(player1.getId()).getKickedOnlyMana(ManaColor.RED)).isZero();
    }

    private Permanent readyThrone() {
        Permanent throne = harness.addToBattlefieldAndReturn(player1, new ThroneOfMakindi());
        throne.setSummoningSick(false);
        return throne;
    }
}

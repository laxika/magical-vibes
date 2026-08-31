package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({UthrosTitanicGodcore.class, GrizzlyBears.class, Ornithopter.class})
class UthrosTitanicGodcoreTest extends BaseCardTest {

    @Test
    @DisplayName("Uthros enters the battlefield tapped")
    void entersTapped() {
        harness.setHand(player1, List.of(new UthrosTitanicGodcore()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.playLand(player1, 0);

        assertThat(findPermanent(player1, "Uthros, Titanic Godcore").isTapped()).isTrue();
    }

    @Test
    @DisplayName("Uthros adds one blue mana")
    void addsOneBlueMana() {
        Permanent uthros = harness.addToBattlefieldAndReturn(player1, new UthrosTitanicGodcore());

        harness.activateAbility(player1, battlefieldIndex(uthros), 0, null, null);

        assertThat(uthros.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Station puts counters equal to the tapped creature's power on Uthros")
    void stationUsesTappedCreaturePower() {
        Permanent uthros = harness.addToBattlefieldAndReturn(player1, new UthrosTitanicGodcore());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        harness.activateAbility(player1, battlefieldIndex(uthros), 1, null, null);
        bears.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.passBothPriorities();

        assertThat(bears.isTapped()).isTrue();
        assertThat(uthros.getCounterCount(CounterType.CHARGE)).isEqualTo(3);
    }

    @Test
    @DisplayName("The charged mana ability adds blue mana for each artifact its controller controls")
    void chargedManaAbilityCountsControlledArtifacts() {
        Permanent uthros = harness.addToBattlefieldAndReturn(player1, new UthrosTitanicGodcore());
        harness.addToBattlefield(player1, new Ornithopter());
        harness.addToBattlefield(player1, new Ornithopter());
        harness.addToBattlefield(player2, new Ornithopter());
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, battlefieldIndex(uthros), 2, null, null);

        assertThat(uthros.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(2);
    }

    private int battlefieldIndex(Permanent permanent) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(permanent);
    }
}

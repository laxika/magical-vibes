package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.e.EumidianTerrabotanist;
import com.github.laxika.magicalvibes.cards.h.Hullcarver;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({UthrosTitanicGodcore.class, EumidianTerrabotanist.class, Hullcarver.class})
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
        Permanent bears = addCreatureReady(player1, new EumidianTerrabotanist());

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
        uthros.setCounterCount(CounterType.CHARGE, 12);
        harness.addToBattlefield(player1, new Hullcarver());
        harness.addToBattlefield(player1, new Hullcarver());
        harness.addToBattlefield(player2, new Hullcarver());
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, battlefieldIndex(uthros), 2, null, null);

        assertThat(uthros.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(2);
    }

    @Test
    @DisplayName("The artifact mana ability is unavailable without charge counters")
    void artifactManaAbilityRequiresChargeCounters() {
        Permanent uthros = harness.addToBattlefieldAndReturn(player1, new UthrosTitanicGodcore());
        harness.addToBattlefield(player1, new Hullcarver());
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, battlefieldIndex(uthros), 2, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(uthros.isTapped()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Eleven charge counters do not unlock the artifact mana ability")
    void artifactManaAbilityRequiresTwelveCounters() {
        Permanent uthros = harness.addToBattlefieldAndReturn(player1, new UthrosTitanicGodcore());
        uthros.setCounterCount(CounterType.CHARGE, 11);
        harness.addToBattlefield(player1, new Hullcarver());
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, battlefieldIndex(uthros), 2, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The unlocked mana ability consumes its cost even with no artifacts")
    void artifactManaAbilityWithNoArtifacts() {
        Permanent uthros = harness.addToBattlefieldAndReturn(player1, new UthrosTitanicGodcore());
        uthros.setCounterCount(CounterType.CHARGE, 12);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, battlefieldIndex(uthros), 2, null, null);

        assertThat(uthros.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isZero();
        assertThat(gd.stack).isEmpty();
        assertThat(uthros.getCounterCount(CounterType.CHARGE)).isEqualTo(12);
    }

    @Test
    @DisplayName("A tapped Planet can be stationed by a summoning-sick creature")
    void stationAllowsTappedSourceAndSummoningSickCreature() {
        Permanent uthros = harness.addToBattlefieldAndReturn(player1, new UthrosTitanicGodcore());
        uthros.tap();
        uthros.setCounterCount(CounterType.CHARGE, 10);
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new EumidianTerrabotanist());
        creature.setSummoningSick(true);

        harness.activateAbility(player1, battlefieldIndex(uthros), 1, null, null);
        assertThat(creature.isTapped()).isTrue();
        assertThat(uthros.getCounterCount(CounterType.CHARGE)).isEqualTo(10);
        harness.passBothPriorities();

        assertThat(uthros.getCounterCount(CounterType.CHARGE)).isEqualTo(12);
    }

    @Test
    @DisplayName("Station cannot be activated during combat")
    void stationRequiresSorceryTiming() {
        Permanent uthros = harness.addToBattlefieldAndReturn(player1, new UthrosTitanicGodcore());
        Permanent creature = addCreatureReady(player1, new EumidianTerrabotanist());
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);

        assertThatThrownBy(() -> harness.activateAbility(player1, battlefieldIndex(uthros), 1, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(creature.isTapped()).isFalse();
        assertThat(uthros.getCounterCount(CounterType.CHARGE)).isZero();
    }

    private int battlefieldIndex(Permanent permanent) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(permanent);
    }
}

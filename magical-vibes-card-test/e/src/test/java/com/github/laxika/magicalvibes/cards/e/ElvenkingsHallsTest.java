package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.a.Attercop;
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

@CardUsed({ElvenkingsHalls.class, ElvenkingsHarper.class, Attercop.class})
class ElvenkingsHallsTest extends BaseCardTest {

    @Test
    @DisplayName("Enters the battlefield tapped")
    void entersTapped() {
        harness.setHand(player1, List.of(new ElvenkingsHalls()));

        harness.playLand(player1, 0);

        assertThat(findPermanent(player1, "Elvenking's Halls").isTapped()).isTrue();
    }

    @Test
    @DisplayName("Tapping for green mana produces one green")
    void tapsForGreenMana() {
        tapFor(ManaColor.GREEN);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isOne();
    }

    @Test
    @DisplayName("Tapping for blue mana produces one blue")
    void tapsForBlueMana() {
        tapFor(ManaColor.BLUE);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isOne();
    }

    @Test
    @DisplayName("Sacrificing the land puts two +1/+1 counters on an Elf you control")
    void sacrificeAbilityPutsCountersOnElf() {
        Permanent halls = addReadyHalls();
        Permanent elf = harness.addToBattlefieldAndReturn(player1, new ElvenkingsHarper());
        addCounterAbilityMana();

        harness.activateAbility(player1, 0, 1, null, elf.getId());
        harness.assertInGraveyard(player1, "Elvenking's Halls");
        assertThat(elf.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.passBothPriorities();

        assertThat(elf.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(halls);
        harness.assertInGraveyard(player1, "Elvenking's Halls");
    }

    @Test
    @DisplayName("The counter ability cannot target a non-Elf")
    void counterAbilityCannotTargetNonElf() {
        addReadyHalls();
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new Attercop());
        addCounterAbilityMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, bears.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Elf you control");
    }

    @Test
    @DisplayName("The counter ability can only be activated as a sorcery")
    void counterAbilityIsSorcerySpeed() {
        addReadyHalls();
        Permanent elf = harness.addToBattlefieldAndReturn(player1, new ElvenkingsHarper());
        addCounterAbilityMana();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.END_STEP);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, elf.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery speed");
    }

    @Test
    void counterAbilityCannotTargetOpponentsElf() {
        addReadyHalls();
        Permanent elf = harness.addToBattlefieldAndReturn(player2, new ElvenkingsHarper());
        addCounterAbilityMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, elf.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Elvenking's Halls");
        harness.assertNotInGraveyard(player1, "Elvenking's Halls");
        assertThat(elf.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void tappedLandCannotPayCounterAbilityCost() {
        Permanent halls = addReadyHalls();
        halls.tap();
        Permanent elf = harness.addToBattlefieldAndReturn(player1, new ElvenkingsHarper());
        addCounterAbilityMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, elf.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Elvenking's Halls");
        assertThat(elf.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void counterAbilityCannotBeActivatedDuringOpponentsMainPhase() {
        addReadyHalls();
        Permanent elf = harness.addToBattlefieldAndReturn(player1, new ElvenkingsHarper());
        addCounterAbilityMana();
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, elf.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery speed");
    }

    @Test
    void counterAbilityCannotBeActivatedWithNonemptyStack() {
        addReadyHalls();
        Permanent elf = harness.addToBattlefieldAndReturn(player1, new ElvenkingsHarper());
        addCounterAbilityMana();
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.activateAbility(player1, 1, 0, null, elf.getId());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, elf.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("stack is empty");
        harness.assertOnBattlefield(player1, "Elvenking's Halls");
    }

    @Test
    void countersAreNotPlacedIfTargetLeavesBeforeResolution() {
        addReadyHalls();
        Permanent elf = harness.addToBattlefieldAndReturn(player1, new ElvenkingsHarper());
        addCounterAbilityMana();
        harness.activateAbility(player1, 0, 1, null, elf.getId());

        gd.playerBattlefields.get(player1.getId()).remove(elf);
        gd.playerGraveyards.get(player1.getId()).add(elf.getCard());
        harness.passBothPriorities();

        assertThat(elf.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.assertInGraveyard(player1, "Elvenking's Halls");
    }

    private void tapFor(ManaColor color) {
        Permanent halls = addReadyHalls();

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, color.name());

        assertThat(halls.isTapped()).isTrue();
    }

    private Permanent addReadyHalls() {
        Permanent halls = harness.addToBattlefieldAndReturn(player1, new ElvenkingsHalls());
        halls.untap();
        return halls;
    }

    private void addCounterAbilityMana() {
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
    }
}

package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.f.FyndhornElves;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
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

@CardUsed({ElvenkingsHalls.class, FyndhornElves.class, GrizzlyBears.class})
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
        Permanent elf = harness.addToBattlefieldAndReturn(player1, new FyndhornElves());
        addCounterAbilityMana();

        harness.activateAbility(player1, 0, 1, null, elf.getId());
        harness.passBothPriorities();

        assertThat(elf.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(halls);
        harness.assertInGraveyard(player1, "Elvenking's Halls");
    }

    @Test
    @DisplayName("The counter ability cannot target a non-Elf")
    void counterAbilityCannotTargetNonElf() {
        addReadyHalls();
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        addCounterAbilityMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, bears.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Elf you control");
    }

    @Test
    @DisplayName("The counter ability can only be activated as a sorcery")
    void counterAbilityIsSorcerySpeed() {
        addReadyHalls();
        Permanent elf = harness.addToBattlefieldAndReturn(player1, new FyndhornElves());
        addCounterAbilityMana();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, elf.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery speed");
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

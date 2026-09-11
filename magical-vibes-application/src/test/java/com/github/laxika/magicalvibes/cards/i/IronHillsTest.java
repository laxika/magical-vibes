package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.d.DwarvenLieutenant;
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

@CardUsed({IronHills.class, DwarvenLieutenant.class, GrizzlyBears.class})
class IronHillsTest extends BaseCardTest {

    @Test
    @DisplayName("Enters the battlefield tapped")
    void entersTapped() {
        harness.setHand(player1, List.of(new IronHills()));

        harness.playLand(player1, 0);

        assertThat(findPermanent(player1, "Iron Hills").isTapped()).isTrue();
    }

    @Test
    @DisplayName("Tapping for red mana produces one red")
    void tapsForRedMana() {
        tapFor(ManaColor.RED);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isOne();
    }

    @Test
    @DisplayName("Tapping for white mana produces one white")
    void tapsForWhiteMana() {
        tapFor(ManaColor.WHITE);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isOne();
    }

    @Test
    @DisplayName("Sacrificing the land puts two +1/+1 counters on a Dwarf you control")
    void sacrificeAbilityPutsCountersOnDwarf() {
        Permanent hills = addReadyHills();
        Permanent dwarf = harness.addToBattlefieldAndReturn(player1, new DwarvenLieutenant());
        addCounterAbilityMana();

        harness.activateAbility(player1, 0, 1, null, dwarf.getId());
        harness.passBothPriorities();

        assertThat(dwarf.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(hills);
        harness.assertInGraveyard(player1, "Iron Hills");
    }

    @Test
    @DisplayName("The counter ability cannot target a non-Dwarf")
    void counterAbilityCannotTargetNonDwarf() {
        addReadyHills();
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        addCounterAbilityMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, bears.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Dwarf you control");
    }

    @Test
    @DisplayName("The counter ability can only be activated as a sorcery")
    void counterAbilityIsSorcerySpeed() {
        addReadyHills();
        Permanent dwarf = harness.addToBattlefieldAndReturn(player1, new DwarvenLieutenant());
        addCounterAbilityMana();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, dwarf.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery speed");
    }

    private void tapFor(ManaColor color) {
        Permanent hills = addReadyHills();

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, color.name());

        assertThat(hills.isTapped()).isTrue();
    }

    private Permanent addReadyHills() {
        Permanent hills = harness.addToBattlefieldAndReturn(player1, new IronHills());
        hills.untap();
        return hills;
    }

    private void addCounterAbilityMana() {
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
    }
}

package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.v.VeteranCathar;
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

@CardUsed({LakeTown.class, VeteranCathar.class, GrizzlyBears.class})
class LakeTownTest extends BaseCardTest {

    @Test
    @DisplayName("Enters the battlefield tapped")
    void entersTapped() {
        harness.setHand(player1, List.of(new LakeTown()));

        harness.playLand(player1, 0);

        assertThat(findPermanent(player1, "Lake-town").isTapped()).isTrue();
    }

    @Test
    @DisplayName("Tapping for white mana produces one white")
    void tapsForWhiteMana() {
        tapFor(ManaColor.WHITE);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isOne();
    }

    @Test
    @DisplayName("Tapping for blue mana produces one blue")
    void tapsForBlueMana() {
        tapFor(ManaColor.BLUE);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isOne();
    }

    @Test
    @DisplayName("Sacrificing the land puts two +1/+1 counters on a Human you control")
    void sacrificeAbilityPutsCountersOnHuman() {
        Permanent lakeTown = addReadyLakeTown();
        Permanent human = harness.addToBattlefieldAndReturn(player1, new VeteranCathar());
        addCounterAbilityMana();

        harness.activateAbility(player1, 0, 1, null, human.getId());
        harness.passBothPriorities();

        assertThat(human.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(lakeTown);
        harness.assertInGraveyard(player1, "Lake-town");
    }

    @Test
    @DisplayName("The counter ability cannot target a non-Human")
    void counterAbilityCannotTargetNonHuman() {
        addReadyLakeTown();
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        addCounterAbilityMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, bears.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Human you control");
    }

    @Test
    @DisplayName("The counter ability can only be activated as a sorcery")
    void counterAbilityIsSorcerySpeed() {
        addReadyLakeTown();
        Permanent human = harness.addToBattlefieldAndReturn(player1, new VeteranCathar());
        addCounterAbilityMana();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, human.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery speed");
    }

    private void tapFor(ManaColor color) {
        Permanent lakeTown = addReadyLakeTown();

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, color.name());

        assertThat(lakeTown.isTapped()).isTrue();
    }

    private Permanent addReadyLakeTown() {
        Permanent lakeTown = harness.addToBattlefieldAndReturn(player1, new LakeTown());
        lakeTown.untap();
        return lakeTown;
    }

    private void addCounterAbilityMana() {
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
    }
}

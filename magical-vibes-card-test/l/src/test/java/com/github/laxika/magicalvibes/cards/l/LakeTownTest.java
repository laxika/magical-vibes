package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.o.OrdinaryBear;
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

@CardUsed({LakeTown.class, LakeTownLookout.class, OrdinaryBear.class})
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
        Permanent human = harness.addToBattlefieldAndReturn(player1, new LakeTownLookout());
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
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new OrdinaryBear());
        addCounterAbilityMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, bears.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Human you control");
    }

    @Test
    @DisplayName("The counter ability can only be activated as a sorcery")
    void counterAbilityIsSorcerySpeed() {
        addReadyLakeTown();
        Permanent human = harness.addToBattlefieldAndReturn(player1, new LakeTownLookout());
        addCounterAbilityMana();
        harness.forceStep(TurnStep.END_STEP);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, human.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery speed");
    }

    @Test
    @DisplayName("Sacrifice and mana are paid before counters resolve")
    void paysCostsBeforeResolution() {
        addReadyLakeTown();
        Permanent human = harness.addToBattlefieldAndReturn(player1, new LakeTownLookout());
        addCounterAbilityMana();

        harness.activateAbility(player1, 0, 1, null, human.getId());

        harness.assertNotOnBattlefield(player1, "Lake-town");
        harness.assertInGraveyard(player1, "Lake-town");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isZero();
        assertThat(human.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(human.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("The counter ability cannot target an opponent's Human")
    void counterAbilityCannotTargetOpponentsHuman() {
        addReadyLakeTown();
        Permanent human = harness.addToBattlefieldAndReturn(player2, new LakeTownLookout());
        addCounterAbilityMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, human.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Human you control");

        harness.assertOnBattlefield(player1, "Lake-town");
        assertThat(human.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("The counter ability requires an untapped land")
    void counterAbilityCannotUseTappedLand() {
        Permanent lakeTown = addReadyLakeTown();
        lakeTown.tap();
        Permanent human = harness.addToBattlefieldAndReturn(player1, new LakeTownLookout());
        addCounterAbilityMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, human.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Lake-town");
        assertThat(human.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("The counter ability cannot be activated during an opponent's main phase")
    void counterAbilityCannotActivateOnOpponentsTurn() {
        addReadyLakeTown();
        Permanent human = harness.addToBattlefieldAndReturn(player1, new LakeTownLookout());
        addCounterAbilityMana();
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, human.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery speed");
    }

    @Test
    @DisplayName("The counter ability requires an empty stack")
    void counterAbilityCannotActivateWithNonemptyStack() {
        addReadyLakeTown();
        addReadyLakeTown();
        Permanent human = harness.addToBattlefieldAndReturn(player1, new LakeTownLookout());
        addCounterAbilityMana();
        addCounterAbilityMana();

        harness.activateAbility(player1, 0, 1, null, human.getId());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, human.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("stack is empty");

        assertThat(gd.stack).hasSize(1);
        harness.assertOnBattlefield(player1, "Lake-town");
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

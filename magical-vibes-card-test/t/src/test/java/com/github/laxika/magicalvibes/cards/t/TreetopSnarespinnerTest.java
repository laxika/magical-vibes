package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
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

@CardUsed({TreetopSnarespinner.class, GrizzlyBears.class, SerraAngel.class})
class TreetopSnarespinnerTest extends BaseCardTest {

    @Test
    @DisplayName("Ability puts a +1/+1 counter on a creature you control")
    void putsCounterOnTargetCreatureYouControl() {
        Permanent snarespinner = addReadySnarespinner();
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        prepareSorcerySpeed();
        addAbilityMana();

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(snarespinner.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Ability cannot target a creature an opponent controls")
    void cannotTargetOpponentCreature() {
        addReadySnarespinner();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        prepareSorcerySpeed();
        addAbilityMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature you control");
    }

    @Test
    @DisplayName("Ability can only be activated at sorcery speed")
    void requiresSorcerySpeed() {
        addReadySnarespinner();
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        addAbilityMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery speed");
    }

    @Test
    @DisplayName("A tapped, summoning-sick Snarespinner can put a counter on itself")
    void canTargetItselfWhileTappedAndSummoningSick() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new TreetopSnarespinner());
        source.setSummoningSick(true);
        source.tap();
        prepareSorcerySpeed();
        addAbilityMana();

        harness.activateAbility(player1, 0, null, source.getId());
        harness.passBothPriorities();

        assertThat(source.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(source.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Ability may be activated again after resolving during the same main phase")
    void canActivateRepeatedly() {
        Permanent source = addReadySnarespinner();
        prepareSorcerySpeed();
        harness.addMana(player1, ManaColor.GREEN, 6);

        harness.activateAbility(player1, 0, null, source.getId());
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, source.getId());
        harness.passBothPriorities();

        assertThat(source.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(source.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Ability cannot be activated while another ability is on the stack")
    void cannotActivateWithNonemptyStack() {
        Permanent source = addReadySnarespinner();
        prepareSorcerySpeed();
        harness.addMana(player1, ManaColor.GREEN, 6);
        harness.activateAbility(player1, 0, null, source.getId());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, source.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("stack is empty");

        harness.passBothPriorities();
        assertThat(source.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Ability cannot be activated during your upkeep")
    void cannotActivateOutsideMainPhase() {
        Permanent source = addReadySnarespinner();
        prepareSorcerySpeed();
        harness.forceStep(TurnStep.UPKEEP);
        addAbilityMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, source.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery speed");
    }

    @Test
    @DisplayName("Ability does not put counters on a target that changes controllers")
    void targetMustStillBeControlledOnResolution() {
        addReadySnarespinner();
        Permanent target = harness.addToBattlefieldAndReturn(player1, new TreetopSnarespinner());
        prepareSorcerySpeed();
        addAbilityMana();
        harness.activateAbility(player1, 0, null, target.getId());

        gd.playerBattlefields.get(player1.getId()).remove(target);
        gd.playerBattlefields.get(player2.getId()).add(target);
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Ability resolves even if its source leaves the battlefield")
    void resolvesWithoutSource() {
        Permanent source = addReadySnarespinner();
        Permanent target = harness.addToBattlefieldAndReturn(player1, new TreetopSnarespinner());
        prepareSorcerySpeed();
        addAbilityMana();
        harness.activateAbility(player1, 0, null, target.getId());

        gd.playerBattlefields.get(player1.getId()).remove(source);
        gd.playerGraveyards.get(player1.getId()).add(source.getCard());
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Reach allows blocking a flying Angel and deathtouch destroys it")
    void blocksFlyingCreatureAndDealsLethalDeathtouchDamage() {
        addCreatureReady(player1, new SerraAngel());
        addCreatureReady(player2, new TreetopSnarespinner());
        declareAttackersAndPrepareBlockers(List.of(0));

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        harness.assertInGraveyard(player1, "Serra Angel");
        harness.assertInGraveyard(player2, "Treetop Snarespinner");
        harness.assertLife(player2, 20);
    }

    private Permanent addReadySnarespinner() {
        return addCreatureReady(player1, new TreetopSnarespinner());
    }

    private void prepareSorcerySpeed() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

    private void addAbilityMana() {
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.GREEN, 1);
    }
}

package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.s.StrixhavenStadium;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({NoviceDissector.class, StrixhavenStadium.class})
class NoviceDissectorTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing another creature puts a +1/+1 counter on the target creature")
    void sacrificesAnotherCreatureAndPutsCounterOnTarget() {
        Permanent dissector = addCreatureReady(player1, new NoviceDissector());
        Permanent sacrifice = addCreatureReady(player1, new NoviceDissector());
        Permanent target = addCreatureReady(player2, new NoviceDissector());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        prepareMainPhase(player1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getId().equals(sacrifice.getId()))
                .anyMatch(permanent -> permanent.getId().equals(dissector.getId()));
    }

    @Test
    @DisplayName("Cannot sacrifice Novice Dissector itself")
    void cannotSacrificeItself() {
        Permanent dissector = addCreatureReady(player1, new NoviceDissector());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        prepareMainPhase(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, dissector.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreature() {
        addCreatureReady(player1, new NoviceDissector());
        addCreatureReady(player1, new NoviceDissector());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new StrixhavenStadium());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        prepareMainPhase(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can only be activated at sorcery speed")
    void sorcerySpeedOnly() {
        addCreatureReady(player1, new NoviceDissector());
        addCreatureReady(player1, new NoviceDissector());
        Permanent target = addCreatureReady(player2, new NoviceDissector());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        prepareMainPhase(player2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery speed");
    }

    @Test
    @DisplayName("Can target itself while tapped and summoning sick")
    void canTargetItselfWhileTappedAndSummoningSick() {
        Permanent dissector = harness.addToBattlefieldAndReturn(player1, new NoviceDissector());
        dissector.setSummoningSick(true);
        dissector.tap();
        Permanent sacrifice = addCreatureReady(player1, new NoviceDissector());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        prepareMainPhase(player1);

        harness.activateAbility(player1, 0, null, dissector.getId());

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(sacrifice);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(sacrifice.getCard());
        assertThat(dissector.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.passBothPriorities();

        assertThat(dissector.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(dissector.isTapped()).isTrue();
    }

    @Test
    @DisplayName("May sacrifice the targeted creature, leaving the ability without a legal target")
    void canSacrificeTheTarget() {
        Permanent dissector = addCreatureReady(player1, new NoviceDissector());
        Permanent sacrifice = addCreatureReady(player1, new NoviceDissector());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        prepareMainPhase(player1);

        harness.activateAbility(player1, 0, null, sacrifice.getId());

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(sacrifice);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(sacrifice.getCard());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(dissector.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(sacrifice.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Cannot activate during upkeep")
    void cannotActivateDuringUpkeep() {
        Permanent dissector = addCreatureReady(player1, new NoviceDissector());
        Permanent sacrifice = addCreatureReady(player1, new NoviceDissector());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, dissector.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery speed");
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(sacrifice);
    }

    @Test
    @DisplayName("Cannot activate while a spell is on the stack")
    void cannotActivateWithNonemptyStack() {
        Permanent dissector = addCreatureReady(player1, new NoviceDissector());
        Permanent sacrifice = addCreatureReady(player1, new NoviceDissector());
        prepareMainPhase(player1);
        harness.castFromHand(player1, new NoviceDissector(), "{3}{B}");
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, dissector.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("stack is empty");
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(sacrifice);
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Can activate in the postcombat main phase")
    void canActivateInPostcombatMain() {
        Permanent dissector = addCreatureReady(player1, new NoviceDissector());
        addCreatureReady(player1, new NoviceDissector());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        prepareMainPhase(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);

        harness.activateAbility(player1, 0, null, dissector.getId());
        harness.passBothPriorities();

        assertThat(dissector.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot pay the sacrifice cost with an opponent's creature")
    void cannotSacrificeOpponentsCreature() {
        Permanent dissector = addCreatureReady(player1, new NoviceDissector());
        Permanent opponent = addCreatureReady(player2, new NoviceDissector());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        prepareMainPhase(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, dissector.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(opponent);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cannot activate without the one mana payment")
    void cannotActivateWithoutMana() {
        Permanent dissector = addCreatureReady(player1, new NoviceDissector());
        Permanent sacrifice = addCreatureReady(player1, new NoviceDissector());
        prepareMainPhase(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, dissector.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(dissector, sacrifice);
        assertThat(gd.stack).isEmpty();
    }

    private void prepareMainPhase(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}

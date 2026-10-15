package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MBakuJabariChieftain;
import com.github.laxika.magicalvibes.cards.r.RoyalTalonFighterJet;
import com.github.laxika.magicalvibes.model.Card;
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

@CardUsed({NakiaWakandanOperative.class, GrizzlyBears.class, Forest.class, MBakuJabariChieftain.class, RoyalTalonFighterJet.class})
class NakiaWakandanOperativeTest extends BaseCardTest {

    @Test
    @DisplayName("Makes you the monarch when your commander enters")
    void commanderEnteringMakesYouMonarch() {
        addCreatureReady(player1, new NakiaWakandanOperative());
        Card commander = new MBakuJabariChieftain();
        gd.makeCommander(player1.getId(), commander);

        harness.enterBattlefieldAndReturn(player1, commander);
        harness.passBothPriorities();

        assertThat(gd.monarchPlayerId).isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Does not trigger when a noncommander enters")
    void noncommanderEnteringDoesNotMakeYouMonarch() {
        addCreatureReady(player1, new NakiaWakandanOperative());

        harness.enterBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.passBothPriorities();

        assertThat(gd.monarchPlayerId).isNull();
    }

    @Test
    @DisplayName("Puts two +1/+1 counters on a target creature")
    void putsTwoCountersOnTargetCreature() {
        Permanent nakia = addCreatureReady(player1, new NakiaWakandanOperative());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        prepareMainPhase(player1);

        harness.activateAbility(player1, battlefieldIndex(nakia), null, target.getId());
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(nakia.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Cannot target a noncreature non-Vehicle permanent")
    void cannotTargetLand() {
        Permanent nakia = addCreatureReady(player1, new NakiaWakandanOperative());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        prepareMainPhase(player1);

        assertThatThrownBy(() -> harness.activateAbility(
                player1, battlefieldIndex(nakia), null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can only be activated at sorcery speed")
    void sorcerySpeedOnly() {
        Permanent nakia = addCreatureReady(player1, new NakiaWakandanOperative());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        prepareMainPhase(player2);

        assertThatThrownBy(() -> harness.activateAbility(
                player1, battlefieldIndex(nakia), null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery speed");
    }

    @Test
    void makesYouMonarchWhenNakiaEntersAsYourCommander() {
        Card commander = new NakiaWakandanOperative();
        gd.makeCommander(player1.getId(), commander);

        harness.enterBattlefieldAndReturn(player1, commander);
        resolveAllTriggers();

        assertThat(gd.monarchPlayerId).isEqualTo(player1.getId());
    }

    @Test
    void yourCommanderEnteringUnderOpponentControlMakesYouMonarch() {
        addCreatureReady(player1, new NakiaWakandanOperative());
        Card commander = new MBakuJabariChieftain();
        gd.makeCommander(player1.getId(), commander);

        harness.enterBattlefieldAndReturn(player2, commander);
        resolveAllTriggers();

        assertThat(gd.monarchPlayerId).isEqualTo(player1.getId());
    }

    @Test
    void opponentsCommanderEnteringUnderYourControlDoesNotTrigger() {
        addCreatureReady(player1, new NakiaWakandanOperative());
        Card commander = new MBakuJabariChieftain();
        gd.makeCommander(player2.getId(), commander);

        harness.enterBattlefieldAndReturn(player1, commander);
        resolveAllTriggers();

        assertThat(gd.monarchPlayerId).isNull();
    }

    @Test
    void opponentsCommanderEnteringUnderOpponentControlDoesNotTrigger() {
        addCreatureReady(player1, new NakiaWakandanOperative());
        Card commander = new MBakuJabariChieftain();
        gd.makeCommander(player2.getId(), commander);

        harness.enterBattlefieldAndReturn(player2, commander);
        resolveAllTriggers();

        assertThat(gd.monarchPlayerId).isNull();
    }

    @Test
    void commanderEnteringTakesMonarchyFromOpponent() {
        addCreatureReady(player1, new NakiaWakandanOperative());
        gd.monarchPlayerId = player2.getId();
        Card commander = new MBakuJabariChieftain();
        gd.makeCommander(player1.getId(), commander);

        harness.enterBattlefieldAndReturn(player1, commander);
        resolveAllTriggers();

        assertThat(gd.monarchPlayerId).isEqualTo(player1.getId());
    }

    @Test
    void putsCountersOnAnUncrewedVehicle() {
        Permanent nakia = addCreatureReady(player1, new NakiaWakandanOperative());
        Permanent vehicle = harness.addToBattlefieldAndReturn(player2, new RoyalTalonFighterJet());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        prepareMainPhase(player1);

        harness.activateAbility(player1, battlefieldIndex(nakia), null, vehicle.getId());
        harness.passBothPriorities();

        assertThat(vehicle.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(nakia.isTapped()).isTrue();
    }

    @Test
    void canTargetItself() {
        Permanent nakia = addCreatureReady(player1, new NakiaWakandanOperative());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        prepareMainPhase(player1);

        harness.activateAbility(player1, battlefieldIndex(nakia), null, nakia.getId());
        harness.passBothPriorities();

        assertThat(nakia.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void cannotActivateWhileSummoningSick() {
        Permanent nakia = harness.addToBattlefieldAndReturn(player1, new NakiaWakandanOperative());
        nakia.setSummoningSick(true);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        prepareMainPhase(player1);

        assertThatThrownBy(() -> harness.activateAbility(
                player1, battlefieldIndex(nakia), null, nakia.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(nakia.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void cannotActivateWithoutTwoMana() {
        Permanent nakia = addCreatureReady(player1, new NakiaWakandanOperative());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        prepareMainPhase(player1);

        assertThatThrownBy(() -> harness.activateAbility(
                player1, battlefieldIndex(nakia), null, nakia.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(nakia.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void cannotActivateDuringCombat() {
        Permanent nakia = addCreatureReady(player1, new NakiaWakandanOperative());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        prepareMainPhase(player1);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);

        assertThatThrownBy(() -> harness.activateAbility(
                player1, battlefieldIndex(nakia), null, nakia.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery speed");
    }

    @Test
    void cannotActivateWithASpellOnTheStack() {
        Permanent nakia = addCreatureReady(player1, new NakiaWakandanOperative());
        prepareMainPhase(player1);
        harness.castFromHand(player1, new GrizzlyBears(), "{1}{G}");
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(
                player1, battlefieldIndex(nakia), null, nakia.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("stack is empty");
    }

    private int battlefieldIndex(Permanent permanent) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(permanent);
    }

    private void prepareMainPhase(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}

package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HowlpackAvenger;
import com.github.laxika.magicalvibes.cards.j.JaceBeleren;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.DayNight;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({IllTemperedLoner.class, HowlpackAvenger.class, Shock.class, GrizzlyBears.class, JaceBeleren.class})
class IllTemperedLonerTest extends BaseCardTest {

    @Test
    void frontFaceReflectsDamageToAnyTarget() {
        Permanent loner = harness.addToBattlefieldAndReturn(player2, new IllTemperedLoner());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.setLife(player1, 20);

        harness.castInstant(player1, 0, loner.getId());
        harness.passBothPriorities();

        harness.handlePermanentChosen(player2, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
    }

    @Test
    void backFaceReflectsDamageDealtToAPlaneswalker() {
        Permanent avenger = harness.addToBattlefieldAndReturn(player2, new HowlpackAvenger());
        Permanent jace = harness.addToBattlefieldAndReturn(player2, new JaceBeleren());
        jace.setCounterCount(CounterType.LOYALTY, 3);
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.setLife(player1, 20);

        harness.castInstant(player1, 0, jace.getId());
        harness.passBothPriorities();

        harness.handlePermanentChosen(player2, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
        assertThat(jace.getCounterCount(CounterType.LOYALTY)).isEqualTo(1);
        assertThat(avenger.getCard()).isInstanceOf(HowlpackAvenger.class);
    }

    @Test
    void backFaceReflectsCombatDamageToAControlledPermanent() {
        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent avenger = harness.addToBattlefieldAndReturn(player2, new HowlpackAvenger());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        avenger.setSummoningSick(false);
        avenger.setBlocking(true);
        avenger.addBlockingTarget(0);
        harness.setLife(player1, 20);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        harness.handlePermanentChosen(player2, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
    }

    @Test
    void dayNightTransformsBothFaces() {
        gd.dayNight = DayNight.DAY;
        Permanent loner = harness.addToBattlefieldAndReturn(player1, new IllTemperedLoner());

        gd.spellsCastLastTurn.clear();
        advanceToNextTurn(player1);
        assertThat(gd.dayNight).isEqualTo(DayNight.NIGHT);
        assertThat(loner.getCard()).isInstanceOf(HowlpackAvenger.class);

        gd.recordSpellCast(player1.getId(), new Shock());
        gd.recordSpellCast(player1.getId(), new Shock());
        advanceToNextTurn(player2);
        assertThat(loner.getCard()).isInstanceOf(IllTemperedLoner.class);
    }

    @Test
    void bothFacesCanActivateTheirBoostAbility() {
        Permanent loner = addReady(player1, new IllTemperedLoner());
        activateBoost(loner);
        assertThat(gqs.getEffectivePower(gd, loner)).isEqualTo(loner.getBasePower() + 2);

        Permanent avenger = addReady(player2, new HowlpackAvenger());
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.activateAbility(player2, 0, 0, null, null);
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, avenger)).isEqualTo(avenger.getBasePower() + 2);
    }

    private Permanent addReady(com.github.laxika.magicalvibes.model.Player player, Card card) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player, card);
        permanent.setSummoningSick(false);
        return permanent;
    }

    private void activateBoost(Permanent permanent) {
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(permanent), 0, null, null);
        harness.passBothPriorities();
    }

    private void advanceToNextTurn(com.github.laxika.magicalvibes.model.Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.CLEANUP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}

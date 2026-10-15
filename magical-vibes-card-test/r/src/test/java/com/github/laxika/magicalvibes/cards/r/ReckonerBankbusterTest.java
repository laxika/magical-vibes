package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.c.CoilingStalker;
import com.github.laxika.magicalvibes.cards.m.MarchOfOtherworldlyLight;
import com.github.laxika.magicalvibes.cards.t.TrainedArynx;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ReckonerBankbuster.class, CoilingStalker.class, MarchOfOtherworldlyLight.class})
class ReckonerBankbusterTest extends BaseCardTest {

    @Test
    void entersWithThreeChargeCounters() {
        harness.castFromHand(player1, new ReckonerBankbuster(), "{2}");
        harness.passBothPriorities();

        Permanent bankbuster = findPermanent(player1, "Reckoner Bankbuster");
        assertThat(bankbuster.getCounterCount(CounterType.CHARGE)).isEqualTo(3);
    }

    @Test
    void drawsAndRemovesChargeCounter() {
        Permanent bankbuster = addBankbusterWithCounters(3);
        CoilingStalker drawnCard = new CoilingStalker();
        harness.setLibrary(player1, List.of(drawnCard));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(bankbuster.getCounterCount(CounterType.CHARGE)).isEqualTo(2);
        assertThat(gd.playerHands.get(player1.getId())).contains(drawnCard);
    }

    @Test
    void lastChargeCounterCreatesTreasureAndPilot() {
        Permanent bankbuster = addBankbusterWithCounters(1);
        harness.setLibrary(player1, List.of(new CoilingStalker()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(bankbuster.getCounterCount(CounterType.CHARGE)).isZero();
        assertThat(findPermanents(player1, "Treasure")).hasSize(1);
        assertThat(findPermanents(player1, "Pilot")).hasSize(1);
    }

    @Test
    void pilotCanCrewVehicleWithItsPowerBonus() {
        Permanent bankbuster = addBankbusterWithCounters(1);
        harness.setLibrary(player1, List.of(new CoilingStalker()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        Permanent pilot = findPermanent(player1, "Pilot");
        bankbuster.untap();
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(bankbuster), 1, null, null);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, bankbuster)).isTrue();
        assertThat(pilot.isTapped()).isTrue();
        assertThat(pilot.getCard().getSubtypes()).contains(CardSubtype.PILOT);
    }

    @Test
    void cannotActivateDrawAbilityWithoutChargeCounters() {
        Permanent bankbuster = addBankbusterWithCounters(0);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(
                player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(bankbuster),
                0,
                null,
                null
        )).isInstanceOf(IllegalStateException.class);
    }

    @Test
    @CardUsed(TrainedArynx.class)
    void pilotPowerBonusDoesNotApplyToSaddle() {
        addBankbusterWithCounters(1);
        harness.setLibrary(player1, List.of(new CoilingStalker()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        Permanent pilot = findPermanent(player1, "Pilot");
        Permanent mount = harness.addToBattlefieldAndReturn(player1, new TrainedArynx());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(mount), 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(pilot.isTapped()).isFalse();
        assertThat(mount.isSaddled()).isFalse();
    }

    @Test
    void paysManaTapAndCounterBeforeDrawing() {
        Permanent bankbuster = addBankbusterWithCounters(2);
        CoilingStalker drawnCard = new CoilingStalker();
        harness.setLibrary(player1, List.of(drawnCard));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(bankbuster.isTapped()).isTrue();
        assertThat(bankbuster.getCounterCount(CounterType.CHARGE)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isZero();
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(drawnCard);

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(drawnCard);
        assertThat(findPermanents(player1, "Treasure")).isEmpty();
        assertThat(findPermanents(player1, "Pilot")).isEmpty();
    }

    @Test
    void checksChargeCountersAtResolution() {
        Permanent bankbuster = addBankbusterWithCounters(1);
        CoilingStalker drawnCard = new CoilingStalker();
        harness.setLibrary(player1, List.of(drawnCard));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 0, null, null);
        bankbuster.setCounterCount(CounterType.CHARGE, 1);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(drawnCard);
        assertThat(findPermanents(player1, "Treasure")).isEmpty();
        assertThat(findPermanents(player1, "Pilot")).isEmpty();
    }

    @Test
    void lastCounterStillCreatesTokensAfterSourceIsExiled() {
        assertDrawAfterSourceIsExiled(1, 1);
    }

    @Test
    void remainingCountersPreventTokensAfterSourceIsExiled() {
        assertDrawAfterSourceIsExiled(2, 0);
    }

    @Test
    void cannotActivateDrawWhileTapped() {
        Permanent bankbuster = addBankbusterWithCounters(3);
        bankbuster.tap();
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(bankbuster.getCounterCount(CounterType.CHARGE)).isEqualTo(3);
    }

    @Test
    void cannotActivateDrawWithoutTwoMana() {
        Permanent bankbuster = addBankbusterWithCounters(3);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(bankbuster.isTapped()).isFalse();
        assertThat(bankbuster.getCounterCount(CounterType.CHARGE)).isEqualTo(3);
    }

    @Test
    void twoPowerCreatureCannotPayCrewThreeAlone() {
        Permanent bankbuster = addBankbusterWithCounters(3);
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new CoilingStalker());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(creature.isTapped()).isFalse();
        assertThat(gqs.isCreature(gd, bankbuster)).isFalse();
    }

    @Test
    void multipleCreaturesCanPayCrewThreeWithoutMana() {
        Permanent bankbuster = addBankbusterWithCounters(3);
        Permanent first = harness.addToBattlefieldAndReturn(player1, new CoilingStalker());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new CoilingStalker());

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(first.isTapped()).isTrue();
        assertThat(second.isTapped()).isTrue();
        assertThat(bankbuster.isTapped()).isFalse();
        assertThat(gqs.isCreature(gd, bankbuster)).isTrue();
    }

    private void assertDrawAfterSourceIsExiled(int counters, int expectedTokens) {
        Permanent bankbuster = addBankbusterWithCounters(counters);
        CoilingStalker drawnCard = new CoilingStalker();
        harness.setLibrary(player1, List.of(drawnCard));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, 0, null, null);

        harness.setHand(player2, List.of(new MarchOfOtherworldlyLight()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.castInstantForXWithDiscards(player2, 0, 2, List.of(bankbuster.getId()), List.of());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Reckoner Bankbuster")).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).contains(drawnCard);
        assertThat(findPermanents(player1, "Treasure")).hasSize(expectedTokens);
        assertThat(findPermanents(player1, "Pilot")).hasSize(expectedTokens);
    }

    private Permanent addBankbusterWithCounters(int counters) {
        Permanent bankbuster = harness.addToBattlefieldAndReturn(player1, new ReckonerBankbuster());
        bankbuster.setCounterCount(CounterType.CHARGE, counters);
        return bankbuster;
    }
}

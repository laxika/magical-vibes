package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.s.SakuraTribeElder;
import com.github.laxika.magicalvibes.cards.s.SolRing;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TayamLuminousEnigma.class, SakuraTribeElder.class, SolRing.class})
class TayamLuminousEnigmaTest extends BaseCardTest {

    @Test
    @DisplayName("Other creatures enter with vigilance counters")
    void otherCreaturesEnterWithVigilanceCounters() {
        harness.addToBattlefield(player1, new TayamLuminousEnigma());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castFromHand(player1, new SakuraTribeElder(), "{1}{G}");
        harness.passBothPriorities();

        Permanent elder = findPermanent(player1, "Sakura-Tribe Elder");
        assertThat(elder.getCounterCount(CounterType.VIGILANCE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Ability removes any three creature counters, mills, and returns a permanent")
    void removesAnyCountersMillsAndReturnsPermanent() {
        Permanent tayam = harness.addToBattlefieldAndReturn(player1, new TayamLuminousEnigma());
        tayam.setCounterCount(CounterType.VIGILANCE, 3);
        Card returned = new SakuraTribeElder();
        harness.setGraveyard(player1, List.of(returned));
        harness.setLibrary(player1, List.of(new SakuraTribeElder(), new SakuraTribeElder(), new SakuraTribeElder()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(tayam.getCounterCount(CounterType.VIGILANCE)).isZero();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.GraveyardChoice.class);

        harness.handleGraveyardCardChosen(player1,
                gd.playerGraveyards.get(player1.getId()).indexOf(returned));

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(returned.getId()));
    }

    @Test
    void tayamDoesNotGiveItselfAVigilanceCounter() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player1, new TayamLuminousEnigma(), "{1}{W}{B}{G}");
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Tayam, Luminous Enigma")
                .getCounterCount(CounterType.VIGILANCE)).isZero();
    }

    @Test
    void opposingCreaturesDoNotGetVigilanceCounters() {
        harness.addToBattlefield(player1, new TayamLuminousEnigma());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player2, new SakuraTribeElder(), "{1}{G}");
        harness.passBothPriorities();

        assertThat(findPermanent(player2, "Sakura-Tribe Elder")
                .getCounterCount(CounterType.VIGILANCE)).isZero();
    }

    @Test
    void canReturnANewlyMilledCardWithAShortLibrary() {
        Permanent tayam = harness.addToBattlefieldAndReturn(player1, new TayamLuminousEnigma());
        tayam.setCounterCount(CounterType.VIGILANCE, 3);
        Card returned = new SakuraTribeElder();
        harness.setGraveyard(player1, List.of());
        harness.setLibrary(player1, List.of(returned));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.handleGraveyardCardChosen(player1, 0);

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(findPermanent(player1, "Sakura-Tribe Elder")
                .getCounterCount(CounterType.VIGILANCE)).isEqualTo(1);
    }

    @Test
    void cannotReturnAPermanentWithManaValueAboveThree() {
        Permanent tayam = harness.addToBattlefieldAndReturn(player1, new TayamLuminousEnigma());
        tayam.setCounterCount(CounterType.VIGILANCE, 3);
        Card tooExpensive = new TayamLuminousEnigma();
        harness.setGraveyard(player1, List.of(tooExpensive));
        harness.setLibrary(player1, List.of());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(tooExpensive);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(tayam.getCounterCount(CounterType.VIGILANCE)).isZero();
    }

    @Test
    void paysCountersOfDifferentKindsAcrossMultipleCreatures() {
        Permanent tayam = harness.addToBattlefieldAndReturn(player1, new TayamLuminousEnigma());
        Permanent first = harness.addToBattlefieldAndReturn(player1, new SakuraTribeElder());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new SakuraTribeElder());
        tayam.setCounterCount(CounterType.VIGILANCE, 1);
        first.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        second.setCounterCount(CounterType.REACH, 1);
        harness.setLibrary(player1, List.of());
        harness.setGraveyard(player1, List.of());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(tayam.getCounterCount(CounterType.VIGILANCE)).isZero();
        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(second.getCounterCount(CounterType.REACH)).isZero();
        harness.passBothPriorities();
    }

    @Test
    void cannotPayWithOpponentsCounters() {
        harness.addToBattlefield(player1, new TayamLuminousEnigma());
        Permanent opponent = harness.addToBattlefieldAndReturn(player2, new SakuraTribeElder());
        opponent.setCounterCount(CounterType.VIGILANCE, 3);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(opponent.getCounterCount(CounterType.VIGILANCE)).isEqualTo(3);
    }

    @Test
    void returnsNoncreaturePermanentsWithoutGivingThemVigilanceCounters() {
        Permanent tayam = harness.addToBattlefieldAndReturn(player1, new TayamLuminousEnigma());
        tayam.setCounterCount(CounterType.VIGILANCE, 3);
        harness.setGraveyard(player1, List.of(new SolRing()));
        harness.setLibrary(player1, List.of());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.handleGraveyardCardChosen(player1, 0);

        assertThat(findPermanent(player1, "Sol Ring")
                .getCounterCount(CounterType.VIGILANCE)).isZero();
        harness.assertNotInGraveyard(player1, "Sol Ring");
    }

    @Test
    void cannotPayWithCountersOnNoncreaturePermanents() {
        harness.addToBattlefield(player1, new TayamLuminousEnigma());
        Permanent ring = harness.addToBattlefieldAndReturn(player1, new SolRing());
        ring.setCounterCount(CounterType.CHARGE, 3);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(ring.getCounterCount(CounterType.CHARGE)).isEqualTo(3);
    }

    @Test
    void mustLetControllerChooseCounterKindsWhenMoreThanThreeAreAvailable() {
        Permanent tayam = harness.addToBattlefieldAndReturn(player1, new TayamLuminousEnigma());
        tayam.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);
        tayam.setCounterCount(CounterType.VIGILANCE, 3);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(tayam.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(tayam.getCounterCount(CounterType.VIGILANCE)).isEqualTo(3);
        assertThat(gd.interaction.isAwaitingInput()).isTrue();
    }
}

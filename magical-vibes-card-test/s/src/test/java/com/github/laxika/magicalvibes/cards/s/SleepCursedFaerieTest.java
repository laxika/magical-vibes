package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.CandyGrapple;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SleepCursedFaerie.class, CandyGrapple.class})
class SleepCursedFaerieTest extends BaseCardTest {

    @Test
    @DisplayName("Enters tapped with three stun counters")
    void entersTappedWithThreeStunCounters() {
        harness.setHand(player1, List.of(new SleepCursedFaerie()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent faerie = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(faerie.isTapped()).isTrue();
        assertThat(faerie.getCounterCount(CounterType.STUN)).isEqualTo(3);
    }

    @Test
    @DisplayName("Untap ability removes stun counters before untapping")
    void untapAbilityRemovesStunCountersBeforeUntapping() {
        Permanent faerie = harness.enterBattlefieldAndReturn(player1, new SleepCursedFaerie());
        harness.addMana(player1, ManaColor.BLUE, 4);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        for (int i = 0; i < 3; i++) {
            harness.activateAbility(player1, 0, 0, null, null);
            harness.passBothPriorities();
            assertThat(faerie.isTapped()).isTrue();
            assertThat(faerie.getCounterCount(CounterType.STUN)).isEqualTo(2 - i);
        }

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(faerie.isTapped()).isFalse();
        assertThat(faerie.getCounterCount(CounterType.STUN)).isZero();
    }

    @Test
    @DisplayName("Each controller untap step removes one stun counter, then the next untaps")
    void normalUntapStepsRemoveOneCounterAtATime() {
        Permanent faerie = harness.enterBattlefieldAndReturn(player1, new SleepCursedFaerie());

        harness.performUntapStep(player2);
        assertThat(faerie.isTapped()).isTrue();
        assertThat(faerie.getCounterCount(CounterType.STUN)).isEqualTo(3);

        for (int remaining = 2; remaining >= 0; remaining--) {
            harness.performUntapStep(player1);
            assertThat(faerie.isTapped()).isTrue();
            assertThat(faerie.getCounterCount(CounterType.STUN)).isEqualTo(remaining);
        }

        harness.performUntapStep(player1);
        assertThat(faerie.isTapped()).isFalse();
        assertThat(faerie.getCounterCount(CounterType.STUN)).isZero();
    }

    @Test
    @DisplayName("Untap ability works during the opponent's turn while summoning sick")
    void canActivateDuringOpponentsTurn() {
        Permanent faerie = harness.enterBattlefieldAndReturn(player1, new SleepCursedFaerie());
        harness.forceActivePlayer(player2);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        assertThat(faerie.getCounterCount(CounterType.STUN)).isEqualTo(3);
        harness.passBothPriorities();

        assertThat(faerie.isTapped()).isTrue();
        assertThat(faerie.getCounterCount(CounterType.STUN)).isEqualTo(2);
    }

    @Test
    @DisplayName("Untapping an already untapped creature does not remove stun counters")
    void untapAbilityDoesNotRemoveCountersWhenAlreadyUntapped() {
        Permanent faerie = harness.addToBattlefieldAndReturn(player1, new SleepCursedFaerie());
        faerie.setCounterCount(CounterType.STUN, 3);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(faerie.isTapped()).isFalse();
        assertThat(faerie.getCounterCount(CounterType.STUN)).isEqualTo(3);
    }

    @Test
    @DisplayName("Ward counters an opponent's spell when they cannot pay")
    void wardCountersUnpaidOpponentSpell() {
        Permanent faerie = harness.enterBattlefieldAndReturn(player1, new SleepCursedFaerie());
        harness.setHand(player2, List.of(new CandyGrapple()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.castInstant(player2, 0, faerie.getId());
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(faerie);
        harness.assertInGraveyard(player2, "Candy Grapple");
        assertThat(faerie.getCounterCount(CounterType.STUN)).isEqualTo(3);
    }

    @Test
    @DisplayName("Paying ward allows the opponent's spell to resolve")
    void payingWardAllowsOpponentSpellToResolve() {
        Permanent faerie = harness.enterBattlefieldAndReturn(player1, new SleepCursedFaerie());
        harness.setHand(player2, List.of(new CandyGrapple()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 3);

        harness.castInstant(player2, 0, faerie.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Sleep-Cursed Faerie");
        assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Ward does not trigger for its controller's spell")
    void wardDoesNotTaxControllersSpell() {
        Permanent faerie = harness.enterBattlefieldAndReturn(player1, new SleepCursedFaerie());
        harness.setHand(player1, List.of(new CandyGrapple()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveInstant(player1, 0, faerie.getId());

        harness.assertInGraveyard(player1, "Sleep-Cursed Faerie");
        assertThat(gd.stack).isEmpty();
    }
}

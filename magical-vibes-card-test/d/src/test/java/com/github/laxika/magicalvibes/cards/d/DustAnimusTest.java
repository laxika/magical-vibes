package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.p.Plains;
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

@CardUsed({DustAnimus.class, Plains.class})
class DustAnimusTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with two +1/+1 counters and a lifelink counter with five untapped lands")
    void entersWithCountersAndLifelinkWithFiveUntappedLands() {
        addPlains(5);

        castDustAnimus();

        Permanent animus = findPermanent(player1, "Dust Animus");
        assertThat(animus.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(animus.getCounterCount(CounterType.LIFELINK)).isEqualTo(1);
    }

    @Test
    @DisplayName("Does not enter with counters when one of five lands is tapped")
    void doesNotEnterWithCountersWhenARequiredLandIsTapped() {
        addPlains(4);
        Permanent tappedPlains = harness.addToBattlefieldAndReturn(player1, new Plains());
        tappedPlains.tap();

        castDustAnimus();

        Permanent animus = findPermanent(player1, "Dust Animus");
        assertThat(animus.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(animus.getCounterCount(CounterType.LIFELINK)).isZero();
    }

    @Test
    @DisplayName("Opponent's untapped lands do not count")
    void doesNotCountOpponentsLands() {
        addPlains(4);
        harness.addToBattlefield(player2, new Plains());

        castDustAnimus();

        Permanent animus = findPermanent(player1, "Dust Animus");
        assertThat(animus.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(animus.getCounterCount(CounterType.LIFELINK)).isZero();
    }

    @Test
    void entersWithCountersWithoutBeingCast() {
        addPlains(6);

        Permanent animus = harness.enterBattlefieldAndReturn(player1, new DustAnimus());

        assertThat(animus.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(animus.getCounterCount(CounterType.LIFELINK)).isEqualTo(1);
    }

    @Test
    void lifelinkCounterGainsLifeFromCombatDamage() {
        addPlains(5);
        Permanent animus = harness.enterBattlefieldAndReturn(player1, new DustAnimus());
        animus.setSummoningSick(false);
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());
        int opponentLifeBefore = gd.playerLifeTotals.get(player2.getId());

        declareAttackers(player1, List.of(5));
        resolveCombat(player1);

        harness.assertLife(player1, lifeBefore + 4);
        harness.assertLife(player2, opponentLifeBefore - 4);
    }

    @Test
    void checksUntappedLandsAtResolutionRatherThanCasting() {
        addPlains(5);
        harness.setHand(player1, List.of(new DustAnimus()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0);

        harness.tapPermanent(player1, 0);
        harness.passBothPriorities();

        Permanent animus = findPermanent(player1, "Dust Animus");
        assertThat(animus.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(animus.getCounterCount(CounterType.LIFELINK)).isZero();
    }

    @Test
    void plottingExilesTheCardAndAllowsFreeCastingOnALaterTurn() {
        DustAnimus card = new DustAnimus();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castWithAlternateCost(player1, 0, List.of());

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
        assertThatThrownBy(() -> harness.castFromExile(player1, card.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.setHand(player2, List.of());
        harness.passUntil(player1, TurnStep.DECLARE_ATTACKERS);
        harness.beginAttackerDeclarationInput();
        gs.declareAttackers(gd, player1, List.of());
        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        harness.passUntil(player2, TurnStep.DECLARE_ATTACKERS);
        harness.beginAttackerDeclarationInput();
        gs.declareAttackers(gd, player2, List.of());
        harness.passUntil(player1, TurnStep.PRECOMBAT_MAIN);
        addPlains(5);

        harness.castFromExile(player1, card.getId());
        harness.passBothPriorities();

        Permanent animus = findPermanent(player1, "Dust Animus");
        assertThat(animus.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(animus.getCounterCount(CounterType.LIFELINK)).isEqualTo(1);
    }

    @Test
    void cannotPlotOutsideAMainPhase() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UPKEEP);
        harness.setHand(player1, List.of(new DustAnimus()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castWithAlternateCost(player1, 0, List.of()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    private void addPlains(int count) {
        for (int i = 0; i < count; i++) {
            harness.addToBattlefield(player1, new Plains());
        }
    }

    private void castDustAnimus() {
        harness.setHand(player1, List.of(new DustAnimus()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
    }
}

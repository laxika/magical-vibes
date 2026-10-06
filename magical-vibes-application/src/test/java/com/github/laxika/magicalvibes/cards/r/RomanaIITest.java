package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RomanaII.class, RaiseTheAlarm.class})
class RomanaIITest extends BaseCardTest {

    @Test
    @DisplayName("Creates a tapped copy of a token that entered this turn")
    void createsTappedTokenCopyOfTokenEnteredThisTurn() {
        Permanent romana = addReadyRomana();
        List<Permanent> soldiers = createSoldiers();

        harness.activateAbility(player1, permanentIndex(romana), null, soldiers.getFirst().getId());
        harness.passBothPriorities();

        List<Permanent> tokens = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .toList();
        assertThat(tokens).hasSize(3);
        assertThat(tokens.getLast().isTapped()).isTrue();
    }

    @Test
    @DisplayName("A token from an earlier turn is not a legal target")
    void cannotTargetTokenThatEnteredEarlier() {
        Permanent romana = addReadyRomana();
        List<Permanent> soldiers = createSoldiers();
        endTurn(player1);

        assertThatThrownBy(() -> harness.activateAbility(
                player1, permanentIndex(romana), null, soldiers.getFirst().getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can target a token controlled by an opponent")
    void canTargetOpponentsToken() {
        Permanent romana = addReadyRomana();
        Permanent token = createOpponentToken();

        harness.activateAbility(player1, permanentIndex(romana), null, token.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())).hasSize(1);
        assertThat(gd.playerBattlefields.get(player1.getId()).getLast().isTapped()).isTrue();
    }

    @Test
    @DisplayName("Cannot target a nontoken permanent that entered this turn")
    void cannotTargetNontoken() {
        Permanent romana = addReadyRomana();

        assertThatThrownBy(() -> harness.activateAbility(
                player1, permanentIndex(romana), null, romana.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(romana.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Copying a token does not copy its counters or damage")
    void copiesBaseTokenWithoutCountersOrDamage() {
        Permanent romana = addReadyRomana();
        Permanent original = createSoldiers().getFirst();
        original.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        original.setMarkedDamage(1);

        harness.activateAbility(player1, permanentIndex(romana), null, original.getId());
        assertThat(romana.isTapped()).isTrue();
        harness.passBothPriorities();

        Permanent copy = gd.playerBattlefields.get(player1.getId()).getLast();
        assertThat(copy.getId()).isNotEqualTo(original.getId());
        assertThat(copy.isTapped()).isTrue();
        assertThat(copy.getPlusOnePlusOneCounters()).isZero();
        assertThat(copy.getMarkedDamage()).isZero();
        assertThat(gqs.getEffectivePower(gd, copy)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, copy)).isEqualTo(1);
    }

    private Permanent addReadyRomana() {
        Permanent romana = harness.addToBattlefieldAndReturn(player1, new RomanaII());
        romana.setSummoningSick(false);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        return romana;
    }

    private List<Permanent> createSoldiers() {
        harness.setHand(player1, List.of(new RaiseTheAlarm()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castAndResolveInstant(player1, 0);
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .toList();
    }

    private Permanent createOpponentToken() {
        harness.setHand(player2, List.of(new RaiseTheAlarm()));
        harness.addMana(player2, ManaColor.WHITE, 2);
        harness.castAndResolveInstant(player2, 0);
        return gd.playerBattlefields.get(player2.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst()
                .orElseThrow();
    }

    private void endTurn(Player activePlayer) {
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
    }

    private int permanentIndex(Permanent permanent) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(permanent);
    }
}

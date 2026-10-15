package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CostOfBrilliance.class, GrizzlyBears.class, Island.class})
class CostOfBrillianceTest extends BaseCardTest {

    @Test
    @DisplayName("Target player draws two cards and loses 2 life")
    void targetPlayerDrawsTwoCardsAndLoses2Life() {
        int opponentHandBefore = gd.playerHands.get(player2.getId()).size();

        castAndResolveCostOfBrillianceTargeting(player2.getId());

        assertThat(gd.playerHands.get(player2.getId())).hasSize(opponentHandBefore + 2);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Puts +1/+1 counter on optional creature target")
    void putsCounterOnOptionalCreatureTarget() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new CostOfBrilliance()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveSorcery(player1, 0, List.of(player2.getId(), bear.getId()));

        assertThat(bear.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Resolves without creature target when none is chosen")
    void resolvesWithoutCreatureTarget() {
        castAndResolveCostOfBrillianceTargeting(player2.getId());

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        harness.assertInGraveyard(player1, "Cost of Brilliance");
    }

    @Test
    @DisplayName("Can target yourself")
    void canTargetSelf() {
        castAndResolveCostOfBrillianceTargeting(player1.getId());

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Does not affect non-targeted player")
    void doesNotAffectNonTargetedPlayer() {
        castAndResolveCostOfBrillianceTargeting(player2.getId());

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Cannot target a creature as the player target")
    void cannotTargetCreatureAsPlayer() {
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new CostOfBrilliance()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, bear.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a non-creature as the optional second target")
    void cannotTargetNonCreatureAsSecondTarget() {
        Permanent island = harness.addToBattlefieldAndReturn(player1, new Island());
        harness.setHand(player1, List.of(new CostOfBrilliance()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, List.of(player2.getId(), island.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can put the counter on an opponent's creature while drawing for yourself")
    void canTargetOpponentsCreatureAndSelf() {
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new CostOfBrilliance()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveSorcery(player1, 0, List.of(player1.getId(), bear.getId()));

        assertThat(bear.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        harness.assertLife(player1, 18);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Still draws and loses life when the creature target leaves the battlefield")
    void resolvesPlayerEffectsWhenCreatureTargetLeaves() {
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        int handBefore = gd.playerHands.get(player2.getId()).size();
        harness.setHand(player1, List.of(new CostOfBrilliance()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castSorcery(player1, 0, List.of(player2.getId(), bear.getId()));

        gd.playerBattlefields.get(player2.getId()).remove(bear);
        gd.playerGraveyards.get(player2.getId()).add(bear.getCard());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).hasSize(handBefore + 2);
        harness.assertLife(player2, 18);
        assertThat(bear.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.assertInGraveyard(player1, "Cost of Brilliance");
    }

    @Test
    @DisplayName("Cannot choose two creature targets")
    void cannotChooseTwoCreatureTargets() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new CostOfBrilliance()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0,
                List.of(player2.getId(), first.getId(), second.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot cast without the required player target")
    void cannotCastWithoutPlayerTarget() {
        harness.setHand(player1, List.of(new CostOfBrilliance()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, List.<UUID>of()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void castAndResolveCostOfBrillianceTargeting(UUID targetPlayerId) {
        harness.setHand(player1, List.of(new CostOfBrilliance()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        // Cost of Brilliance has two target groups (required player + optional creature); cast via the
        // multi-target path with only the player named so the optional creature group stays empty.
        harness.castAndResolveSorcery(player1, 0, List.of(targetPlayerId));
    }
}

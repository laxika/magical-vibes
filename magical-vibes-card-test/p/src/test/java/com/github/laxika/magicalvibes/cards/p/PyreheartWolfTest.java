package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrafdiggersCage;
import com.github.laxika.magicalvibes.cards.r.RussetWolves;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PyreheartWolf.class, RussetWolves.class, GrafdiggersCage.class})
class PyreheartWolfTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking with Pyreheart Wolf grants menace to all creatures you control")
    void attackGrantsMenaceToAllControlledCreatures() {
        Permanent wolf = addCreatureReady(player1, new PyreheartWolf());
        Permanent bears = addCreatureReady(player1, new RussetWolves());
        Permanent opponentCreature = addCreatureReady(player2, new RussetWolves());

        attackWithWolf();

        assertThat(wolf.hasKeyword(Keyword.MENACE)).isTrue();
        assertThat(bears.hasKeyword(Keyword.MENACE)).isTrue();
        assertThat(opponentCreature.hasKeyword(Keyword.MENACE)).isFalse();
    }

    @Test
    @DisplayName("Menace granted by Pyreheart Wolf stops a single blocker")
    void grantedMenaceStopsSingleBlocker() {
        addCreatureReady(player1, new PyreheartWolf());
        addCreatureReady(player2, new RussetWolves());

        attackWithWolf();
        harness.beginBlockerDeclarationInput();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be blocked except by two or more creatures");
    }

    @Test
    @DisplayName("Menace granted by Pyreheart Wolf allows two blockers")
    void grantedMenaceAllowsTwoBlockers() {
        addCreatureReady(player1, new PyreheartWolf());
        addCreatureReady(player2, new RussetWolves());
        addCreatureReady(player2, new RussetWolves());

        attackWithWolf();
        harness.beginBlockerDeclarationInput();

        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 0)
        ));

        assertThat(gd.playerBattlefields.get(player2.getId()).get(0).isBlocking()).isTrue();
        assertThat(gd.playerBattlefields.get(player2.getId()).get(1).isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Pyreheart Wolf's granted menace wears off at end of turn")
    void grantedMenaceWearsOffAtEndOfTurn() {
        addCreatureReady(player1, new PyreheartWolf());
        Permanent bears = addCreatureReady(player1, new RussetWolves());

        attackWithWolf();

        assertThat(bears.hasKeyword(Keyword.MENACE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(bears.hasKeyword(Keyword.MENACE)).isFalse();
    }

    @Test
    @DisplayName("Pyreheart Wolf returns with a +1/+1 counter when it dies without one")
    void undyingReturnsWithCounter() {
        Permanent wolf = addCreatureReady(player1, new PyreheartWolf());

        harness.inMutationScope(() -> harness.getPermanentRemovalService().tryDestroyPermanent(gd, wolf));
        harness.passBothPriorities();

        harness.assertNotInGraveyard(player1, "Pyreheart Wolf");
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(perm -> perm.getCard().getName().equals("Pyreheart Wolf"))
                .singleElement()
                .satisfies(returnedWolf ->
                        assertThat(returnedWolf.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1));
    }

    @Test
    @DisplayName("Pyreheart Wolf does not return from undying if it had a +1/+1 counter")
    void undyingDoesNotReturnWithCounter() {
        Permanent wolf = addCreatureReady(player1, new PyreheartWolf());
        wolf.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        harness.inMutationScope(() -> harness.getPermanentRemovalService().tryDestroyPermanent(gd, wolf));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Pyreheart Wolf");
        harness.assertInGraveyard(player1, "Pyreheart Wolf");
    }

    @Test
    @DisplayName("The attack trigger grants menace only when it resolves")
    void menaceIsGrantedAtResolution() {
        Permanent wolf = addCreatureReady(player1, new PyreheartWolf());
        Permanent support = addCreatureReady(player1, new RussetWolves());

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> declareAttackers(List.of(0)));

        assertThat(wolf.hasKeyword(Keyword.MENACE)).isFalse();
        assertThat(support.hasKeyword(Keyword.MENACE)).isFalse();

        Permanent arrivingBeforeResolution = addCreatureReady(player1, new RussetWolves());
        harness.passBothPriorities();

        assertThat(wolf.hasKeyword(Keyword.MENACE)).isTrue();
        assertThat(support.hasKeyword(Keyword.MENACE)).isTrue();
        assertThat(arrivingBeforeResolution.hasKeyword(Keyword.MENACE)).isTrue();

        Permanent arrivingAfterResolution = addCreatureReady(player1, new RussetWolves());
        assertThat(arrivingAfterResolution.hasKeyword(Keyword.MENACE)).isFalse();
    }

    @Test
    @DisplayName("The attack trigger still resolves after Pyreheart Wolf dies")
    void attackTriggerSurvivesSourceDeath() {
        Permanent wolf = addCreatureReady(player1, new PyreheartWolf());
        Permanent support = addCreatureReady(player1, new RussetWolves());
        wolf.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> declareAttackers(List.of(0)));
        harness.inMutationScope(() -> harness.getPermanentRemovalService().tryDestroyPermanent(gd, wolf));
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Pyreheart Wolf");
        assertThat(support.hasKeyword(Keyword.MENACE)).isTrue();
    }

    @Test
    @DisplayName("Grafdigger's Cage prevents Pyreheart Wolf from returning through undying")
    void cagePreventsUndyingReturn() {
        harness.addToBattlefield(player2, new GrafdiggersCage());
        Permanent wolf = addCreatureReady(player1, new PyreheartWolf());
        harness.inMutationScope(() -> harness.getPermanentRemovalService().tryDestroyPermanent(gd, wolf));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Pyreheart Wolf");
        harness.assertInGraveyard(player1, "Pyreheart Wolf");
    }

    private void attackWithWolf() {
        declareAttackers(List.of(0));
        harness.passBothPriorities();
    }
}

package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.q.QuandrixCampus;
import com.github.laxika.magicalvibes.cards.s.SolRing;
import com.github.laxika.magicalvibes.cards.s.Solemnity;
import com.github.laxika.magicalvibes.cards.z.ZimoneQuandrixProdigy;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({NexusMentality.class, ZimoneQuandrixProdigy.class, QuandrixCampus.class, SolRing.class, Solemnity.class})
class NexusMentalityTest extends BaseCardTest {

    @Test
    void firstModeMovesAllCountersBetweenNonlandPermanents() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new SolRing());
        Permanent destination = harness.addToBattlefieldAndReturn(player1, new SolRing());
        source.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        source.setCounterCount(CounterType.CHARGE, 1);
        destination.setCounterCount(CounterType.LOYALTY, 3);

        cast(new int[]{0}, List.of(source.getId(), destination.getId()));

        assertThat(source.getCounters()).isEmpty();
        assertThat(destination.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(destination.getCounterCount(CounterType.CHARGE)).isEqualTo(1);
        assertThat(destination.getCounterCount(CounterType.LOYALTY)).isEqualTo(3);
    }

    @Test
    void secondModeRemovesAllCountersAndDrawsThatManyCards() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new SolRing());
        target.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        target.setCounterCount(CounterType.CHARGE, 1);
        harness.setLibrary(player1, List.of(new QuandrixCampus(), new SolRing(), new QuandrixCampus()));

        cast(new int[]{1}, List.of(target.getId()));

        assertThat(target.getCounters()).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
    }

    @Test
    void commanderAllowsBothModes() {
        ZimoneQuandrixProdigy commander = new ZimoneQuandrixProdigy();
        gd.makeCommander(player1.getId(), commander);
        harness.addToBattlefield(player1, commander);
        Permanent source = harness.addToBattlefieldAndReturn(player1, new SolRing());
        Permanent destination = harness.addToBattlefieldAndReturn(player1, new SolRing());
        Permanent drawTarget = harness.addToBattlefieldAndReturn(player1, new SolRing());
        source.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        drawTarget.setCounterCount(CounterType.CHARGE, 1);
        harness.setLibrary(player1, List.of(new QuandrixCampus()));

        cast(new int[]{0, 1}, List.of(source.getId(), destination.getId(), drawTarget.getId()));

        assertThat(source.getCounters()).isEmpty();
        assertThat(destination.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(drawTarget.getCounters()).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    void bothModesRequireControllingTheRegisteredCommander() {
        ZimoneQuandrixProdigy commander = new ZimoneQuandrixProdigy();
        gd.makeCommander(player1.getId(), commander);
        gd.playerCommandZones.get(player1.getId()).add(commander);
        Permanent source = harness.addToBattlefieldAndReturn(player1, new SolRing());
        Permanent destination = harness.addToBattlefieldAndReturn(player1, new SolRing());
        Permanent drawTarget = harness.addToBattlefieldAndReturn(player1, new SolRing());
        harness.setHand(player1, List.of(new NexusMentality()));
        addMana();

        assertThatThrownBy(() -> harness.castModalInstantWithModes(
                player1, 0, 1, 2, new int[]{0, 1},
                List.of(source.getId(), destination.getId(), drawTarget.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void modesCannotTargetLands() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new SolRing());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new QuandrixCampus());
        harness.setHand(player1, List.of(new NexusMentality()));
        addMana();

        assertThatThrownBy(() -> harness.castModalInstantWithModes(
                player1, 0, 1, 2, new int[]{0}, List.of(creature.getId(), land.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void bothModesCanMoveCountersOntoThePermanentThatThenDraws() {
        ZimoneQuandrixProdigy commander = new ZimoneQuandrixProdigy();
        gd.makeCommander(player1.getId(), commander);
        harness.addToBattlefield(player1, commander);
        Permanent source = harness.addToBattlefieldAndReturn(player1, new SolRing());
        Permanent destination = harness.addToBattlefieldAndReturn(player1, new SolRing());
        source.setCounterCount(CounterType.CHARGE, 2);
        destination.setCounterCount(CounterType.CHARGE, 1);
        harness.setLibrary(player1, List.of(new QuandrixCampus(), new SolRing(), new QuandrixCampus()));

        cast(new int[]{0, 1}, List.of(source.getId(), destination.getId(), destination.getId()));

        assertThat(source.getCounters()).isEmpty();
        assertThat(destination.getCounters()).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
    }

    @Test
    void countersStayOnSourceWhenDestinationCannotReceiveCounters() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new SolRing());
        Permanent destination = harness.addToBattlefieldAndReturn(player1, new SolRing());
        source.setCounterCount(CounterType.CHARGE, 2);
        harness.addToBattlefield(player2, new Solemnity());

        cast(new int[]{0}, List.of(source.getId(), destination.getId()));

        assertThat(source.getCounterCount(CounterType.CHARGE)).isEqualTo(2);
        assertThat(destination.getCounters()).isEmpty();
    }

    @Test
    void secondModeWithNoCountersDrawsNoCards() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new SolRing());
        harness.setLibrary(player1, List.of(new QuandrixCampus()));

        cast(new int[]{1}, List.of(target.getId()));

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    void firstModeRequiresTwoDifferentPermanents() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new SolRing());
        harness.setHand(player1, List.of(new NexusMentality()));
        addMana();

        assertThatThrownBy(() -> harness.castModalInstantWithModes(
                player1, 0, 1, 2, new int[]{0}, List.of(target.getId(), target.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void secondModeCannotTargetOpponentsPermanent() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SolRing());
        harness.setHand(player1, List.of(new NexusMentality()));
        addMana();

        assertThatThrownBy(() -> harness.castModalInstantWithModes(
                player1, 0, 1, 2, new int[]{1}, List.of(target.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void controllingOpponentsCommanderAllowsBothModes() {
        ZimoneQuandrixProdigy commander = new ZimoneQuandrixProdigy();
        gd.makeCommander(player2.getId(), commander);
        harness.addToBattlefield(player1, commander);
        Permanent source = harness.addToBattlefieldAndReturn(player1, new SolRing());
        Permanent destination = harness.addToBattlefieldAndReturn(player1, new SolRing());
        Permanent drawTarget = harness.addToBattlefieldAndReturn(player1, new SolRing());
        source.setCounterCount(CounterType.CHARGE, 1);
        drawTarget.setCounterCount(CounterType.CHARGE, 1);
        harness.setLibrary(player1, List.of(new QuandrixCampus()));

        cast(new int[]{0, 1}, List.of(source.getId(), destination.getId(), drawTarget.getId()));

        assertThat(source.getCounters()).isEmpty();
        assertThat(destination.getCounterCount(CounterType.CHARGE)).isEqualTo(1);
        assertThat(drawTarget.getCounters()).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    void losingCommanderAfterCastingDoesNotUndoBothModes() {
        ZimoneQuandrixProdigy commander = new ZimoneQuandrixProdigy();
        gd.makeCommander(player1.getId(), commander);
        Permanent commanderPermanent = harness.addToBattlefieldAndReturn(player1, commander);
        Permanent source = harness.addToBattlefieldAndReturn(player1, new SolRing());
        Permanent destination = harness.addToBattlefieldAndReturn(player1, new SolRing());
        Permanent drawTarget = harness.addToBattlefieldAndReturn(player1, new SolRing());
        source.setCounterCount(CounterType.CHARGE, 1);
        drawTarget.setCounterCount(CounterType.CHARGE, 1);
        harness.setLibrary(player1, List.of(new QuandrixCampus()));
        harness.setHand(player1, List.of(new NexusMentality()));
        addMana();
        harness.castModalInstantWithModes(player1, 0, 1, 2, new int[]{0, 1},
                List.of(source.getId(), destination.getId(), drawTarget.getId()));
        gd.playerBattlefields.get(player1.getId()).remove(commanderPermanent);

        harness.passBothPriorities();

        assertThat(source.getCounters()).isEmpty();
        assertThat(destination.getCounterCount(CounterType.CHARGE)).isEqualTo(1);
        assertThat(drawTarget.getCounters()).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    void missingMoveDestinationDoesNotRedirectCountersOntoSecondModeTarget() {
        ZimoneQuandrixProdigy commander = new ZimoneQuandrixProdigy();
        gd.makeCommander(player1.getId(), commander);
        harness.addToBattlefield(player1, commander);
        Permanent source = harness.addToBattlefieldAndReturn(player1, new SolRing());
        Permanent destination = harness.addToBattlefieldAndReturn(player1, new SolRing());
        Permanent drawTarget = harness.addToBattlefieldAndReturn(player1, new SolRing());
        source.setCounterCount(CounterType.CHARGE, 2);
        drawTarget.setCounterCount(CounterType.CHARGE, 1);
        harness.setLibrary(player1, List.of(new QuandrixCampus(), new SolRing(), new QuandrixCampus()));
        harness.setHand(player1, List.of(new NexusMentality()));
        addMana();
        harness.castModalInstantWithModes(player1, 0, 1, 2, new int[]{0, 1},
                List.of(source.getId(), destination.getId(), drawTarget.getId()));
        gd.playerBattlefields.get(player1.getId()).remove(destination);

        harness.passBothPriorities();

        assertThat(source.getCounterCount(CounterType.CHARGE)).isEqualTo(2);
        assertThat(drawTarget.getCounters()).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(2);
    }

    private void cast(int[] modes, List<java.util.UUID> targetIds) {
        harness.setHand(player1, List.of(new NexusMentality()));
        addMana();
        harness.castModalInstantWithModes(player1, 0, 1, 2, modes, targetIds);
        harness.passBothPriorities();
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.BLUE, 1);
    }
}

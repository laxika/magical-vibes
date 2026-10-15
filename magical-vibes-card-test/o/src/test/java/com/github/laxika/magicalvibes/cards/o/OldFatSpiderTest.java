package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.p.ProdigalPyromancer;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
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

@CardUsed({OldFatSpider.class, AirElemental.class, GrizzlyBears.class, HillGiant.class,
        ProdigalPyromancer.class, Shock.class})
class OldFatSpiderTest extends BaseCardTest {

    @Test
    @DisplayName("Can't be blocked by a creature with power 2 or less")
    void cannotBeBlockedByPowerTwoOrLess() {
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        Permanent spider = addCreatureReady(player1, new OldFatSpider());
        spider.setAttacking(true);

        prepareDeclareBlockers();

        int blockerIdx = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIdx = gd.playerBattlefields.get(player1.getId()).indexOf(spider);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(blockerIdx, attackerIdx))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can be blocked by a creature with power greater than 2")
    void canBeBlockedByPowerGreaterThanTwo() {
        Permanent blocker = addCreatureReady(player2, new HillGiant());
        Permanent spider = addCreatureReady(player1, new OldFatSpider());
        spider.setAttacking(true);

        prepareDeclareBlockers();

        int blockerIdx = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIdx = gd.playerBattlefields.get(player1.getId()).indexOf(spider);

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIdx, attackerIdx)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Draws when an opponent's spell targets it")
    void drawsOnOpponentSpellTargetingIt() {
        Permanent spider = harness.addToBattlefieldAndReturn(player1, new OldFatSpider());
        int handBefore = gd.playerHands.get(player1.getId()).size();

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castAndResolveInstant(player2, 0, spider.getId());

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
    }

    @Test
    @DisplayName("Draws when an opponent's ability targets it")
    void drawsOnOpponentAbilityTargetingIt() {
        Permanent spider = harness.addToBattlefieldAndReturn(player1, new OldFatSpider());
        Permanent pyromancer = harness.addToBattlefieldAndReturn(player2, new ProdigalPyromancer());
        pyromancer.setSummoningSick(false);

        int handBefore = gd.playerHands.get(player1.getId()).size();
        harness.activateAbility(player2, 0, null, spider.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
    }

    @Test
    @DisplayName("Does not draw when its controller's spell targets it")
    void doesNotDrawOnOwnSpellTargetingIt() {
        Permanent spider = harness.addToBattlefieldAndReturn(player1, new OldFatSpider());

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, spider.getId());

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("A power-two creature with a +1/+1 counter can block")
    void boostedSmallCreatureCanBlock() {
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        blocker.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        Permanent spider = addCreatureReady(player1, new OldFatSpider());
        spider.setAttacking(true);
        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("A power-three creature with a -1/-1 counter cannot block")
    void reducedLargeCreatureCannotBlock() {
        Permanent blocker = addCreatureReady(player2, new HillGiant());
        blocker.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 1);
        Permanent spider = addCreatureReady(player1, new OldFatSpider());
        spider.setAttacking(true);
        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Does not draw when its controller's ability targets it")
    void doesNotDrawOnOwnAbilityTargetingIt() {
        Permanent spider = harness.addToBattlefieldAndReturn(player1, new OldFatSpider());
        addCreatureReady(player1, new ProdigalPyromancer());
        int handBefore = gd.playerHands.get(player1.getId()).size();

        harness.activateAbility(player1, 1, null, spider.getId());

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);
    }

    @Test
    @DisplayName("The draw trigger resolves before the opponent's targeting spell")
    void drawsBeforeTargetingSpellResolves() {
        Permanent spider = harness.addToBattlefieldAndReturn(player1, new OldFatSpider());
        GrizzlyBears drawnCard = new GrizzlyBears();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(drawnCard));
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, spider.getId());

        assertThat(gd.stack).hasSize(2);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.passBothPriorities();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
    }

    @Test
    @DisplayName("Reach allows Old Fat Spider to block a flying creature")
    void canBlockFlyingCreature() {
        Permanent attacker = addCreatureReady(player1, new AirElemental());
        attacker.setAttacking(true);
        Permanent spider = addCreatureReady(player2, new OldFatSpider());
        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(spider.isBlocking()).isTrue();
    }
}

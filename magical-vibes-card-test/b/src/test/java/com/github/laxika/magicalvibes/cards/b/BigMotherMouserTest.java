package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.d.DoublingSeason;
import com.github.laxika.magicalvibes.cards.w.WrathOfGod;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BigMotherMouser.class, WrathOfGod.class, DoublingSeason.class})
class BigMotherMouserTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with two +1/+1 counters")
    void entersWithTwoCounters() {
        harness.setHand(player1, List.of(new BigMotherMouser()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent mouser = findPermanent(player1, "Big Mother Mouser");
        assertThat(mouser.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Doubles its +1/+1 counters when it attacks")
    void doublesCountersOnAttack() {
        Permanent mouser = addMouserReady(player1, 2);

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(mouser.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
    }

    @Test
    @DisplayName("Creates one artifact Robot token per +1/+1 counter when it dies")
    void deathCreatesRobotTokensPerCounter() {
        addMouserReady(player1, 3);

        harness.forceActivePlayer(player2);
        harness.forceStep(com.github.laxika.magicalvibes.model.TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(new WrathOfGod()));
        harness.addMana(player2, ManaColor.WHITE, 4);
        harness.castSorcery(player2, 0);
        resolveAllTriggers();

        List<Permanent> robots = findPermanents(player1, "Robot");
        assertThat(robots).hasSize(3);
        assertThat(robots).allSatisfy(robot -> {
            assertThat(robot.getCard().getAdditionalTypes()).contains(CardType.ARTIFACT);
            assertThat(gqs.isCreature(gd, robot)).isTrue();
            assertThat(gqs.getEffectivePower(gd, robot)).isEqualTo(1);
            assertThat(gqs.getEffectiveToughness(gd, robot)).isEqualTo(1);
        });
    }

    @Test
    @DisplayName("Attack counter placement applies Doubling Season to the counters being added")
    void attackAppliesCounterReplacement() {
        harness.addToBattlefield(player1, new DoublingSeason());
        Permanent mouser = addMouserReady(player1, 2);

        declareAttackers(List.of(1));
        resolveAllTriggers();

        assertThat(mouser.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(6);
    }

    @Test
    @DisplayName("Entry counters are placed before state-based actions and apply Doubling Season")
    void entersWithReplacedCounters() {
        harness.addToBattlefield(player1, new DoublingSeason());
        harness.setHand(player1, List.of(new BigMotherMouser()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        Permanent mouser = findPermanent(player1, "Big Mother Mouser");
        assertThat(mouser.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
        harness.assertNotInGraveyard(player1, "Big Mother Mouser");
    }

    @Test
    @DisplayName("Only the attacking Mouser doubles counters")
    void attackDoesNotDoubleOtherMousers() {
        Permanent attacker = addMouserReady(player1, 3);
        Permanent nonattacker = addMouserReady(player1, 2);

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(attacker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(6);
        assertThat(nonattacker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Simultaneous deaths use each Mouser's own counters and controller")
    void simultaneousDeathsKeepCounterCountsAndControllersSeparate() {
        Permanent first = addMouserReady(player1, 2);
        first.setCounterCount(CounterType.CHARGE, 5);
        addMouserReady(player1, 3);
        addMouserReady(player2, 4);
        harness.setHand(player1, List.of(new WrathOfGod()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castSorcery(player1, 0);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Big Mother Mouser")).isEmpty();
        assertThat(findPermanents(player2, "Big Mother Mouser")).isEmpty();
        assertThat(findPermanents(player1, "Robot")).hasSize(5);
        assertThat(findPermanents(player2, "Robot")).hasSize(4);
    }

    @Test
    @DisplayName("Dying with no +1/+1 counters creates no Robots")
    void deathWithNoPlusOneCountersCreatesNoTokens() {
        Permanent mouser = addMouserReady(player1, 0);
        mouser.setCounterCount(CounterType.CHARGE, 3);

        harness.runStateBasedActions();
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Big Mother Mouser");
        assertThat(findPermanents(player1, "Robot")).isEmpty();
    }

    private Permanent addMouserReady(Player player, int counters) {
        Permanent mouser = addCreatureReady(player, new BigMotherMouser());
        mouser.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, counters);
        return mouser;
    }
}

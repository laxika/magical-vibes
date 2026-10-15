package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.k.KathariScreecher;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CourtArchers.class, KathariScreecher.class})
class CourtArchersTest extends BaseCardTest {

    @Test
    @DisplayName("Exalted — another creature attacking alone gets +1/+1")
    void allyAttackingAloneBoosted() {
        addCreatureReady(player1, new CourtArchers());
        Permanent screecher = addCreatureReady(player1, new KathariScreecher());

        declareAttackers(player1, List.of(1)); // Kathari Screecher attacks alone
        harness.passBothPriorities(); // resolve exalted trigger

        assertThat(gqs.getEffectivePower(gd, screecher)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, screecher)).isEqualTo(3);
    }

    @Test
    @DisplayName("Exalted — the Archers attacking alone boosts itself")
    void selfAttackingAloneBoosted() {
        Permanent archers = addCreatureReady(player1, new CourtArchers());

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities(); // resolve exalted trigger

        assertThat(gqs.getEffectivePower(gd, archers)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, archers)).isEqualTo(4);
    }

    @Test
    @DisplayName("Exalted boost wears off at end of turn")
    void boostWearsOff() {
        addCreatureReady(player1, new CourtArchers());
        Permanent screecher = addCreatureReady(player1, new KathariScreecher());

        declareAttackers(player1, List.of(1));
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, screecher)).isEqualTo(3);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, screecher)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, screecher)).isEqualTo(2);
    }

    @Test
    @DisplayName("Exalted does not trigger when attacking with more than one creature")
    void noTriggerWhenNotAlone() {
        addCreatureReady(player1, new CourtArchers());
        Permanent screecher = addCreatureReady(player1, new KathariScreecher());

        declareAttackers(player1, List.of(0, 1)); // both attack — not alone

        assertThat(gd.stack).noneMatch(e -> e.getCard().getName().equals("Court Archers"));
        assertThat(gqs.getEffectivePower(gd, screecher)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, screecher)).isEqualTo(2);
    }

    @Test
    @DisplayName("Each Court Archers contributes a separate exalted bonus")
    void multipleExaltedAbilitiesStack() {
        Permanent attacker = addCreatureReady(player1, new CourtArchers());
        addCreatureReady(player1, new CourtArchers());

        declareAttackers(player1, List.of(0));
        assertThat(gd.stack).hasSize(2);
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, attacker)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, attacker)).isEqualTo(5);
    }

    @Test
    @DisplayName("Exalted does not boost an opponent's lone attacker")
    void opponentAttackingAloneIsNotBoosted() {
        addCreatureReady(player1, new CourtArchers());
        Permanent attacker = addCreatureReady(player2, new KathariScreecher());

        declareAttackers(player2, List.of(0));
        resolveAllTriggers();

        assertThat(gd.stack).isEmpty();
        assertThat(gqs.getEffectivePower(gd, attacker)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, attacker)).isEqualTo(2);
    }

    @Test
    @DisplayName("Exalted still boosts a lone attacker removed from combat before resolution")
    void removingAttackerFromCombatDoesNotPreventBoost() {
        Permanent attacker = addCreatureReady(player1, new CourtArchers());

        declareAttackers(player1, List.of(0));
        assertThat(gd.stack).hasSize(1);
        attacker.setAttacking(false);
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, attacker)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, attacker)).isEqualTo(4);
    }

    @Test
    @DisplayName("Reach allows Court Archers to block a flying creature")
    void canBlockFlyingCreature() {
        Permanent archers = addCreatureReady(player2, new CourtArchers());
        addCreatureReady(player1, new KathariScreecher());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(archers.isBlocking()).isTrue();
    }
}

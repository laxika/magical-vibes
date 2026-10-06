package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.e.Eviscerate;
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

@CardUsed({RendclawTrow.class, Eviscerate.class})
class RendclawTrowTest extends BaseCardTest {

    @Test
    @DisplayName("Persist returns Rendclaw Trow with a -1/-1 counter when it dies with no -1/-1 counters")
    void persistReturnsWithMinusCounter() {
        harness.addToBattlefield(player1, new RendclawTrow());
        harness.setHand(player1, List.of(new Eviscerate()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castSorcery(player1, 0, 0, harness.getPermanentId(player1, "Rendclaw Trow"));
        resolveAllTriggers();

        Permanent trow = findPermanent(player1, "Rendclaw Trow");
        assertThat(trow).isNotNull();
        assertThat(trow.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
        assertThat(trow.getEffectivePower()).isEqualTo(1);
        assertThat(trow.getEffectiveToughness()).isEqualTo(1);
    }

    @Test
    @DisplayName("Persist does not return Rendclaw Trow when it died with a -1/-1 counter")
    void persistDoesNotReturnWithExistingMinusCounter() {
        Permanent trow = harness.addToBattlefieldAndReturn(player1, new RendclawTrow());
        trow.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 1);
        harness.setHand(player1, List.of(new Eviscerate()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castSorcery(player1, 0, 0, trow.getId());
        resolveAllTriggers();

        assertThat(gd.stack).isEmpty();
        harness.assertNotOnBattlefield(player1, "Rendclaw Trow");
        harness.assertInGraveyard(player1, "Rendclaw Trow");
    }

    @Test
    @DisplayName("Wither damage leaves counters rather than marked damage on a surviving blocker")
    void witherLeavesCountersOnSurvivingBlocker() {
        Permanent attacker = addCreatureReady(player1, new RendclawTrow());
        attacker.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 1);
        attacker.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new RendclawTrow());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passUntil(TurnStep.POSTCOMBAT_MAIN);

        assertThat(findPermanent(player2, "Rendclaw Trow").getId()).isEqualTo(blocker.getId());
        assertThat(blocker.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
        assertThat(blocker.getMarkedDamage()).isZero();
        harness.assertInGraveyard(player1, "Rendclaw Trow");
        harness.assertNotOnBattlefield(player1, "Rendclaw Trow");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Lethal wither damage prevents both creatures from returning through persist")
    void lethalWitherDamagePreventsPersist() {
        Permanent attacker = addCreatureReady(player1, new RendclawTrow());
        attacker.setAttacking(true);
        addCreatureReady(player2, new RendclawTrow());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passUntil(TurnStep.POSTCOMBAT_MAIN);

        harness.assertNotOnBattlefield(player1, "Rendclaw Trow");
        harness.assertNotOnBattlefield(player2, "Rendclaw Trow");
        harness.assertInGraveyard(player1, "Rendclaw Trow");
        harness.assertInGraveyard(player2, "Rendclaw Trow");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Wither damage to a player causes normal life loss")
    void witherDealsNormalDamageToPlayer() {
        Permanent attacker = addCreatureReady(player1, new RendclawTrow());
        attacker.setAttacking(true);
        harness.setLife(player2, 20);

        resolveCombat();

        harness.assertLife(player2, 18);
    }
}

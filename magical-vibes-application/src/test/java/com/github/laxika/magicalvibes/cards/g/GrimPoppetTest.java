package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GrimPoppetTest extends BaseCardTest {

    // ===== ETB: enters with three -1/-1 counters =====

    @Test
    @DisplayName("Enters the battlefield with three -1/-1 counters (4/4 becomes 1/1)")
    void entersWithThreeMinusCounters() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new GrimPoppet()));
        harness.addMana(player1, ManaColor.COLORLESS, 7);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB effect

        Permanent poppet = findPoppet(player1);

        assertThat(poppet.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(3);
        assertThat(poppet.getEffectivePower()).isEqualTo(1);
        assertThat(poppet.getEffectiveToughness()).isEqualTo(1);
    }

    // ===== Activated ability =====

    @Test
    @DisplayName("Ability removes a -1/-1 counter from itself and puts one on target creature")
    void abilityMovesCounterToTarget() {
        Permanent poppet = addReadyPoppet(player1);
        harness.addToBattlefield(player2, new com.github.laxika.magicalvibes.cards.g.GrizzlyBears());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.activateAbility(player1, 0, null, bearsId);
        harness.passBothPriorities();

        // Cost: removed one -1/-1 counter from Grim Poppet (3 -> 2)
        assertThat(poppet.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(2);

        // Effect: target gained a -1/-1 counter
        Permanent bears = harness.getGameQueryService().findPermanentById(gd, bearsId);
        assertThat(bears.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Two -1/-1 counters kill a 2/2 target creature")
    void twoCountersKillTarget() {
        Permanent poppet = addReadyPoppet(player1);
        harness.addToBattlefield(player2, new com.github.laxika.magicalvibes.cards.g.GrizzlyBears());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.activateAbility(player1, 0, null, bearsId);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, bearsId);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    // ===== Cannot activate without counters =====

    @Test
    @DisplayName("Cannot activate ability when no -1/-1 counters remain")
    void cannotActivateWithoutCounters() {
        Permanent poppet = addReadyPoppet(player1);
        poppet.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 0);
        harness.addToBattlefield(player2, new com.github.laxika.magicalvibes.cards.g.GrizzlyBears());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, bearsId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough counters");
    }

    // ===== "another" target restriction =====

    @Test
    @DisplayName("Cannot target itself (another target creature)")
    void cannotTargetSelf() {
        Permanent poppet = addReadyPoppet(player1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, poppet.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    // ===== Helpers =====

    private Permanent addReadyPoppet(Player player) {
        GrimPoppet card = new GrimPoppet();
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        perm.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 3);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Permanent findPoppet(Player player) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Grim Poppet"))
                .findFirst().orElseThrow();
    }
}

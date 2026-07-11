package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FloodchaserTest extends BaseCardTest {

    // ===== Enters with six +1/+1 counters =====

    @Test
    @DisplayName("Enters the battlefield with six +1/+1 counters")
    void entersWithSixCounters() {
        harness.setHand(player1, List.of(new Floodchaser()));
        harness.addMana(player1, ManaColor.BLUE, 6);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent floodchaser = floodchaser(player1);
        assertThat(floodchaser.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(6);
    }

    // ===== Activated ability: target land becomes an Island =====

    @Test
    @DisplayName("Activating removes a +1/+1 counter and target land becomes an Island (type-replacing)")
    void activateMakesLandIsland() {
        Permanent floodchaser = new Permanent(new Floodchaser());
        floodchaser.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 6);
        gd.playerBattlefields.get(player1.getId()).add(floodchaser);
        harness.addToBattlefield(player1, new Forest());
        harness.addMana(player1, ManaColor.BLUE, 1);
        UUID forestId = harness.getPermanentId(player1, "Forest");

        harness.activateAbility(player1, 0, null, forestId);
        harness.passBothPriorities();

        assertThat(floodchaser.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(5);

        Permanent forest = gqs.findPermanentById(gd, forestId);
        GameQueryService.StaticBonus bonus = gqs.computeStaticBonus(gd, forest);
        assertThat(bonus.landSubtypeOverriding()).isTrue();
        assertThat(bonus.grantedSubtypes()).containsExactly(CardSubtype.ISLAND);
    }

    @Test
    @DisplayName("Cannot activate with no +1/+1 counters to remove")
    void cannotActivateWithoutCounters() {
        Permanent floodchaser = new Permanent(new Floodchaser());
        floodchaser.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 0);
        gd.playerBattlefields.get(player1.getId()).add(floodchaser);
        harness.addToBattlefield(player1, new Forest());
        harness.addMana(player1, ManaColor.BLUE, 1);
        UUID forestId = harness.getPermanentId(player1, "Forest");

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, forestId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a non-land permanent")
    void cannotTargetNonLand() {
        Permanent floodchaser = new Permanent(new Floodchaser());
        floodchaser.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 6);
        gd.playerBattlefields.get(player1.getId()).add(floodchaser);
        harness.addToBattlefield(player1, new Forest()); // valid target so activatable
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.BLUE, 1);
        UUID bearsId = harness.getPermanentId(player1, "Grizzly Bears");

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, bearsId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a land");
    }

    // ===== Attack restriction =====

    @Test
    @DisplayName("Can attack when defending player controls an Island")
    void canAttackWhenDefenderControlsIsland() {
        harness.setLife(player2, 20);
        harness.addToBattlefield(player2, new Island());

        Permanent floodchaser = readyAttacker();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();

        gs.declareAttackers(gd, player1, List.of(0));

        // 0/0 base + 6 counters = 6 power
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(14);
    }

    @Test
    @DisplayName("Cannot attack when defending player controls no Island")
    void cannotAttackWithoutIsland() {
        readyAttacker();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();

        assertThatThrownBy(() -> gs.declareAttackers(gd, player1, List.of(0)))
                .isInstanceOf(IllegalStateException.class);
    }

    // ===== Helpers =====

    private Permanent floodchaser(com.github.laxika.magicalvibes.model.Player player) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Floodchaser"))
                .findFirst().orElseThrow();
    }

    private Permanent readyAttacker() {
        Permanent floodchaser = new Permanent(new Floodchaser());
        floodchaser.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 6);
        floodchaser.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(floodchaser);
        return floodchaser;
    }
}

package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.i.IndomitableAncients;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.m.MurmuringBosk;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Floodchaser.class, Island.class, MurmuringBosk.class, IndomitableAncients.class})
class FloodchaserTest extends BaseCardTest {

    // ===== Enters with six +1/+1 counters =====

    @Test
    @DisplayName("Enters the battlefield with six +1/+1 counters")
    void entersWithSixCounters() {
        harness.setHand(player1, List.of(new Floodchaser()));
        harness.addMana(player1, ManaColor.BLUE, 6);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent floodchaser = findPermanent(player1, "Floodchaser");
        assertThat(floodchaser.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(6);
    }

    // ===== Activated ability: target land becomes an Island =====

    @Test
    @DisplayName("Activating removes a +1/+1 counter and target land becomes an Island (type-replacing)")
    void activateMakesLandIsland() {
        Permanent floodchaser = addFloodchaserWithCounters(6);
        Permanent land = harness.addToBattlefieldAndReturn(player2, new MurmuringBosk());
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, null, land.getId());
        harness.passBothPriorities();

        assertThat(floodchaser.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(5);

        GameQueryService.StaticBonus bonus = gqs.computeStaticBonus(gd, land);
        assertThat(bonus.landSubtypeOverriding()).isTrue();
        assertThat(bonus.grantedSubtypes()).containsExactly(CardSubtype.ISLAND);
        assertThat(gqs.effectiveBasicLandTypes(gd, land)).containsExactly(CardSubtype.ISLAND);
    }

    @Test
    @DisplayName("The Island type wears off at the end of the turn")
    void islandTypeWearsOffAtEndOfTurn() {
        Permanent floodchaser = addFloodchaserWithCounters(6);
        Permanent land = harness.addToBattlefieldAndReturn(player1, new MurmuringBosk());
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, null, land.getId());
        harness.passBothPriorities();

        assertThat(gqs.effectiveBasicLandTypes(gd, land)).containsExactly(CardSubtype.ISLAND);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.effectiveBasicLandTypes(gd, land)).containsExactly(CardSubtype.FOREST);
        assertThat(floodchaser.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(5);
    }

    @Test
    @DisplayName("Cannot activate with no +1/+1 counters to remove")
    void cannotActivateWithoutCounters() {
        addFloodchaserWithCounters(0);
        Permanent land = harness.addToBattlefieldAndReturn(player1, new MurmuringBosk());
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, land.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot activate without a blue mana")
    void cannotActivateWithoutBlueMana() {
        addFloodchaserWithCounters(6);
        Permanent land = harness.addToBattlefieldAndReturn(player1, new MurmuringBosk());
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, land.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a non-land permanent")
    void cannotTargetNonLand() {
        addFloodchaserWithCounters(6);
        harness.addToBattlefield(player1, new MurmuringBosk()); // valid target so activatable
        Permanent nonLand = harness.addToBattlefieldAndReturn(player1, new IndomitableAncients());
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, nonLand.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a land");
    }

    // ===== Attack restriction =====

    @Test
    @DisplayName("Can attack when defending player controls an Island")
    void canAttackWhenDefenderControlsIsland() {
        harness.setLife(player2, 20);
        harness.addToBattlefield(player2, new Island());

        readyAttacker();

        declareAttackers(List.of(0));

        // 0/0 base + 6 counters = 6 power
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(14);
    }

    @Test
    @DisplayName("Cannot attack when defending player controls no Island")
    void cannotAttackWithoutIsland() {
        readyAttacker();

        assertThatThrownBy(() -> declareAttackers(List.of(0)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot attack when only the attacking player controls an Island")
    void cannotAttackWhenOnlyAttackingPlayerControlsIsland() {
        readyAttacker();
        harness.addToBattlefield(player1, new Island());

        assertThatThrownBy(() -> declareAttackers(List.of(0)))
                .isInstanceOf(IllegalStateException.class);
    }

    // ===== Helpers =====

    private Permanent addFloodchaserWithCounters(int counterCount) {
        Permanent floodchaser = harness.addToBattlefieldAndReturn(player1, new Floodchaser());
        floodchaser.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, counterCount);
        return floodchaser;
    }

    private void readyAttacker() {
        Permanent floodchaser = addCreatureReady(player1, new Floodchaser());
        floodchaser.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 6);
    }
}

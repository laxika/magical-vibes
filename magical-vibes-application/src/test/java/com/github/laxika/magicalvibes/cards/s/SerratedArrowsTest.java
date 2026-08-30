package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AnabaBodyguard;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SerratedArrows.class, AnabaBodyguard.class})
class SerratedArrowsTest extends BaseCardTest {

    @Test
    @DisplayName("Enters the battlefield with three arrowhead counters")
    void entersWithThreeArrowheadCounters() {
        harness.setHand(player1, List.of(new SerratedArrows()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        Permanent arrows = findPermanent(player1, "Serrated Arrows");
        assertThat(arrows.getCounterCount(CounterType.ARROWHEAD)).isEqualTo(3);
    }

    @Test
    @DisplayName("Ability removes an arrowhead counter and puts a -1/-1 counter on target creature")
    void abilityMovesCounterToTargetCreature() {
        Permanent arrows = addArrows(player1);
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new AnabaBodyguard());

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(arrows.getCounterCount(CounterType.ARROWHEAD)).isEqualTo(2);
        assertThat(arrows.isTapped()).isTrue();
        assertThat(creature.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Ability cannot target a noncreature permanent")
    void abilityCannotTargetNoncreaturePermanent() {
        Permanent arrows = addArrows(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, arrows.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature");

        assertThat(arrows.getCounterCount(CounterType.ARROWHEAD)).isEqualTo(3);
        assertThat(arrows.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Ability cannot be activated without an arrowhead counter")
    void abilityCannotActivateWithoutArrowheadCounter() {
        Permanent arrows = addArrows(player1);
        arrows.setCounterCount(CounterType.ARROWHEAD, 0);
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new AnabaBodyguard());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(arrows.isTapped()).isFalse();
        assertThat(creature.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Upkeep trigger sacrifices the artifact when no arrowhead counters remain")
    void upkeepSacrificesWithoutCounters() {
        Permanent arrows = addArrows(player1);
        arrows.setCounterCount(CounterType.ARROWHEAD, 0);

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve the sacrifice trigger

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(arrows);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Serrated Arrows"));
    }

    @Test
    @DisplayName("Upkeep trigger leaves the artifact alone while counters remain")
    void upkeepKeepsArtifactWithCounters() {
        Permanent arrows = addArrows(player1);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(arrows);
        assertThat(arrows.getCounterCount(CounterType.ARROWHEAD)).isEqualTo(3);
    }

    @Test
    @DisplayName("Upkeep trigger does not apply during an opponent's upkeep")
    void upkeepDoesNotTriggerDuringOpponentsUpkeep() {
        Permanent arrows = addArrows(player1);
        arrows.setCounterCount(CounterType.ARROWHEAD, 0);

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(arrows);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(c -> c.getName().equals("Serrated Arrows"));
    }

    private Permanent addArrows(Player owner) {
        return harness.enterBattlefieldAndReturn(owner, new SerratedArrows());
    }
}

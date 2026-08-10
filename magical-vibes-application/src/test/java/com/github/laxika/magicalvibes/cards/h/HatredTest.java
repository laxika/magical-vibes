package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class HatredTest extends BaseCardTest {

    @Test
    @DisplayName("Pays X life and gives target creature +X/+0")
    void paysLifeAndBoostsPowerOnly() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new Hatred()));
        harness.setLife(player1, 20);
        harness.addMana(player1, ManaColor.BLACK, 6);

        UUID bearId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.castInstantForX(player1, 0, 4, List.of(bearId));
        harness.passBothPriorities();

        Permanent bear = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(16);
        assertThat(bear.getPowerModifier()).isEqualTo(4);
        assertThat(bear.getToughnessModifier()).isEqualTo(0);
        assertThat(bear.getEffectivePower()).isEqualTo(6);
        assertThat(bear.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Cannot be cast when X life cannot be paid")
    void cannotPayMoreLifeThanAvailable() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new Hatred()));
        harness.setLife(player1, 3);
        harness.addMana(player1, ManaColor.BLACK, 6);

        UUID bearId = harness.getPermanentId(player1, "Grizzly Bears");
        assertThatThrownBy(() -> harness.castInstantForX(player1, 0, 4, List.of(bearId)))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(3);
    }

    @Test
    @DisplayName("Boost wears off at cleanup step")
    void boostWearsOffAtCleanup() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new Hatred()));
        harness.setLife(player1, 20);
        harness.addMana(player1, ManaColor.BLACK, 5);

        UUID bearId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.castInstantForX(player1, 0, 3, List.of(bearId));
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent bear = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(bear.getPowerModifier()).isEqualTo(0);
        assertThat(bear.getEffectivePower()).isEqualTo(2);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new FountainOfYouth());
        harness.setHand(player1, List.of(new Hatred()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        UUID targetId = harness.getPermanentId(player1, "Fountain of Youth");
        assertThatThrownBy(() -> harness.castInstant(player1, 0, 1, targetId))
                .isInstanceOf(IllegalStateException.class);
    }
}

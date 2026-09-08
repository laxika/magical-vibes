package com.github.laxika.magicalvibes.cards.s;

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

class StealStrengthTest extends BaseCardTest {

    @Test
    @DisplayName("First target gets +1/+1 and second target gets -1/-1")
    void boostsFirstAndDebuffsSecond() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new StealStrength()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        UUID firstId = harness.getPermanentId(player1, "Grizzly Bears");
        UUID secondId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castInstant(player1, 0, List.of(firstId, secondId));
        harness.passBothPriorities();

        Permanent first = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(first.getPowerModifier()).isEqualTo(1);
        assertThat(first.getToughnessModifier()).isEqualTo(1);

        Permanent second = gd.playerBattlefields.get(player2.getId()).getFirst();
        assertThat(second.getPowerModifier()).isEqualTo(-1);
        assertThat(second.getToughnessModifier()).isEqualTo(-1);
    }

    @Test
    @DisplayName("Cannot target the same creature for both targets")
    void cannotTargetSameCreatureTwice() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new StealStrength()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        UUID bearId = harness.getPermanentId(player1, "Grizzly Bears");

        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.of(bearId, bearId)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("different");
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new FountainOfYouth());
        harness.setHand(player1, List.of(new StealStrength()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        UUID creatureId = harness.getPermanentId(player1, "Grizzly Bears");
        UUID noncreatureId = harness.getPermanentId(player2, "Fountain of Youth");

        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.of(creatureId, noncreatureId)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature");
    }

    @Test
    @DisplayName("The effects wear off at cleanup")
    void effectsWearOffAtCleanup() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new StealStrength()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        UUID firstId = harness.getPermanentId(player1, "Grizzly Bears");
        UUID secondId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castInstant(player1, 0, List.of(firstId, secondId));
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent first = gd.playerBattlefields.get(player1.getId()).getFirst();
        Permanent second = gd.playerBattlefields.get(player2.getId()).getFirst();
        assertThat(first.getPowerModifier()).isZero();
        assertThat(first.getToughnessModifier()).isZero();
        assertThat(second.getPowerModifier()).isZero();
        assertThat(second.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("The first effect still applies when the second target leaves")
    void firstEffectAppliesWhenSecondTargetLeaves() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new StealStrength()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        UUID firstId = harness.getPermanentId(player1, "Grizzly Bears");
        UUID secondId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castInstant(player1, 0, List.of(firstId, secondId));
        gd.playerBattlefields.get(player2.getId()).clear();
        harness.passBothPriorities();

        Permanent first = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(first.getPowerModifier()).isEqualTo(1);
        assertThat(first.getToughnessModifier()).isEqualTo(1);
    }

    @Test
    @DisplayName("The second effect still applies when the first target leaves")
    void secondEffectAppliesWhenFirstTargetLeaves() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new StealStrength()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        UUID firstId = harness.getPermanentId(player1, "Grizzly Bears");
        UUID secondId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castInstant(player1, 0, List.of(firstId, secondId));
        gd.playerBattlefields.get(player1.getId()).clear();
        harness.passBothPriorities();

        Permanent second = gd.playerBattlefields.get(player2.getId()).getFirst();
        assertThat(second.getPowerModifier()).isEqualTo(-1);
        assertThat(second.getToughnessModifier()).isEqualTo(-1);
    }
}

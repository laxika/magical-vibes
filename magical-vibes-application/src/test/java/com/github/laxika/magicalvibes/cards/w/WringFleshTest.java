package com.github.laxika.magicalvibes.cards.w;

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

class WringFleshTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving gives target creature -3/-1 until end of turn")
    void resolvingGivesMinusThreeMinusOne() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new WringFlesh()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        UUID bearId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castInstant(player1, 0, bearId);
        harness.passBothPriorities();

        Permanent bear = gd.playerBattlefields.get(player2.getId()).getFirst();
        assertThat(bear.getEffectivePower()).isEqualTo(-1);
        assertThat(bear.getEffectiveToughness()).isEqualTo(1);
        harness.assertInGraveyard(player1, "Wring Flesh");
    }

    @Test
    @DisplayName("A creature with toughness 1 dies to the -1 toughness")
    void oneToughnessCreatureDies() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new WringFlesh(), new WringFlesh()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        UUID bearId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castInstant(player1, 0, bearId);
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.castInstant(player1, 0, bearId);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Debuff wears off at cleanup")
    void debuffWearsOff() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new WringFlesh()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        UUID bearId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.castInstant(player1, 0, bearId);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent bear = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(bear.getEffectivePower()).isEqualTo(2);
        assertThat(bear.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new FountainOfYouth());
        harness.setHand(player1, List.of(new WringFlesh()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        UUID fountainId = harness.getPermanentId(player1, "Fountain of Youth");
        assertThatThrownBy(() -> harness.castInstant(player1, 0, fountainId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }
}

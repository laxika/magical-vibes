package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.m.Manalith;
import com.github.laxika.magicalvibes.cards.r.RuneclawBear;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.u.Unsummon;
import com.github.laxika.magicalvibes.testutil.CardUsed;
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

@CardUsed({WringFlesh.class, RuneclawBear.class, Manalith.class, LlanowarElves.class, Unsummon.class})
class WringFleshTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving gives target creature -3/-1 until end of turn")
    void resolvingGivesMinusThreeMinusOne() {
        harness.addToBattlefield(player2, new RuneclawBear());
        harness.setHand(player1, List.of(new WringFlesh()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        UUID bearId = harness.getPermanentId(player2, "Runeclaw Bear");
        harness.castAndResolveInstant(player1, 0, bearId);

        Permanent bear = gd.playerBattlefields.get(player2.getId()).getFirst();
        assertThat(bear.getEffectivePower()).isEqualTo(-1);
        assertThat(bear.getEffectiveToughness()).isEqualTo(1);
        harness.assertInGraveyard(player1, "Wring Flesh");
    }

    @Test
    @DisplayName("A creature with toughness 1 dies to the -1 toughness")
    void oneToughnessCreatureDies() {
        harness.addToBattlefield(player2, new RuneclawBear());
        harness.setHand(player1, List.of(new WringFlesh(), new WringFlesh()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        UUID bearId = harness.getPermanentId(player2, "Runeclaw Bear");
        harness.castAndResolveInstant(player1, 0, bearId);
        harness.clearPriorityPassed();
        harness.castAndResolveInstant(player1, 0, bearId);

        harness.assertNotOnBattlefield(player2, "Runeclaw Bear");
        harness.assertInGraveyard(player2, "Runeclaw Bear");
    }

    @Test
    @DisplayName("Debuff wears off at cleanup")
    void debuffWearsOff() {
        harness.addToBattlefield(player1, new RuneclawBear());
        harness.setHand(player1, List.of(new WringFlesh()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        UUID bearId = harness.getPermanentId(player1, "Runeclaw Bear");
        harness.castAndResolveInstant(player1, 0, bearId);

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
        harness.addToBattlefield(player1, new RuneclawBear());
        harness.addToBattlefield(player1, new Manalith());
        harness.setHand(player1, List.of(new WringFlesh()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        UUID fountainId = harness.getPermanentId(player1, "Manalith");
        assertThatThrownBy(() -> harness.castInstant(player1, 0, fountainId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("A single Wring Flesh kills a creature that starts with one toughness")
    void killsCreatureWithOneBaseToughness() {
        harness.addToBattlefield(player2, new LlanowarElves());
        harness.setHand(player1, List.of(new WringFlesh()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player2, "Llanowar Elves"));

        harness.assertNotOnBattlefield(player2, "Llanowar Elves");
        harness.assertInGraveyard(player2, "Llanowar Elves");
        harness.assertInGraveyard(player1, "Wring Flesh");
    }

    @Test
    @DisplayName("Wring Flesh does not affect another creature when its target leaves before resolution")
    void targetLeavesBeforeResolution() {
        harness.addToBattlefield(player2, new RuneclawBear());
        harness.addToBattlefield(player2, new LlanowarElves());
        harness.setHand(player1, List.of(new WringFlesh(), new Unsummon()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        UUID bearId = harness.getPermanentId(player2, "Runeclaw Bear");
        harness.castInstant(player1, 0, bearId);
        harness.castAndResolveInstant(player1, 0, bearId);
        harness.passBothPriorities();

        harness.assertInHand(player2, "Runeclaw Bear");
        harness.assertNotOnBattlefield(player2, "Runeclaw Bear");
        harness.assertOnBattlefield(player2, "Llanowar Elves");
        Permanent elves = gd.playerBattlefields.get(player2.getId()).getFirst();
        assertThat(elves.getEffectivePower()).isEqualTo(1);
        assertThat(elves.getEffectiveToughness()).isEqualTo(1);
        harness.assertInGraveyard(player1, "Wring Flesh");
        harness.assertInGraveyard(player1, "Unsummon");
        assertThat(gd.stack).isEmpty();
    }
}

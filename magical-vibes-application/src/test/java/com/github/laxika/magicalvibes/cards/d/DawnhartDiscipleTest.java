package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.e.EliteVanguard;
import com.github.laxika.magicalvibes.cards.g.GatherTheTownsfolk;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DawnhartDisciple.class, EliteVanguard.class, GrizzlyBears.class, GatherTheTownsfolk.class})
class DawnhartDiscipleTest extends BaseCardTest {

    @Test
    @DisplayName("Gets +1/+1 until end of turn when another Human enters")
    void boostsWhenHumanEnters() {
        Permanent disciple = harness.addToBattlefieldAndReturn(player1, new DawnhartDisciple());

        harness.setHand(player1, List.of(new EliteVanguard()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(disciple.getPowerModifier()).isEqualTo(1);
        assertThat(disciple.getToughnessModifier()).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, disciple)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, disciple)).isEqualTo(3);
    }

    @Test
    @DisplayName("Does not boost when a non-Human creature enters")
    void noBoostWhenNonHumanEnters() {
        Permanent disciple = harness.addToBattlefieldAndReturn(player1, new DawnhartDisciple());

        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(disciple.getPowerModifier()).isEqualTo(0);
        assertThat(disciple.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Does not trigger when an opponent's Human enters")
    void noBoostWhenOpponentHumanEnters() {
        Permanent disciple = harness.addToBattlefieldAndReturn(player1, new DawnhartDisciple());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setHand(player2, List.of(new EliteVanguard()));
        harness.addMana(player2, ManaColor.WHITE, 1);

        harness.castCreature(player2, 0);
        harness.passBothPriorities();

        assertThat(disciple.getPowerModifier()).isEqualTo(0);
        assertThat(disciple.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Boost wears off at end of turn")
    void boostWearsOffAtCleanup() {
        Permanent disciple = harness.addToBattlefieldAndReturn(player1, new DawnhartDisciple());

        harness.setHand(player1, List.of(new EliteVanguard()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castCreature(player1, 0);
        resolveAllTriggers();
        assertThat(disciple.getPowerModifier()).isEqualTo(1);

        harness.setHand(player1, new ArrayList<>());
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(disciple.getPowerModifier()).isEqualTo(0);
        assertThat(disciple.getToughnessModifier()).isEqualTo(0);
        assertThat(gqs.getEffectivePower(gd, disciple)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, disciple)).isEqualTo(2);
    }

    @Test
    @DisplayName("Stacks multiple boosts from multiple Human entries")
    void stacksMultipleBoosts() {
        Permanent disciple = harness.addToBattlefieldAndReturn(player1, new DawnhartDisciple());

        harness.setHand(player1, List.of(new EliteVanguard()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castCreature(player1, 0);
        resolveAllTriggers();
        assertThat(disciple.getPowerModifier()).isEqualTo(1);

        harness.setHand(player1, List.of(new EliteVanguard()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(disciple.getPowerModifier()).isEqualTo(2);
        assertThat(disciple.getToughnessModifier()).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, disciple)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, disciple)).isEqualTo(4);
    }

    @Test
    @DisplayName("Does not trigger for its own entry")
    void doesNotBoostItselfOnEntry() {
        harness.setHand(player1, List.of(new DawnhartDisciple()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent disciple = findPermanent(player1, "Dawnhart Disciple");
        assertThat(gd.stack).isEmpty();
        assertThat(disciple.getPowerModifier()).isZero();
        assertThat(disciple.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("An entering Disciple boosts the existing Disciple but not itself")
    void anotherDiscipleBoostsOnlyExistingDisciple() {
        Permanent original = harness.addToBattlefieldAndReturn(player1, new DawnhartDisciple());
        harness.setHand(player1, List.of(new DawnhartDisciple()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent entering = findPermanents(player1, "Dawnhart Disciple").stream()
                .filter(permanent -> !permanent.getId().equals(original.getId()))
                .findFirst().orElseThrow();
        assertThat(gd.stack).hasSize(1);
        assertThat(original.getPowerModifier()).isZero();
        resolveAllTriggers();

        assertThat(original.getPowerModifier()).isEqualTo(1);
        assertThat(original.getToughnessModifier()).isEqualTo(1);
        assertThat(entering.getPowerModifier()).isZero();
        assertThat(entering.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Each Human token entering simultaneously gives a separate boost")
    void boostsForEachHumanToken() {
        Permanent disciple = harness.addToBattlefieldAndReturn(player1, new DawnhartDisciple());
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new GatherTheTownsfolk()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castSorcery(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).hasSize(2);
        assertThat(disciple.getPowerModifier()).isZero();
        harness.passBothPriorities();
        assertThat(disciple.getPowerModifier()).isEqualTo(1);
        assertThat(disciple.getToughnessModifier()).isEqualTo(1);
        resolveAllTriggers();
        assertThat(disciple.getPowerModifier()).isEqualTo(2);
        assertThat(disciple.getToughnessModifier()).isEqualTo(2);
    }
}

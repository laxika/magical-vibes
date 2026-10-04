package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.d.DiscipleOfTheOldWays;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FuriousResistance.class, DiscipleOfTheOldWays.class})
class FuriousResistanceTest extends BaseCardTest {

    @Test
    @DisplayName("Gives a blocking creature +3/+0 and first strike")
    void boostsBlockingCreature() {
        Permanent blocker = addBlockingBear(player1);
        setupSpell();

        harness.castInstant(player1, 0, blocker.getId());
        harness.passBothPriorities();

        assertThat(blocker.getPowerModifier()).isEqualTo(3);
        assertThat(blocker.getToughnessModifier()).isEqualTo(0);
        assertThat(blocker.hasKeyword(Keyword.FIRST_STRIKE)).isTrue();
        harness.assertInGraveyard(player1, "Furious Resistance");
    }

    @Test
    @DisplayName("Boost and first strike wear off at cleanup")
    void effectsWearOff() {
        Permanent blocker = addBlockingBear(player1);
        setupSpell();

        harness.castInstant(player1, 0, blocker.getId());
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(blocker.getPowerModifier()).isEqualTo(0);
        assertThat(blocker.hasKeyword(Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Cannot target a creature that is not blocking")
    void cannotTargetNonBlockingCreature() {
        addBlockingBear(player1);
        Permanent bystander = harness.addToBattlefieldAndReturn(player1, new DiscipleOfTheOldWays());
        setupSpell();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, bystander.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("blocking");
    }

    @Test
    @DisplayName("Can boost an opponent's blocking creature")
    void boostsOpponentsBlocker() {
        Permanent blocker = addBlockingBear(player2);
        setupSpell();

        harness.castInstant(player1, 0, blocker.getId());
        harness.passBothPriorities();

        assertThat(blocker.getPowerModifier()).isEqualTo(3);
        assertThat(blocker.getToughnessModifier()).isEqualTo(0);
        assertThat(blocker.hasKeyword(Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("Does not resolve if the target stops blocking")
    void targetStopsBlockingBeforeResolution() {
        Permanent blocker = addBlockingBear(player1);
        setupSpell();

        harness.castInstant(player1, 0, blocker.getId());
        blocker.setBlocking(false);
        harness.passBothPriorities();

        assertThat(blocker.getPowerModifier()).isEqualTo(0);
        assertThat(blocker.getToughnessModifier()).isEqualTo(0);
        assertThat(blocker.hasKeyword(Keyword.FIRST_STRIKE)).isFalse();
        harness.assertInGraveyard(player1, "Furious Resistance");
    }

    @Test
    @DisplayName("Resolved effects remain when the creature stops blocking")
    void resolvedEffectsDoNotRequireContinuedBlocking() {
        Permanent blocker = addBlockingBear(player1);
        setupSpell();

        harness.castInstant(player1, 0, blocker.getId());
        harness.passBothPriorities();
        blocker.setBlocking(false);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);

        assertThat(blocker.getPowerModifier()).isEqualTo(3);
        assertThat(blocker.getToughnessModifier()).isEqualTo(0);
        assertThat(blocker.hasKeyword(Keyword.FIRST_STRIKE)).isTrue();
    }

    private void setupSpell() {
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.forceActivePlayer(player1);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new FuriousResistance()));
        harness.addMana(player1, ManaColor.RED, 1);
    }

    private Permanent addBlockingBear(Player player) {
        Permanent bear = harness.addToBattlefieldAndReturn(player, new DiscipleOfTheOldWays());
        bear.setSummoningSick(false);
        bear.setBlocking(true);
        return bear;
    }
}

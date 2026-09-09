package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.b.BenalishInfantry;
import com.github.laxika.magicalvibes.cards.m.MindStone;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FitOfRage.class, BenalishInfantry.class, MindStone.class})
class FitOfRageTest extends BaseCardTest {

    @Test
    @DisplayName("Grants +3/+3 and first strike to target creature")
    void grantsBoostAndFirstStrike() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new BenalishInfantry());
        harness.setHand(player1, List.of(new FitOfRage()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castSorcery(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(creature.getPowerModifier()).isEqualTo(3);
        assertThat(creature.getToughnessModifier()).isEqualTo(3);
        assertThat(creature.getGrantedKeywords()).contains(Keyword.FIRST_STRIKE);
    }

    @Test
    @DisplayName("Boost and first strike wear off at cleanup step")
    void boostWearsOffAtCleanup() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new BenalishInfantry());
        harness.setHand(player1, List.of(new FitOfRage()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castSorcery(player1, 0, creature.getId());
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(creature.getPowerModifier()).isZero();
        assertThat(creature.getToughnessModifier()).isZero();
        assertThat(creature.getGrantedKeywords()).doesNotContain(Keyword.FIRST_STRIKE);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new MindStone());
        harness.setHand(player1, List.of(new FitOfRage()));
        harness.addMana(player1, ManaColor.RED, 2);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Can target a creature controlled by an opponent")
    void canTargetOpponentsCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new BenalishInfantry());
        harness.setHand(player1, List.of(new FitOfRage()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castSorcery(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(creature.getPowerModifier()).isEqualTo(3);
        assertThat(creature.getToughnessModifier()).isEqualTo(3);
        assertThat(creature.getGrantedKeywords()).contains(Keyword.FIRST_STRIKE);
    }
}

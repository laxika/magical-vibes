package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.b.BenalishInfantry;
import com.github.laxika.magicalvibes.cards.d.DingusStaff;
import com.github.laxika.magicalvibes.model.Card;
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

@CardUsed({GuidedStrike.class, BenalishInfantry.class, DingusStaff.class})
class GuidedStrikeTest extends BaseCardTest {

    @Test
    @DisplayName("Grants +1/+0 and first strike to target creature, then draws a card")
    void boostsGrantsFirstStrikeAndDraws() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new BenalishInfantry());
        harness.setHand(player1, List.of(new GuidedStrike()));
        harness.setLibrary(player1, List.of(new BenalishInfantry()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castInstant(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(creature.getPowerModifier()).isEqualTo(1);
        assertThat(creature.getToughnessModifier()).isZero();
        assertThat(creature.getGrantedKeywords()).contains(Keyword.FIRST_STRIKE);

        List<Card> hand = harness.getGameData().playerHands.get(player1.getId());
        assertThat(hand).hasSize(1);
    }

    @Test
    @DisplayName("Boost and first strike wear off at end of turn")
    void wearsOffAtCleanup() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new BenalishInfantry());
        harness.setHand(player1, List.of(new GuidedStrike()));
        harness.setLibrary(player1, List.of(new BenalishInfantry()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castInstant(player1, 0, creature.getId());
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(creature.getPowerModifier()).isZero();
        assertThat(creature.getGrantedKeywords()).doesNotContain(Keyword.FIRST_STRIKE);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        harness.addToBattlefield(player1, new BenalishInfantry());
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new DingusStaff());
        harness.setHand(player1, List.of(new GuidedStrike()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Can target a creature controlled by an opponent")
    void canTargetOpponentsCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new BenalishInfantry());
        harness.setHand(player1, List.of(new GuidedStrike()));
        harness.setLibrary(player1, List.of(new BenalishInfantry()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castInstant(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(creature.getPowerModifier()).isEqualTo(1);
        assertThat(creature.getGrantedKeywords()).contains(Keyword.FIRST_STRIKE);
    }
}

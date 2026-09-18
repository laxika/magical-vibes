package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.b.BenalishInfantry;
import com.github.laxika.magicalvibes.cards.d.DingusStaff;
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
        int powerBefore = gqs.getEffectivePower(gd, creature);
        int toughnessBefore = gqs.getEffectiveToughness(gd, creature);
        harness.setHand(player1, List.of(new GuidedStrike()));
        harness.setLibrary(player1, List.of(new BenalishInfantry()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castAndResolveInstant(player1, 0, creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(powerBefore + 1);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(toughnessBefore);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.FIRST_STRIKE)).isTrue();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Boost and first strike wear off at end of turn")
    void wearsOffAtCleanup() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new BenalishInfantry());
        int powerBefore = gqs.getEffectivePower(gd, creature);
        int toughnessBefore = gqs.getEffectiveToughness(gd, creature);
        harness.setHand(player1, List.of(new GuidedStrike()));
        harness.setLibrary(player1, List.of(new BenalishInfantry()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castAndResolveInstant(player1, 0, creature.getId());

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(powerBefore);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(toughnessBefore);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.FIRST_STRIKE)).isFalse();
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
        int powerBefore = gqs.getEffectivePower(gd, creature);
        int opponentHandSizeBefore = gd.playerHands.get(player2.getId()).size();
        harness.setHand(player1, List.of(new GuidedStrike()));
        harness.setLibrary(player1, List.of(new BenalishInfantry()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castAndResolveInstant(player1, 0, creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(powerBefore + 1);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(opponentHandSizeBefore);
    }

    @Test
    @DisplayName("Fizzles without applying effects or drawing if the target leaves before resolution")
    void fizzlesWhenTargetLeavesBeforeResolution() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new BenalishInfantry());
        harness.setHand(player1, List.of(new GuidedStrike()));
        harness.setLibrary(player1, List.of(new BenalishInfantry()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castInstant(player1, 0, creature.getId());
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, creature));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(creature);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        harness.assertInGraveyard(player1, "Guided Strike");
    }
}

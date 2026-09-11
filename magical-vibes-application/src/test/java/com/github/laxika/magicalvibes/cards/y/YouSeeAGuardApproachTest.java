package com.github.laxika.magicalvibes.cards.y;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({YouSeeAGuardApproach.class, GrizzlyBears.class, Island.class})
class YouSeeAGuardApproachTest extends BaseCardTest {

    @Test
    void distractsTheGuardByTappingAnyCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        cast(0, target);

        assertThat(target.isTapped()).isTrue();
    }

    @Test
    void hideGrantsHexproofToYourCreatureUntilEndOfTurn() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        cast(1, target);

        assertThat(gqs.hasKeyword(gd, target, Keyword.HEXPROOF)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, target, Keyword.HEXPROOF)).isFalse();
    }

    @Test
    void modesOnlyAllowTheirSpecifiedTargets() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Island());
        prepareSpell();

        assertThatThrownBy(() -> harness.castModalInstant(player1, 0, 0, List.of(land.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature");

        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        prepareSpell();

        assertThatThrownBy(() -> harness.castModalInstant(
                player1, 0, 1, List.of(opposingCreature.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature you control");
    }

    private void cast(int modeIndex, Permanent target) {
        prepareSpell();
        harness.castModalInstant(player1, 0, modeIndex, List.of(target.getId()));
        harness.passBothPriorities();
    }

    private void prepareSpell() {
        harness.setHand(player1, List.of(new YouSeeAGuardApproach()));
        harness.addMana(player1, ManaColor.BLUE, 1);
    }
}

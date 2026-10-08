package com.github.laxika.magicalvibes.cards.y;

import com.github.laxika.magicalvibes.cards.h.HillGiantHerdgorger;
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

@CardUsed({YouSeeAGuardApproach.class, HillGiantHerdgorger.class, Island.class})
class YouSeeAGuardApproachTest extends BaseCardTest {

    @Test
    void distractsTheGuardByTappingAnyCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new HillGiantHerdgorger());

        cast(0, target);

        assertThat(target.isTapped()).isTrue();
    }

    @Test
    void hideGrantsHexproofToYourCreatureUntilEndOfTurn() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new HillGiantHerdgorger());

        cast(1, target);

        assertThat(gqs.hasKeyword(gd, target, Keyword.HEXPROOF)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        assertThat(gqs.hasKeyword(gd, target, Keyword.HEXPROOF)).isTrue();
        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(gqs.hasKeyword(gd, target, Keyword.HEXPROOF)).isFalse();
    }

    @Test
    void modesOnlyAllowTheirSpecifiedTargets() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Island());
        prepareSpell();

        assertThatThrownBy(() -> harness.castModalInstant(player1, 0, 0, List.of(land.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature");

        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new HillGiantHerdgorger());
        prepareSpell();

        assertThatThrownBy(() -> harness.castModalInstant(
                player1, 0, 1, List.of(opposingCreature.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature you control");
    }

    @Test
    void distractCanTapYourOwnCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new HillGiantHerdgorger());

        cast(0, target);

        assertThat(target.isTapped()).isTrue();
        assertThat(gqs.hasKeyword(gd, target, Keyword.HEXPROOF)).isFalse();
    }

    @Test
    void distractCanTargetAnAlreadyTappedCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new HillGiantHerdgorger());
        target.setTapped(true);

        cast(0, target);

        assertThat(target.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void hideMakesAnOpponentsPendingSpellLoseItsTarget() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new HillGiantHerdgorger());
        harness.setHand(player2, List.of(new YouSeeAGuardApproach()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.castModalInstant(player2, 0, 0, List.of(target.getId()));

        cast(1, target);
        harness.passBothPriorities();

        assertThat(target.isTapped()).isFalse();
        assertThat(gqs.hasKeyword(gd, target, Keyword.HEXPROOF)).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void hexproofStillAllowsYourOwnDistractSpellToTargetTheCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new HillGiantHerdgorger());

        cast(1, target);
        cast(0, target);

        assertThat(target.isTapped()).isTrue();
        assertThat(gqs.hasKeyword(gd, target, Keyword.HEXPROOF)).isTrue();
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

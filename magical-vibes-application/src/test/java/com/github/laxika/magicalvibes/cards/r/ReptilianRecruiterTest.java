package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.b.BellowsLizard;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ReptilianRecruiter.class, BellowsLizard.class, GrizzlyBears.class, HillGiant.class, Mountain.class})
class ReptilianRecruiterTest extends BaseCardTest {

    @Test
    @DisplayName("Steals, untaps, and grants haste to a creature with power 2 or less")
    void stealsSmallCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        target.tap();

        castRecruiter(target.getId());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isFalse();
        assertThat(target.hasKeyword(Keyword.HASTE)).isTrue();
        assertThat(gd.playerBattlefields.get(player1.getId())).anyMatch(p -> p.getId().equals(target.getId()));
        assertThat(gd.isStolenUntilEndOfTurn(target.getId())).isTrue();
    }

    @Test
    @DisplayName("Steals a creature with greater power when you control another Lizard")
    void stealsLargeCreatureWithAnotherLizard() {
        harness.addToBattlefield(player1, new BellowsLizard());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        target.tap();

        castRecruiter(target.getId());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isFalse();
        assertThat(target.hasKeyword(Keyword.HASTE)).isTrue();
        assertThat(gd.playerBattlefields.get(player1.getId())).anyMatch(p -> p.getId().equals(target.getId()));
    }

    @Test
    @DisplayName("Does nothing to a large creature when you control no other Lizard")
    void doesNothingWithoutQualifyingCondition() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        target.tap();

        castRecruiter(target.getId());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
        assertThat(target.hasKeyword(Keyword.HASTE)).isFalse();
        assertThat(gd.playerBattlefields.get(player2.getId())).anyMatch(p -> p.getId().equals(target.getId()));
        assertThat(gd.isStolenUntilEndOfTurn(target.getId())).isFalse();
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Mountain());

        assertThatThrownBy(() -> castRecruiter(target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Control and haste expire at cleanup")
    void controlAndHasteExpireAtCleanup() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castRecruiter(target.getId());
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(target.hasKeyword(Keyword.HASTE)).isFalse();
        assertThat(gd.playerBattlefields.get(player2.getId())).anyMatch(p -> p.getId().equals(target.getId()));
        assertThat(gd.isStolenUntilEndOfTurn(target.getId())).isFalse();
    }

    private void castRecruiter(UUID targetId) {
        harness.setHand(player1, List.of(new ReptilianRecruiter()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreature(player1, 0, 0, targetId);
        harness.passBothPriorities();
    }
}

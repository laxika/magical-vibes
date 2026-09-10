package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
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

@CardUsed({TeamTactics.class, GrizzlyBears.class, Mountain.class})
class TeamTacticsTest extends BaseCardTest {

    @Test
    void grantsDoubleStrikeWithoutTeamwork() {
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

        cast(target, List.of());

        assertThat(gqs.hasKeyword(gd, target, Keyword.DOUBLE_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, target, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    void grantsDoubleStrikeAndTrampleWithTeamwork() {
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        Permanent teammate = addCreatureReady(player1, new GrizzlyBears());

        cast(target, List.of(teammate.getId()));

        assertThat(gqs.hasKeyword(gd, target, Keyword.DOUBLE_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, target, Keyword.TRAMPLE)).isTrue();
        assertThat(teammate.isTapped()).isTrue();
    }

    @Test
    void grantedKeywordsWearOffAtEndOfTurn() {
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        Permanent teammate = addCreatureReady(player1, new GrizzlyBears());

        cast(target, List.of(teammate.getId()));
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, target, Keyword.DOUBLE_STRIKE)).isFalse();
        assertThat(gqs.hasKeyword(gd, target, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    void cannotTargetNonCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Mountain());
        addMana();
        harness.setHand(player1, List.of(new TeamTactics()));

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void cast(Permanent target, List<java.util.UUID> teamworkPermanents) {
        harness.setHand(player1, List.of(new TeamTactics()));
        addMana();
        harness.castInstantWithSacrifices(player1, 0, target.getId(), teamworkPermanents);
        harness.passBothPriorities();
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }
}

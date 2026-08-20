package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FirjasRetributionTest extends BaseCardTest {

    @Test
    @DisplayName("Chapter I creates a flying, vigilant Angel Warrior")
    void chapterICreatesAngelWarrior() {
        Permanent saga = addSagaWithLore(0);

        triggerAndResolveChapter(saga);

        Permanent token = findPermanent(player1, "Angel Warrior");
        assertThat(token.getCard().getPower()).isEqualTo(4);
        assertThat(token.getCard().getToughness()).isEqualTo(4);
        assertThat(token.getCard().getColor()).isEqualTo(CardColor.WHITE);
        assertThat(token.getCard().getSubtypes()).contains(CardSubtype.ANGEL, CardSubtype.WARRIOR);
        assertThat(token.hasKeyword(Keyword.FLYING)).isTrue();
        assertThat(token.hasKeyword(Keyword.VIGILANCE)).isTrue();
    }

    @Test
    @DisplayName("Chapter II grants Angels a power-restricted destruction ability")
    void chapterIIGrantsPowerRestrictedDestruction() {
        Permanent saga = addSagaWithLore(1);
        Permanent angel = addCreatureReady(player1, new SerraAngel());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        Permanent equalPowerTarget = addCreatureReady(player2, new SerraAngel());

        triggerAndResolveChapter(saga);

        int angelIndex = gd.playerBattlefields.get(player1.getId()).indexOf(angel);
        assertThatThrownBy(() -> harness.activateAbility(player1, angelIndex, null, equalPowerTarget.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("power less than this creature's power");

        harness.activateAbility(player1, angelIndex, null, target.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Serra Angel");
    }

    @Test
    @DisplayName("Chapter III grants double strike to Angels until end of turn")
    void chapterIIIGrantsDoubleStrikeUntilEndOfTurn() {
        Permanent saga = addSagaWithLore(2);
        Permanent angel = addCreatureReady(player1, new SerraAngel());
        Permanent nonAngel = addCreatureReady(player1, new GrizzlyBears());

        triggerAndResolveChapter(saga);

        assertThat(angel.hasKeyword(Keyword.DOUBLE_STRIKE)).isTrue();
        assertThat(nonAngel.hasKeyword(Keyword.DOUBLE_STRIKE)).isFalse();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(angel.hasKeyword(Keyword.DOUBLE_STRIKE)).isFalse();
    }

    private Permanent addSagaWithLore(int lore) {
        Permanent saga = harness.addToBattlefieldAndReturn(player1, new FirjasRetribution());
        saga.setCounterCount(CounterType.LORE, lore);
        return saga;
    }

    private void triggerAndResolveChapter(Permanent saga) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        assertThat(saga.getCounterCount(CounterType.LORE)).isGreaterThan(0);
        harness.passBothPriorities();
    }
}

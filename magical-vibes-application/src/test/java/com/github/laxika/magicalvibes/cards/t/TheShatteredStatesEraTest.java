package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.n.NamelessConqueror;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TheShatteredStatesEra.class, NamelessConqueror.class, GrizzlyBears.class})
class TheShatteredStatesEraTest extends BaseCardTest {

    @Test
    @DisplayName("Chapter I gains control, untaps, and grants haste to a creature")
    void chapterIThreatensCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        target.tap();
        addSagaWithLore(0);

        advanceToNextChapter();
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(target);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);
        assertThat(target.isTapped()).isFalse();
        assertThat(gqs.hasKeyword(gd, target, Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("Chapter II gives creatures you control +1/+0 until end of turn")
    void chapterIIBoostsOwnCreatures() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        addSagaWithLore(1);

        advanceToNextChapter();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, ownCreature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, ownCreature)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, opposingCreature)).isEqualTo(2);
    }

    @Test
    @DisplayName("Chapter III returns the Saga transformed as Nameless Conqueror")
    void chapterIIITransformsIntoNamelessConqueror() {
        addSagaWithLore(2);

        advanceToNextChapter();
        harness.passBothPriorities();

        Permanent conqueror = findPermanent(player1, "Nameless Conqueror");
        assertThat(conqueror).isNotNull();
        assertThat(conqueror.isTransformed()).isTrue();
    }

    private Permanent addSagaWithLore(int loreCounters) {
        Permanent saga = harness.addToBattlefieldAndReturn(player1, new TheShatteredStatesEra());
        saga.setCounterCount(CounterType.LORE, loreCounters);
        return saga;
    }

    private void advanceToNextChapter() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}

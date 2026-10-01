package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GildedGoose;
import com.github.laxika.magicalvibes.cards.g.GoldenEgg;
import com.github.laxika.magicalvibes.cards.m.Millstone;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AGoldenOpportunity.class, AvenWarhawk.class, GildedGoose.class,
        GoldenEgg.class, Millstone.class})
class AGoldenOpportunityTest extends BaseCardTest {

    @Test
    void chapterIConjuresGildedGoose() {
        addSagaWithLore(0);

        triggerChapter();
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Gilded Goose")).singleElement()
                .satisfies(permanent -> assertThat(permanent.getCard().isToken()).isFalse());
    }

    @Test
    void chapterIITapsBirdSacrificesArtifactAndConjuresGoldenEgg() {
        Permanent bird = harness.addToBattlefieldAndReturn(player1, new AvenWarhawk());
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new Millstone());
        addSagaWithLore(1);

        triggerChapter();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        PendingInteraction.PermanentChoice sacrificeChoice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(sacrificeChoice.validPermanentIds()).containsExactly(artifact.getId());
        assertThat(bird.isTapped()).isTrue();

        harness.handlePermanentChosen(player1, artifact.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(artifact);
        assertThat(findPermanents(player1, "Golden Egg")).singleElement();
    }

    @Test
    void chapterIIIDeclinedLeavesPermanentsUnchanged() {
        Permanent bird = harness.addToBattlefieldAndReturn(player1, new AvenWarhawk());
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new Millstone());
        addSagaWithLore(2);

        triggerChapter();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(bird.isTapped()).isFalse();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(artifact);
        assertThat(findPermanents(player1, "Golden Egg")).isEmpty();
    }

    @Test
    void chapterDoesNothingWithoutAnArtifactToSacrifice() {
        Permanent bird = harness.addToBattlefieldAndReturn(player1, new AvenWarhawk());
        addSagaWithLore(1);

        triggerChapter();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(bird.isTapped()).isFalse();
        assertThat(findPermanents(player1, "Golden Egg")).isEmpty();
    }

    private Permanent addSagaWithLore(int loreCounters) {
        Permanent saga = harness.addToBattlefieldAndReturn(player1, new AGoldenOpportunity());
        saga.setCounterCount(CounterType.LORE, loreCounters);
        return saga;
    }

    private void triggerChapter() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}

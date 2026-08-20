package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.d.DarksteelAxe;
import com.github.laxika.magicalvibes.cards.d.DarksteelRelic;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TheTricksterGodsHeist.class, DarksteelAxe.class, DarksteelRelic.class,
        Forest.class, GrizzlyBears.class})
class TheTricksterGodsHeistTest extends BaseCardTest {

    @Test
    @DisplayName("Chapter I exchanges control of two target creatures")
    void chapterIExchangesCreatureControl() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        addSaga(0);

        triggerChapter();
        harness.handlePermanentChosen(player1, ownCreature.getId());
        harness.handlePermanentChosen(player1, opposingCreature.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(opposingCreature);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(ownCreature);
    }

    @Test
    @DisplayName("Chapter II exchanges control of matching nonbasic noncreature permanents")
    void chapterIIExchangesMatchingPermanentControl() {
        Permanent ownArtifact = harness.addToBattlefieldAndReturn(player1, new DarksteelRelic());
        Permanent opposingArtifact = harness.addToBattlefieldAndReturn(player2, new DarksteelAxe());
        Permanent basicLand = harness.addToBattlefieldAndReturn(player1, new Forest());
        addSaga(1);

        triggerChapter();
        PendingInteraction.PermanentChoice firstChoice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(firstChoice.validPermanentIds()).contains(ownArtifact.getId(), opposingArtifact.getId())
                .doesNotContain(basicLand.getId());
        harness.handlePermanentChosen(player1, ownArtifact.getId());
        harness.handlePermanentChosen(player1, opposingArtifact.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(opposingArtifact);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(ownArtifact);
    }

    @Test
    @DisplayName("Chapter III makes a target player lose life and gains you life")
    void chapterIIILosesAndGainsLife() {
        addSaga(2);

        triggerChapter();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(23);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
    }

    private Permanent addSaga(int loreCounters) {
        Permanent saga = harness.addToBattlefieldAndReturn(player1, new TheTricksterGodsHeist());
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

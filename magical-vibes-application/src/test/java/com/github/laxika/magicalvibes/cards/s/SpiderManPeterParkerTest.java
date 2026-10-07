package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AngelOfMercy;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SpiderManPeterParker.class, AngelOfMercy.class, GrizzlyBears.class})
class SpiderManPeterParkerTest extends BaseCardTest {

    @Test
    @DisplayName("Life gain puts a counter on a controlled creature and grants indestructible")
    void lifeGainStrengthensAndProtectsControlledCreature() {
        addCreatureReady(player1, new SpiderManPeterParker());
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        Permanent opponentCreature = addCreatureReady(player2, new GrizzlyBears());

        castAngelOfMercy();
        resolveAllTriggers();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validPermanentIds()).contains(target.getId());
        assertThat(choice.validPermanentIds()).doesNotContain(opponentCreature.getId());

        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, target, Keyword.INDESTRUCTIBLE)).isTrue();
    }

    @Test
    @DisplayName("Granted indestructible wears off at end of turn")
    void indestructibleWearsOffAtEndOfTurn() {
        addCreatureReady(player1, new SpiderManPeterParker());
        Permanent target = addCreatureReady(player1, new GrizzlyBears());

        castAngelOfMercy();
        resolveAllTriggers();
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, target, Keyword.INDESTRUCTIBLE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, target, Keyword.INDESTRUCTIBLE)).isFalse();
        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    private void castAngelOfMercy() {
        harness.castFromHand(player1, new AngelOfMercy(), "{4}{W}");
    }

    @Test
    @DisplayName("Spider-Man can target himself and gains one counter for three life")
    void canTargetSelfForMultiPointLifeGain() {
        Permanent spiderMan = addCreatureReady(player1, new SpiderManPeterParker());
        castAngelOfMercy();
        resolveAllTriggers();
        harness.handlePermanentChosen(player1, spiderMan.getId());
        resolveAllTriggers();

        assertThat(spiderMan.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, spiderMan, Keyword.INDESTRUCTIBLE)).isTrue();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("An opponent gaining life does not trigger Spider-Man")
    void opponentLifeGainDoesNotTrigger() {
        Permanent spiderMan = addCreatureReady(player1, new SpiderManPeterParker());
        harness.forceActivePlayer(player2);
        harness.castFromHand(player2, new AngelOfMercy(), "{4}{W}");
        resolveAllTriggers();

        assertThat(spiderMan.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gqs.hasKeyword(gd, spiderMan, Keyword.INDESTRUCTIBLE)).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Separate life gain events each add a counter to an indestructible target")
    void separateLifeGainsEachAddACounter() {
        addCreatureReady(player1, new SpiderManPeterParker());
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        castAngelOfMercy();
        resolveAllTriggers();
        harness.handlePermanentChosen(player1, target.getId());
        resolveAllTriggers();

        castAngelOfMercy();
        resolveAllTriggers();
        harness.handlePermanentChosen(player1, target.getId());
        resolveAllTriggers();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, target, Keyword.INDESTRUCTIBLE)).isTrue();
    }
}

package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.s.StormCrow;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TheCloneSaga.class, Forest.class, GrizzlyBears.class, Island.class, StormCrow.class})
class TheCloneSagaTest extends BaseCardTest {

    @Test
    @DisplayName("Chapter I surveils three")
    void chapterISurveilsThree() {
        addSagaWithLore(0);
        Card first = new Forest();
        Card second = new Island();
        Card third = new Forest();
        harness.setLibrary(player1, List.of(first, second, third, new Island()));

        advanceToNextChapter();

        PendingInteraction.Scry surveil = gd.interaction.activeInteraction(PendingInteraction.Scry.class);
        assertThat(surveil).isNotNull();
        assertThat(surveil.cards()).hasSize(3);

        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(0, 1, 2), List.of()));
    }

    @Test
    @DisplayName("Chapter II copies the next creature spell and removes legendary")
    void chapterIICopiesNextCreatureSpellWithoutLegendary() {
        addSagaWithLore(1);
        advanceToNextChapter();

        Card legendaryCreature = new GrizzlyBears();
        legendaryCreature.setSupertypes(Set.of(CardSupertype.LEGENDARY));
        harness.setHand(player1, List.of(legendaryCreature));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        List<Permanent> bears = findPermanents(player1, "Grizzly Bears");
        assertThat(bears).hasSize(2);
        assertThat(bears).anyMatch(permanent -> !permanent.getCard().isToken()
                && permanent.getCard().getSupertypes().contains(CardSupertype.LEGENDARY));
        assertThat(bears).anyMatch(permanent -> permanent.getCard().isToken()
                && !permanent.getCard().getSupertypes().contains(CardSupertype.LEGENDARY));
    }

    @Test
    @DisplayName("Chapter III draws for a creature with the chosen name dealing combat damage")
    void chapterIIIDrawsForChosenCreatureName() {
        addSagaWithLore(2);
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new StormCrow());
        harness.setLibrary(player1, List.of(new Forest(), new Island()));

        advanceToNextChapter();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class)).isNotNull();
        harness.handleListChoice(player1, "Grizzly Bears");

        int handBeforeCombat = gd.playerHands.get(player1.getId()).size();
        declareAttackers(List.of(0, 1));
        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId()).size()).isEqualTo(handBeforeCombat + 1);
    }

    private Permanent addSagaWithLore(int loreCounters) {
        harness.addToBattlefield(player1, new TheCloneSaga());
        Permanent saga = findPermanent(player1, "The Clone Saga");
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

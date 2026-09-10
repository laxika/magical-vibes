package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.ProdigalSorcerer;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({OldFatSpiderCantSeeMe.class, GrizzlyBears.class, ProdigalSorcerer.class})
class OldFatSpiderCantSeeMeTest extends BaseCardTest {

    @Test
    @DisplayName("Chapter I grants hexproof to a creature you control while the Saga remains")
    void chapterIGrantsHexproofToYourCreature() {
        Permanent saga = addSaga(0);
        Permanent ownCreature = addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());

        triggerChapter();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validPermanentIds()).containsExactly(ownCreature.getId());

        harness.handlePermanentChosen(player1, ownCreature.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, ownCreature, Keyword.HEXPROOF)).isTrue();
        assertThat(saga.getCounterCount(CounterType.LORE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Chapter II prevents all damage dealt by the chosen creature")
    void chapterIIPreventsNoncombatDamageFromChosenCreature() {
        addSaga(1);
        Permanent sorcerer = addCreatureReady(player1, new ProdigalSorcerer());

        triggerChapter();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validPermanentIds()).containsExactly(sorcerer.getId());

        harness.handlePermanentChosen(player1, sorcerer.getId());
        harness.passBothPriorities();

        harness.setLife(player2, 20);
        int sorcererIndex = gd.playerBattlefields.get(player1.getId()).indexOf(sorcerer);
        harness.activateAbility(player1, sorcererIndex, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Chapters III and IV each draw a card")
    void chaptersIIIAndIVDrawCards() {
        addSaga(2);
        GrizzlyBears firstCard = new GrizzlyBears();
        ProdigalSorcerer secondCard = new ProdigalSorcerer();
        harness.setLibrary(player1, List.of(firstCard, secondCard));

        triggerChapter();
        harness.passBothPriorities();
        assertThat(gd.playerHands.get(player1.getId())).contains(firstCard);

        triggerChapter();
        harness.passBothPriorities();
        assertThat(gd.playerHands.get(player1.getId())).contains(secondCard);
        harness.assertNotOnBattlefield(player1, "Old Fat Spider Can't See Me");
    }

    private Permanent addSaga(int loreCounters) {
        Permanent saga = harness.addToBattlefieldAndReturn(player1, new OldFatSpiderCantSeeMe());
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

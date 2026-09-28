package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TheEleventhHour.class, TheTenthDoctor.class, Forest.class, GrizzlyBears.class})
class TheEleventhHourTest extends BaseCardTest {

    @Test
    void chapterISearchesForADoctor() {
        Card nonDoctor = new Forest();
        Card doctor = new TheTenthDoctor();
        harness.setLibrary(player1, List.of(nonDoctor, doctor));
        addSagaWithLore(0);

        advanceToNextChapter();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().cards()).containsExactly(doctor);

        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).contains(doctor);
    }

    @Test
    void chapterIICreatesFoodAndAHumanThatReducesCreatureSpellCosts() {
        addSagaWithLore(1);

        advanceToNextChapter();

        assertThat(findPermanents(player1, "Food")).hasSize(1);
        Permanent human = findPermanent(player1, "Human");
        assertThat(human.getCard().getSubtypes()).contains(CardSubtype.HUMAN);

        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Grizzly Bears")).hasSize(1);
    }

    @Test
    void chapterIIICreatesALegendaryAlienNamedPrisonerZero() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        addSagaWithLore(2);

        advanceToNextChapter();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validPermanentIds()).contains(target.getId()).doesNotContain(land.getId());

        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        Permanent copy = findPermanent(player1, "Prisoner Zero");
        assertThat(copy.getCard().isToken()).isTrue();
        assertThat(copy.getCard().getSubtypes()).contains(CardSubtype.ALIEN);
        assertThat(copy.getCard().getSupertypes()).contains(CardSupertype.LEGENDARY);
        assertThat(copy.getCard().hasType(CardType.CREATURE)).isTrue();
        assertThat(copy.getCard().getPower()).isEqualTo(2);
        assertThat(copy.getCard().getToughness()).isEqualTo(2);
    }

    private Permanent addSagaWithLore(int lore) {
        Permanent saga = harness.addToBattlefieldAndReturn(player1, new TheEleventhHour());
        saga.setCounterCount(CounterType.LORE, lore);
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

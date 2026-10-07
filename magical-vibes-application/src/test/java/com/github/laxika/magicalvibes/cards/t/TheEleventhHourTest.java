package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.a.AdiposeOffspring;
import com.github.laxika.magicalvibes.cards.f.Forest;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TheEleventhHour.class, TheTenthDoctor.class, Forest.class, AdiposeOffspring.class})
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
    void enteringSagaTriggersChapterIAndAllowsFailingToFind() {
        Card doctor = new TheTenthDoctor();
        harness.setLibrary(player1, List.of(doctor));
        harness.setHand(player1, List.of(new TheEleventhHour()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "The Eleventh Hour").getCounterCount(CounterType.LORE))
                .isEqualTo(1);
        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().cards()).containsExactly(doctor);
        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerDecks.get(player1.getId())).contains(doctor);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(doctor);
    }

    @Test
    void chapterIIFoodCanBeSacrificedForThreeLife() {
        addSagaWithLore(1);
        advanceToNextChapter();
        Permanent food = findPermanent(player1, "Food");
        int startingLife = gd.playerLifeTotals.get(player1.getId());
        int foodIndex = gd.playerBattlefields.get(player1.getId()).indexOf(food);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, foodIndex, null, null);
        harness.passBothPriorities();

        harness.assertLife(player1, startingLife + 3);
        harness.assertNotOnBattlefield(player1, "Food");
        harness.assertOnBattlefield(player1, "Human");
    }

    @Test
    void chapterIICreatesFoodAndAHumanThatReducesDoctorSpellCosts() {
        addSagaWithLore(1);

        advanceToNextChapter();

        assertThat(findPermanents(player1, "Food")).hasSize(1);
        Permanent human = findPermanent(player1, "Human");
        assertThat(human.getCard().getSubtypes()).contains(CardSubtype.HUMAN);

        harness.setHand(player1, List.of(new TheTenthDoctor()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "The Tenth Doctor");
    }

    @Test
    void humanDoesNotReduceNonDoctorCreatureSpellCosts() {
        addSagaWithLore(1);
        advanceToNextChapter();
        harness.setHand(player1, List.of(new AdiposeOffspring()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInHand(player1, "Adipose Offspring");
    }

    @Test
    void humanDoesNotReduceOpponentsDoctorSpellCosts() {
        addSagaWithLore(1);
        advanceToNextChapter();
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new TheTenthDoctor()));
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castCreature(player2, 0))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInHand(player2, "The Tenth Doctor");
    }

    @Test
    void chapterIIICreatesALegendaryAlienNamedPrisonerZero() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new TheTenthDoctor());
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
        assertThat(copy.getCard().getSubtypes()).containsExactly(CardSubtype.ALIEN);
        assertThat(copy.getCard().getSupertypes()).contains(CardSupertype.LEGENDARY);
        assertThat(copy.getCard().hasType(CardType.CREATURE)).isTrue();
        assertThat(copy.getCard().getPower()).isEqualTo(3);
        assertThat(copy.getCard().getToughness()).isEqualTo(5);
        harness.assertOnBattlefield(player2, "The Tenth Doctor");
        harness.assertInGraveyard(player1, "The Eleventh Hour");
    }

    private void addSagaWithLore(int lore) {
        Permanent saga = harness.addToBattlefieldAndReturn(player1, new TheEleventhHour());
        saga.setCounterCount(CounterType.LORE, lore);
    }

    private void advanceToNextChapter() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passUntil(player1, TurnStep.PRECOMBAT_MAIN);
        harness.passBothPriorities();
    }
}

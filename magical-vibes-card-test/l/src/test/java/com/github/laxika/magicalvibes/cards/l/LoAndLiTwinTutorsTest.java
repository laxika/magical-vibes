package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.b.BoomerangBasics;
import com.github.laxika.magicalvibes.cards.f.FirebendingLesson;
import com.github.laxika.magicalvibes.cards.h.HonestWork;
import com.github.laxika.magicalvibes.cards.m.MaiJadedEdge;
import com.github.laxika.magicalvibes.cards.p.PlatypusBear;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LoAndLiTwinTutors.class, BoomerangBasics.class, FirebendingLesson.class, HonestWork.class,
        MaiJadedEdge.class, PlatypusBear.class, LightningStrike.class})
class LoAndLiTwinTutorsTest extends BaseCardTest {

    @Test
    @DisplayName("ETB offers Lesson and Noble cards only")
    void etbOffersLessonAndNobleCards() {
        FirebendingLesson lesson = new FirebendingLesson();
        MaiJadedEdge noble = new MaiJadedEdge();
        LightningStrike nonMatchingSpell = new LightningStrike();
        PlatypusBear nonMatchingCreature = new PlatypusBear();
        harness.setLibrary(player1, List.of(nonMatchingSpell, lesson, nonMatchingCreature, noble));

        castTutors();

        List<Card> offered = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)
                .params().cards();
        assertThat(offered).containsExactlyInAnyOrder(lesson, noble);
        harness.handleCardChosen(player1, offered.indexOf(lesson));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(lesson);
        assertThat(gd.playerDecks.get(player1.getId()))
                .containsExactlyInAnyOrder(nonMatchingSpell, nonMatchingCreature, noble);
        assertThat(gameLogContains("reveals")).isTrue();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Noble creatures you control have lifelink")
    void nobleCreaturesHaveLifelink() {
        Permanent tutors = harness.addToBattlefieldAndReturn(player1, new LoAndLiTwinTutors());
        Permanent noble = harness.addToBattlefieldAndReturn(player1, new MaiJadedEdge());
        Permanent nonNoble = harness.addToBattlefieldAndReturn(player1, new PlatypusBear());
        Permanent opposingNoble = harness.addToBattlefieldAndReturn(player2, new MaiJadedEdge());

        assertThat(gqs.hasKeyword(gd, noble, Keyword.LIFELINK)).isTrue();
        assertThat(gqs.hasKeyword(gd, nonNoble, Keyword.LIFELINK)).isFalse();
        assertThat(gqs.hasKeyword(gd, tutors, Keyword.LIFELINK)).isFalse();
        assertThat(gqs.hasKeyword(gd, opposingNoble, Keyword.LIFELINK)).isFalse();
    }

    @Test
    @DisplayName("Only Lesson spells have lifelink")
    void onlyLessonSpellsHaveLifelink() {
        harness.addToBattlefield(player1, new LoAndLiTwinTutors());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new PlatypusBear());
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new FirebendingLesson()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, target.getId());
        assertThat(gqs.shouldControllerSpellHaveLifelink(gd, gd.stack.getLast())).isTrue();
        harness.passBothPriorities();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(22);

        harness.setHand(player1, List.of(new LightningStrike()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castInstant(player1, 0, player2.getId());
        assertThat(gqs.shouldControllerSpellHaveLifelink(gd, gd.stack.getLast())).isFalse();
        harness.passBothPriorities();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(22);
    }

    @Test
    void canFindANobleInsteadOfALesson() {
        MaiJadedEdge noble = new MaiJadedEdge();
        FirebendingLesson lesson = new FirebendingLesson();
        harness.setLibrary(player1, List.of(lesson, noble));
        castTutors();
        List<Card> offered = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)
                .params().cards();
        harness.handleCardChosen(player1, offered.indexOf(noble));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(noble);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(lesson);
        assertThat(gameLogContains("reveals")).isTrue();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void canFailToFindEvenWhenAMatchingCardExists() {
        FirebendingLesson lesson = new FirebendingLesson();
        harness.setLibrary(player1, List.of(lesson));
        castTutors();
        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(lesson);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void searchCompletesWhenThereAreNoMatchingCards() {
        LightningStrike spell = new LightningStrike();
        PlatypusBear creature = new PlatypusBear();
        harness.setLibrary(player1, List.of(spell, creature));
        castTutors();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(spell, creature);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void searchCompletesWithAnEmptyLibrary() {
        harness.setLibrary(player1, List.of());
        castTutors();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void opposingLessonDoesNotGainLifelink() {
        harness.addToBattlefield(player1, new LoAndLiTwinTutors());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new PlatypusBear());
        harness.setLife(player2, 20);
        harness.setHand(player2, List.of(new FirebendingLesson()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, target.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    @Test
    void losingAllAbilitiesStopsGrantingLifelinkToLessons() {
        Permanent tutors = harness.addToBattlefieldAndReturn(player1, new LoAndLiTwinTutors());
        Permanent noble = harness.addToBattlefieldAndReturn(player1, new MaiJadedEdge());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new PlatypusBear());
        harness.setHand(player2, List.of(new HonestWork()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.castEnchantment(player2, 0, tutors.getId());
        resolveAllTriggers();
        assertThat(gqs.hasKeyword(gd, noble, Keyword.LIFELINK)).isFalse();

        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new FirebendingLesson()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, target.getId());
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    @Test
    void removingTutorsBeforeLessonResolvesStopsLifelink() {
        Permanent tutors = harness.addToBattlefieldAndReturn(player1, new LoAndLiTwinTutors());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new PlatypusBear());
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new FirebendingLesson()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, target.getId());
        harness.setHand(player2, List.of(new LightningStrike()));
        harness.addMana(player2, ManaColor.RED, 2);
        harness.castInstant(player2, 0, tutors.getId());
        resolveAllTriggers();

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(tutors.getCard());
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    @Test
    void nonDamagingSorceryLessonHasLifelinkWithoutGainingLife() {
        harness.addToBattlefield(player1, new LoAndLiTwinTutors());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new PlatypusBear());
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new BoomerangBasics()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castSorcery(player1, 0, target.getId());
        assertThat(gqs.shouldControllerSpellHaveLifelink(gd, gd.stack.getLast())).isTrue();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).contains(target.getCard());
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    private void castTutors() {
        harness.castFromHand(player1, new LoAndLiTwinTutors(), "{4}{B}");
        resolveAllTriggers();
    }
}

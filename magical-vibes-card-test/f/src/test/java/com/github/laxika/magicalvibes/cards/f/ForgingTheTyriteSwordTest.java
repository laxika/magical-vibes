package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GoldveinPick;
import com.github.laxika.magicalvibes.cards.h.HalvarGodOfBattle;
import com.github.laxika.magicalvibes.model.Card;
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

@CardUsed({ForgingTheTyriteSword.class, FearlessPup.class, HalvarGodOfBattle.class,
        GoldveinPick.class})
class ForgingTheTyriteSwordTest extends BaseCardTest {

    @Test
    void chaptersOneAndTwoCreateTreasureTokens() {
        harness.setHand(player1, List.of(new ForgingTheTyriteSword()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Treasure")).hasSize(1);

        Permanent saga = findPermanent(player1, "Forging the Tyrite Sword");
        saga.setCounterCount(CounterType.LORE, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passUntil(player1, TurnStep.PRECOMBAT_MAIN);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Treasure")).hasSize(2);
    }

    @Test
    void chapterThreeSearchesForHalvarOrEquipment() {
        harness.addToBattlefield(player1, new ForgingTheTyriteSword());
        harness.setHand(player1, List.of());
        Permanent saga = findPermanent(player1, "Forging the Tyrite Sword");
        saga.setCounterCount(CounterType.LORE, 2);
        harness.setLibrary(player1, List.of(new FearlessPup(), new HalvarGodOfBattle(), new GoldveinPick()));

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passUntil(player1, TurnStep.PRECOMBAT_MAIN);
        resolveAllTriggers();

        PendingInteraction.LibrarySearch search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().reveals()).isTrue();
        assertThat(search.params().cards().stream().map(Card::getName))
                .containsExactlyInAnyOrder("Halvar, God of Battle", "Goldvein Pick");

        int equipmentIndex = search.params().cards().stream()
                .map(Card::getName)
                .toList()
                .indexOf("Goldvein Pick");
        harness.handleCardChosen(player1, equipmentIndex);

        harness.assertInHand(player1, "Goldvein Pick");
        harness.assertInGraveyard(player1, "Forging the Tyrite Sword");
        harness.assertNotOnBattlefield(player1, "Forging the Tyrite Sword");
        assertThat(gd.playerDecks.get(player1.getId()).stream().map(Card::getName))
                .containsExactlyInAnyOrder("Fearless Pup", "Halvar, God of Battle");
    }

    @Test
    void chapterThreeDoesNotSearchForUnmatchingCards() {
        harness.addToBattlefield(player1, new ForgingTheTyriteSword());
        harness.setHand(player1, List.of());
        Permanent saga = findPermanent(player1, "Forging the Tyrite Sword");
        saga.setCounterCount(CounterType.LORE, 2);
        harness.setLibrary(player1, List.of(new FearlessPup()));

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passUntil(player1, TurnStep.PRECOMBAT_MAIN);
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().getName().equals("Treasure"));
    }

    @Test
    void chapterThreeCanChooseHalvarAndKeepsSagaUntilSearchCompletes() {
        harness.addToBattlefield(player1, new ForgingTheTyriteSword());
        harness.setHand(player1, List.of());
        findPermanent(player1, "Forging the Tyrite Sword").setCounterCount(CounterType.LORE, 2);
        harness.setLibrary(player1, List.of(new HalvarGodOfBattle(), new FearlessPup()));

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passUntil(player1, TurnStep.PRECOMBAT_MAIN);
        resolveAllTriggers();

        PendingInteraction.LibrarySearch search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards().stream().map(Card::getName))
                .containsExactly("Halvar, God of Battle");
        harness.assertOnBattlefield(player1, "Forging the Tyrite Sword");
        harness.assertNotInGraveyard(player1, "Forging the Tyrite Sword");

        harness.handleCardChosen(player1, 0);

        harness.assertInHand(player1, "Halvar, God of Battle");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId()).stream().map(Card::getName))
                .containsExactly("Fearless Pup");
        harness.assertInGraveyard(player1, "Forging the Tyrite Sword");
        harness.assertNotOnBattlefield(player1, "Forging the Tyrite Sword");
    }

    @Test
    void chapterThreeCanFailToFindEvenWithMatchingCards() {
        harness.addToBattlefield(player1, new ForgingTheTyriteSword());
        harness.setHand(player1, List.of());
        findPermanent(player1, "Forging the Tyrite Sword").setCounterCount(CounterType.LORE, 2);
        harness.setLibrary(player1, List.of(new HalvarGodOfBattle(), new GoldveinPick()));
        harness.setLibrary(player2, List.of(new GoldveinPick()));

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passUntil(player1, TurnStep.PRECOMBAT_MAIN);
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNotNull();
        harness.handleCardChosen(player1, -1);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId()).stream().map(Card::getName))
                .containsExactlyInAnyOrder("Halvar, God of Battle", "Goldvein Pick");
        assertThat(gd.playerDecks.get(player2.getId()).stream().map(Card::getName))
                .containsExactly("Goldvein Pick");
        harness.assertInGraveyard(player1, "Forging the Tyrite Sword");
        harness.assertNotOnBattlefield(player1, "Forging the Tyrite Sword");
    }

    @Test
    void chapterThreeWithEmptyLibraryStillSacrificesSaga() {
        harness.addToBattlefield(player1, new ForgingTheTyriteSword());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of());
        findPermanent(player1, "Forging the Tyrite Sword").setCounterCount(CounterType.LORE, 2);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passUntil(player1, TurnStep.PRECOMBAT_MAIN);
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Forging the Tyrite Sword");
        harness.assertNotOnBattlefield(player1, "Forging the Tyrite Sword");
    }
}

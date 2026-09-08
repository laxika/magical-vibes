package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HalvarGodOfBattle;
import com.github.laxika.magicalvibes.cards.s.SwordOfTheRealms;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ForgingTheTyriteSword.class, GrizzlyBears.class, HalvarGodOfBattle.class,
        SwordOfTheRealms.class})
class ForgingTheTyriteSwordTest extends BaseCardTest {

    @Test
    void chaptersOneAndTwoCreateTreasureTokens() {
        harness.setHand(player1, List.of(new ForgingTheTyriteSword()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Treasure")).hasSize(1);

        Permanent saga = findPermanent(player1, "Forging the Tyrite Sword");
        saga.setCounterCount(CounterType.LORE, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Treasure")).hasSize(2);
    }

    @Test
    void chapterThreeSearchesForHalvarOrEquipment() {
        harness.addToBattlefield(player1, new ForgingTheTyriteSword());
        harness.setHand(player1, List.of());
        Permanent saga = findPermanent(player1, "Forging the Tyrite Sword");
        saga.setCounterCount(CounterType.LORE, 2);
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new HalvarGodOfBattle(), new SwordOfTheRealms()));

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().reveals()).isTrue();
        assertThat(search.params().cards().stream().map(Card::getName))
                .containsExactlyInAnyOrder("Halvar, God of Battle", "Sword of the Realms");

        int equipmentIndex = search.params().cards().stream()
                .map(Card::getName)
                .toList()
                .indexOf("Sword of the Realms");
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(equipmentIndex));

        harness.assertInHand(player1, "Sword of the Realms");
        assertThat(gd.playerDecks.get(player1.getId()).stream().map(Card::getName))
                .containsExactlyInAnyOrder("Grizzly Bears", "Halvar, God of Battle");
    }

    @Test
    void chapterThreeDoesNotSearchForUnmatchingCards() {
        harness.addToBattlefield(player1, new ForgingTheTyriteSword());
        harness.setHand(player1, List.of());
        Permanent saga = findPermanent(player1, "Forging the Tyrite Sword");
        saga.setCounterCount(CounterType.LORE, 2);
        harness.setLibrary(player1, List.of(new GrizzlyBears()));

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().getName().equals("Treasure"));
    }
}

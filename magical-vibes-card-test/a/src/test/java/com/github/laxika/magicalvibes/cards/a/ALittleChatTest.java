package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.s.SewerCrocodile;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ALittleChat.class, SewerCrocodile.class})
class ALittleChatTest extends BaseCardTest {

    @Test
    @DisplayName("Puts one of the top two cards into hand and the other on the bottom")
    void choosesOneCardForHandAndBottomsTheOther() {
        Card chosen = new SewerCrocodile();
        Card other = new SewerCrocodile();
        harness.setLibrary(player1, List.of(chosen, other));
        harness.setHand(player1, List.of(new ALittleChat()));
        addMana();

        harness.castAndResolveInstant(player1, 0);
        harness.handleMultipleCardsChosen(player1, List.of(chosen.getId()));

        assertThat(gd.playerHands.get(player1.getId())).contains(chosen);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(other);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(other);
    }

    @Test
    @DisplayName("Casualty copies the top-two-card selection")
    void casualtyCopiesSelection() {
        Permanent casualtyCreature = harness.addToBattlefieldAndReturn(player1, new SewerCrocodile());
        Card firstChosen = new SewerCrocodile();
        Card firstOther = new SewerCrocodile();
        Card secondChosen = new SewerCrocodile();
        Card secondOther = new SewerCrocodile();
        harness.setLibrary(player1, List.of(firstChosen, firstOther, secondChosen, secondOther));
        harness.setHand(player1, List.of(new ALittleChat()));
        addMana();

        harness.castInstantWithSacrifice(player1, 0, null, casualtyCreature.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(firstChosen.getId()));
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(secondChosen.getId()));

        assertThat(gd.playerHands.get(player1.getId())).contains(firstChosen, secondChosen);
        assertThat(gd.playerDecks.get(player1.getId()))
                .containsExactly(firstOther, secondOther);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getId().equals(casualtyCreature.getId()));
    }

    @Test
    @DisplayName("Puts the sole remaining library card into hand")
    void putsOnlyLibraryCardIntoHand() {
        Card remaining = new ALittleChat();
        harness.setLibrary(player1, List.of(remaining));
        harness.setHand(player1, List.of(new ALittleChat()));
        addMana();

        harness.castAndResolveInstant(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(remaining);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("An empty library does not cause a failed draw or a loss")
    void resolvesWithEmptyLibrary() {
        harness.setLibrary(player1, List.of());
        harness.setHand(player1, List.of(new ALittleChat()));
        addMana();

        harness.castAndResolveInstant(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
        assertThat(gd.status).isNotEqualTo(GameStatus.FINISHED);
    }

    @Test
    @DisplayName("Can choose the second card and bottoms the first below untouched cards")
    void choosesSecondCardAndPreservesUntouchedLibraryOrder() {
        Card first = new ALittleChat();
        Card second = new ALittleChat();
        Card untouched = new ALittleChat();
        harness.setLibrary(player1, List.of(first, second, untouched));
        harness.setHand(player1, List.of(new ALittleChat()));
        addMana();

        harness.castAndResolveInstant(player1, 0);
        harness.handleMultipleCardsChosen(player1, List.of(second.getId()));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(second);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(untouched, first);
    }

    @Test
    @DisplayName("Choosing a card for hand is mandatory")
    void cannotDeclineHandSelection() {
        Card first = new ALittleChat();
        Card second = new ALittleChat();
        harness.setLibrary(player1, List.of(first, second));
        harness.setHand(player1, List.of(new ALittleChat()));
        addMana();

        harness.castAndResolveInstant(player1, 0);

        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1, List.of()))
                .isInstanceOf(IllegalStateException.class);
        harness.handleMultipleCardsChosen(player1, List.of(first.getId()));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(first);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(second);
    }

    @Test
    @DisplayName("Casualty cannot sacrifice an opponent's creature")
    void cannotSacrificeOpponentsCreature() {
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new SewerCrocodile());
        Card spell = new ALittleChat();
        harness.setHand(player1, List.of(spell));
        addMana();

        assertThatThrownBy(() -> harness.castInstantWithSacrifice(player1, 0, null, opponentCreature.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(spell);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(opponentCreature);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Casualty may be declined even with an eligible creature")
    void canDeclineCasualtyWithEligibleCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new SewerCrocodile());
        Card first = new ALittleChat();
        Card second = new ALittleChat();
        harness.setLibrary(player1, List.of(first, second));
        harness.setHand(player1, List.of(new ALittleChat()));
        addMana();

        harness.castAndResolveInstant(player1, 0);
        harness.handleMultipleCardsChosen(player1, List.of(first.getId()));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(first);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(second);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(creature);
        assertThat(gd.stack).isEmpty();
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }
}

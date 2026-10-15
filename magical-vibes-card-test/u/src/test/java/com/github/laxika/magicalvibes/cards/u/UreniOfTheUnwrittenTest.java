package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.ShivanDragon;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({UreniOfTheUnwritten.class, ShivanDragon.class, GrizzlyBears.class, Forest.class})
class UreniOfTheUnwrittenTest extends BaseCardTest {

    @Test
    @DisplayName("ETB offers a Dragon creature from the top eight cards")
    void etbOffersDragonFromTopEight() {
        ShivanDragon dragon = new ShivanDragon();
        setLibrary(new GrizzlyBears(), new Forest(), new Forest(), new Forest(),
                new Forest(), new Forest(), new Forest(), dragon);

        harness.enterBattlefieldAndReturn(player1, new UreniOfTheUnwritten());
        harness.passBothPriorities();

        PendingInteraction.LibraryRevealChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(choice.validCardIds()).containsExactly(dragon.getId());
        assertThat(choice.maxCount()).isEqualTo(1);

        harness.handleMultipleCardsChosen(player1, List.of(dragon.getId()));

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(Permanent::getCard)
                .contains(dragon);
        assertThat(gd.playerDecks.get(player1.getId())).doesNotContain(dragon);
    }

    @Test
    @DisplayName("Attacking triggers the same Dragon placement ability")
    void attackOffersDragonFromTopEight() {
        Permanent ureni = addCreatureReady(player1, new UreniOfTheUnwritten());
        ShivanDragon dragon = new ShivanDragon();
        setLibrary(new Forest(), new GrizzlyBears(), new Forest(), new Forest(),
                new Forest(), new Forest(), new Forest(), dragon);

        declareAttackers(List.of(0));
        resolveAllTriggers();

        PendingInteraction.LibraryRevealChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(choice.validCardIds()).containsExactly(dragon.getId());
        assertThat(ureni.isAttacking()).isTrue();

        harness.handleMultipleCardsChosen(player1, List.of(dragon.getId()));

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(Permanent::getCard)
                .contains(dragon);
    }

    @Test
    @DisplayName("Choosing a Dragon bottoms only the other eight-card-window cards")
    void chosenDragonEntersUntappedAndRestGoBelowUntouchedCards() {
        ShivanDragon dragon = new ShivanDragon();
        ShivanDragon otherDragon = new ShivanDragon();
        List<Card> remaining = List.of(otherDragon, new GrizzlyBears(), new Forest(),
                new Forest(), new Forest(), new Forest(), new Forest());
        Forest ninth = new Forest();
        GrizzlyBears tenth = new GrizzlyBears();
        harness.setLibrary(player1, List.of(dragon, remaining.get(0), remaining.get(1),
                remaining.get(2), remaining.get(3), remaining.get(4), remaining.get(5),
                remaining.get(6), ninth, tenth));

        harness.enterBattlefieldAndReturn(player1, new UreniOfTheUnwritten());
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(dragon.getId()));

        Permanent enteredDragon = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard() == dragon).findFirst().orElseThrow();
        assertThat(enteredDragon.isTapped()).isFalse();
        assertThat(enteredDragon.isAttacking()).isFalse();
        List<Card> library = gd.playerDecks.get(player1.getId());
        assertThat(library).hasSize(9).startsWith(ninth, tenth);
        assertThat(library.subList(2, library.size())).containsExactlyInAnyOrderElementsOf(remaining);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Declining the Dragon still puts all looked-at cards on the bottom")
    void mayDeclineDragon() {
        ShivanDragon dragon = new ShivanDragon();
        List<Card> lookedAt = List.of(dragon, new Forest(), new Forest(), new Forest(),
                new Forest(), new Forest(), new Forest(), new GrizzlyBears());
        Forest ninth = new Forest();
        harness.setLibrary(player1, List.of(lookedAt.get(0), lookedAt.get(1), lookedAt.get(2),
                lookedAt.get(3), lookedAt.get(4), lookedAt.get(5), lookedAt.get(6),
                lookedAt.get(7), ninth));

        harness.enterBattlefieldAndReturn(player1, new UreniOfTheUnwritten());
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of());

        List<Card> library = gd.playerDecks.get(player1.getId());
        assertThat(library).hasSize(9).startsWith(ninth);
        assertThat(library.subList(1, library.size())).containsExactlyInAnyOrderElementsOf(lookedAt);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(Permanent::getCard).doesNotContain(dragon);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("A Dragon below the top eight cannot be chosen")
    void noMatchingCardsLeavesNinthCardOnTop() {
        List<Card> lookedAt = List.of(new GrizzlyBears(), new Forest(), new Forest(),
                new Forest(), new Forest(), new Forest(), new Forest(), new Forest());
        ShivanDragon ninth = new ShivanDragon();
        harness.setLibrary(player1, List.of(lookedAt.get(0), lookedAt.get(1), lookedAt.get(2),
                lookedAt.get(3), lookedAt.get(4), lookedAt.get(5), lookedAt.get(6),
                lookedAt.get(7), ninth));

        harness.enterBattlefieldAndReturn(player1, new UreniOfTheUnwritten());
        harness.passBothPriorities();

        List<Card> library = gd.playerDecks.get(player1.getId());
        assertThat(library).hasSize(9).startsWith(ninth);
        assertThat(library.subList(1, library.size())).containsExactlyInAnyOrderElementsOf(lookedAt);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(Permanent::getCard).doesNotContain(ninth);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("A library with fewer than eight cards still offers its Dragon")
    void shortLibraryStillOffersDragon() {
        ShivanDragon dragon = new ShivanDragon();
        Forest forest = new Forest();
        setLibrary(forest, dragon);

        harness.enterBattlefieldAndReturn(player1, new UreniOfTheUnwritten());
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(dragon.getId()));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(forest);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(Permanent::getCard).contains(dragon);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("An empty library resolves without a choice or drawing cards")
    void emptyLibraryResolvesWithoutChoice() {
        setLibrary();

        harness.enterBattlefieldAndReturn(player1, new UreniOfTheUnwritten());
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Only one Dragon may be chosen even when multiple Dragons are available")
    void cannotChooseMultipleDragons() {
        ShivanDragon first = new ShivanDragon();
        ShivanDragon second = new ShivanDragon();
        setLibrary(first, second);

        harness.enterBattlefieldAndReturn(player1, new UreniOfTheUnwritten());
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1,
                List.of(first.getId(), second.getId()))).isInstanceOf(IllegalStateException.class);
        harness.handleMultipleCardsChosen(player1, List.of(first.getId()));

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(Permanent::getCard).contains(first).doesNotContain(second);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(second);
    }

    @Test
    @DisplayName("The controller can look at non-Dragon cards before choosing a Dragon")
    void controllerSeesAllLookedAtCards() {
        ShivanDragon dragon = new ShivanDragon();
        GrizzlyBears bear = new GrizzlyBears();
        Forest forest = new Forest();
        setLibrary(dragon, bear, forest);
        harness.clearMessages();

        harness.enterBattlefieldAndReturn(player1, new UreniOfTheUnwritten());
        harness.passBothPriorities();
        String controllerMessages = String.join("\n", harness.getConn1().getSentMessages());
        String opponentMessages = String.join("\n", harness.getConn2().getSentMessages());
        harness.handleMultipleCardsChosen(player1, List.of(dragon.getId()));

        assertThat(controllerMessages).contains(dragon.getId().toString(),
                bear.getId().toString(), forest.getId().toString());
        assertThat(opponentMessages).doesNotContain(dragon.getId().toString(),
                bear.getId().toString(), forest.getId().toString());
    }

    private void setLibrary(Card... cards) {
        harness.setLibrary(player1, List.of(cards));
    }
}

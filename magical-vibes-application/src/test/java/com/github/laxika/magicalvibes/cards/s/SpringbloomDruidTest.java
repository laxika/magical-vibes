package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SpringbloomDruid.class, Forest.class, Mountain.class, GrizzlyBears.class})
class SpringbloomDruidTest extends BaseCardTest {

    @Test
    @DisplayName("Entering may sacrifice a land to search for up to two tapped basic lands")
    void enteringSacrificesLandAndSearchesForBasicLands() {
        Permanent sacrificedLand = harness.addToBattlefieldAndReturn(player1, new Forest());
        Forest forest = new Forest();
        Mountain mountain = new Mountain();
        GrizzlyBears bears = new GrizzlyBears();
        setLibrary(forest, mountain, bears);

        castSpringbloomDruid();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, sacrificedLand.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.LibrarySearch.class);
        chooseLibraryCard(0);
        chooseLibraryCard(0);

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(sacrificedLand.getCard());
        assertThat(findPermanent(forest).isTapped()).isTrue();
        assertThat(findPermanent(mountain).isTapped()).isTrue();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(bears);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Declining the entry ability does not sacrifice or search")
    void decliningEntryAbilityDoesNothing() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        Forest libraryForest = new Forest();
        setLibrary(libraryForest);

        castSpringbloomDruid();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerBattlefields.get(player1.getId())).extracting(Permanent::getCard)
                .contains(land.getCard());
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(libraryForest);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(land.getCard());
    }

    private void castSpringbloomDruid() {
        harness.setHand(player1, List.of(new SpringbloomDruid()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private void chooseLibraryCard(int index) {
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(index));
    }

    private void setLibrary(Card... cards) {
        List<Card> library = gd.playerDecks.get(player1.getId());
        library.clear();
        library.addAll(List.of(cards));
    }

    private Permanent findPermanent(Card card) {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard() == card)
                .findFirst()
                .orElseThrow();
    }
}

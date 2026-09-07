package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
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

@CardUsed({CycleOfRenewal.class, Forest.class, Island.class, Mountain.class, Plains.class, GrizzlyBears.class})
class CycleOfRenewalTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing a land searches for up to two basic lands that enter tapped")
    void sacrificesLandAndPutsTwoBasicLandsOntoBattlefieldTapped() {
        Permanent landToSacrifice = harness.addToBattlefieldAndReturn(player1, new Mountain());
        Card plains = new Plains();
        Card forest = new Forest();
        Card island = new Island();
        Card otherCard = new GrizzlyBears();
        setLibrary(plains, forest, island, otherCard);
        castSpell();

        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, landToSacrifice.getId());

        PendingInteraction.LibrarySearch search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().cards()).containsExactly(plains, forest, island);
        assertThat(search.params().remainingCount()).isEqualTo(2);
        assertThat(search.params().destination()).isEqualTo(LibrarySearchDestination.BATTLEFIELD_TAPPED);

        harness.getGameService().handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));
        harness.getGameService().handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(landToSacrifice.getCard());
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().hasType(CardType.LAND)
                        && permanent.getCard().getSupertypes().contains(CardSupertype.BASIC))
                .hasSize(2)
                .allMatch(Permanent::isTapped);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(island, otherCard);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("A nonland permanent cannot be sacrificed")
    void sacrificeChoiceContainsOnlyLands() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        castSpell();

        harness.passBothPriorities();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).containsExactly(land.getId());
        assertThat(choice.validIds()).doesNotContain(creature.getId());
    }

    @Test
    @DisplayName("The search can find fewer than two basic lands")
    void searchFindsAvailableBasicLands() {
        Permanent landToSacrifice = harness.addToBattlefieldAndReturn(player1, new Forest());
        Card plains = new Plains();
        Card nonbasicLand = new CycleOfRenewal();
        setLibrary(plains, nonbasicLand);
        castSpell();

        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, landToSacrifice.getId());
        harness.getGameService().handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(plains.getId()) && permanent.isTapped());
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Without a land, the spell does not search")
    void noLandToSacrificeDoesNothing() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        setLibrary(new Plains(), new Island());
        castSpell();

        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId()))
                .extracting(Card::getName)
                .containsExactlyInAnyOrder("Plains", "Island");
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().hasType(CardType.LAND));
    }

    private void castSpell() {
        harness.setHand(player1, List.of(new CycleOfRenewal()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.castInstant(player1, 0);
    }

    private void setLibrary(Card... cards) {
        List<Card> deck = gd.playerDecks.get(player1.getId());
        deck.clear();
        deck.addAll(List.of(cards));
    }
}

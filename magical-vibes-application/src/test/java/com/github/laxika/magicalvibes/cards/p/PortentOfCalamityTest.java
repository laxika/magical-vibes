package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.b.BarkformHarvester;
import com.github.laxika.magicalvibes.cards.c.CacheGrab;
import com.github.laxika.magicalvibes.cards.c.ClifftopLookout;
import com.github.laxika.magicalvibes.cards.d.DazzlingDenial;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.h.HeapedHarvest;
import com.github.laxika.magicalvibes.cards.i.InvasionOfZendikar;
import com.github.laxika.magicalvibes.cards.s.SazacapsBrew;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PortentOfCalamity.class, Forest.class, ClifftopLookout.class, DazzlingDenial.class,
        HeapedHarvest.class, CacheGrab.class, BarkformHarvester.class, SazacapsBrew.class, InvasionOfZendikar.class})
class PortentOfCalamityTest extends BaseCardTest {

    @Test
    @DisplayName("Offers one optional exile for each card type and continues after a decline")
    void offersOneCardPerType() {
        ClifftopLookout firstCreature = new ClifftopLookout();
        ClifftopLookout secondCreature = new ClifftopLookout();
        CacheGrab instant = new CacheGrab();
        HeapedHarvest artifact = new HeapedHarvest();
        Forest forest = new Forest();
        cast(5, List.of(forest, firstCreature, secondCreature, instant, artifact));

        assertThat(librarySearchCards()).containsExactly(forest);
        harness.handleCardChosen(player1, -1);

        assertThat(librarySearchCards()).containsExactlyInAnyOrder(firstCreature, secondCreature);
        harness.handleCardChosen(player1, 0);

        assertThat(librarySearchCards()).containsExactly(instant);
        harness.handleCardChosen(player1, 0);
        assertThat(librarySearchCards()).containsExactly(artifact);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerHands.get(player1.getId())).contains(firstCreature, instant, artifact);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(secondCreature, forest);
    }

    @Test
    @DisplayName("After exiling four cards, casts one spell for free and puts the rest into hand")
    void castsOneSpellAndReturnsTheRestToHand() {
        Forest forest = new Forest();
        ClifftopLookout creature = new ClifftopLookout();
        CacheGrab instant = new CacheGrab();
        HeapedHarvest artifact = new HeapedHarvest();
        cast(4, List.of(forest, creature, instant, artifact));

        chooseNextTypeCard();
        chooseNextTypeCard();
        chooseNextTypeCard();
        chooseNextTypeCard();

        PendingInteraction.ImprovisationCapstoneCastChoice castChoice =
                (PendingInteraction.ImprovisationCapstoneCastChoice) gd.interaction.activeInteraction();
        assertThat(castChoice.validCardIds()).containsExactlyInAnyOrder(
                creature.getId(), instant.getId(), artifact.getId());
        harness.handleMultipleCardsChosen(player1, List.of(artifact.getId()));

        assertThat(gd.stack).anyMatch(entry -> entry.getCard() == artifact);
        assertThat(gd.playerHands.get(player1.getId())).containsExactlyInAnyOrder(forest, creature, instant);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    @Test
    void zeroXLeavesLibraryUntouched() {
        Forest forest = new Forest();
        cast(0, List.of(forest));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(forest);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void revealsOnlyAvailableCardsWhenXExceedsLibrarySize() {
        ClifftopLookout creature = new ClifftopLookout();
        cast(5, List.of(creature));
        chooseNextTypeCard();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(creature);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void mayDeclineEveryType() {
        Forest forest = new Forest();
        ClifftopLookout creature = new ClifftopLookout();
        cast(2, List.of(forest, creature));
        harness.handleCardChosen(player1, -1);
        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(forest, creature);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void mayDeclineFreeCastAndPutAllFourCardsIntoHand() {
        Forest forest = new Forest();
        ClifftopLookout creature = new ClifftopLookout();
        CacheGrab instant = new CacheGrab();
        HeapedHarvest artifact = new HeapedHarvest();
        cast(4, List.of(forest, creature, instant, artifact));
        chooseNextTypeCard();
        chooseNextTypeCard();
        chooseNextTypeCard();
        chooseNextTypeCard();
        harness.handleMultipleCardsChosen(player1, List.of());

        assertThat(gd.playerHands.get(player1.getId())).containsExactlyInAnyOrder(forest, creature, instant, artifact);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void canExileDifferentArtifactCreaturesForDifferentTypes() {
        BarkformHarvester first = new BarkformHarvester();
        BarkformHarvester second = new BarkformHarvester();
        cast(2, List.of(first, second));
        chooseNextTypeCard();

        assertThat(librarySearchCards()).containsExactly(second);
        chooseNextTypeCard();

        assertThat(gd.playerHands.get(player1.getId())).containsExactlyInAnyOrder(first, second);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void uncastableSelectedSpellStillGoesIntoHand() {
        Forest forest = new Forest();
        ClifftopLookout creature = new ClifftopLookout();
        DazzlingDenial instant = new DazzlingDenial();
        HeapedHarvest artifact = new HeapedHarvest();
        cast(4, List.of(forest, creature, instant, artifact));
        chooseNextTypeCard();
        chooseNextTypeCard();
        chooseNextTypeCard();
        chooseNextTypeCard();
        harness.handleMultipleCardsChosen(player1, List.of(instant.getId()));

        assertThat(gd.playerHands.get(player1.getId())).containsExactlyInAnyOrder(forest, creature, instant, artifact);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void otherExiledCardsStayExiledWhileChoosingTargetsAndPayingCosts() {
        Forest forest = new Forest();
        Forest discard = new Forest();
        ClifftopLookout creature = new ClifftopLookout();
        SazacapsBrew brew = new SazacapsBrew();
        HeapedHarvest artifact = new HeapedHarvest();
        cast(4, List.of(forest, creature, brew, artifact), List.of(discard));
        chooseNextTypeCard();
        chooseNextTypeCard();
        chooseNextTypeCard();
        chooseNextTypeCard();
        harness.handleMultipleCardsChosen(player1, List.of(brew.getId()));

        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(discard);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(forest, creature, artifact);
    }

    @Test
    void battleCanBeChosenAsTheFreeSpell() {
        Forest forest = new Forest();
        ClifftopLookout creature = new ClifftopLookout();
        CacheGrab instant = new CacheGrab();
        InvasionOfZendikar battle = new InvasionOfZendikar();
        cast(4, List.of(forest, creature, instant, battle));
        chooseNextTypeCard();
        chooseNextTypeCard();
        chooseNextTypeCard();
        chooseNextTypeCard();

        PendingInteraction.ImprovisationCapstoneCastChoice castChoice =
                gd.interaction.activeInteraction(PendingInteraction.ImprovisationCapstoneCastChoice.class);
        assertThat(castChoice.validCardIds()).contains(battle.getId());
        harness.handleMultipleCardsChosen(player1, List.of(battle.getId()));

        assertThat(gd.stack).anyMatch(entry -> entry.getCard() == battle);
        assertThat(gd.playerHands.get(player1.getId())).containsExactlyInAnyOrder(forest, creature, instant);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    @Test
    void freeSpellWithXUsesZero() {
        Forest forest = new Forest();
        ClifftopLookout creature = new ClifftopLookout();
        PortentOfCalamity secondPortent = new PortentOfCalamity();
        HeapedHarvest artifact = new HeapedHarvest();
        Forest unrevealed = new Forest();
        cast(4, List.of(forest, creature, secondPortent, artifact, unrevealed));
        chooseNextTypeCard();
        chooseNextTypeCard();
        chooseNextTypeCard();
        chooseNextTypeCard();
        harness.handleMultipleCardsChosen(player1, List.of(secondPortent.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(unrevealed);
        assertThat(gd.playerHands.get(player1.getId())).containsExactlyInAnyOrder(forest, creature, artifact);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(secondPortent);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    private void cast(int xValue, List<Card> library) {
        cast(xValue, library, List.of());
    }

    private void cast(int xValue, List<Card> library, List<Card> otherHandCards) {
        harness.setLibrary(player1, library);
        List<Card> hand = new ArrayList<>();
        hand.add(new PortentOfCalamity());
        hand.addAll(otherHandCards);
        harness.setHand(player1, hand);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, xValue);
        harness.castSorcery(player1, 0, xValue);
        harness.passBothPriorities();
    }

    private List<Card> librarySearchCards() {
        return gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards();
    }

    private void chooseNextTypeCard() {
        harness.handleCardChosen(player1, 0);
    }
}

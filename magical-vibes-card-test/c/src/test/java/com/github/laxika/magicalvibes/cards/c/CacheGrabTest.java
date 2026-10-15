package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.b.BakersbaneDuo;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.s.ScalesOfShale;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CacheGrab.class, BakersbaneDuo.class, Forest.class, ScalesOfShale.class})
class CacheGrabTest extends BaseCardTest {

    @Test
    @DisplayName("Mills four cards and may return a permanent card to hand")
    void returnsSelectedPermanent() {
        Card returned = new BakersbaneDuo();
        setLibrary(returned, new Forest(), new Forest(), new Forest());

        castCacheGrab();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).contains(returned);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(4);
        harness.assertOnBattlefield(player1, "Food");
    }

    @Test
    @DisplayName("Creates a Food token when a Squirrel is controlled")
    void createsFoodForControlledSquirrel() {
        harness.addToBattlefield(player1, new BakersbaneDuo());
        setLibrary(new ScalesOfShale(), new ScalesOfShale(), new ScalesOfShale(), new ScalesOfShale());

        castCacheGrab();

        harness.assertOnBattlefield(player1, "Food");
    }

    @Test
    @DisplayName("Creates Food when returning another permanent while controlling a Squirrel")
    void createsFoodForControlledSquirrelAfterReturningOtherPermanent() {
        harness.addToBattlefield(player1, new BakersbaneDuo());
        setLibrary(new Forest(), new ScalesOfShale(), new ScalesOfShale(), new ScalesOfShale());

        castCacheGrab();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertOnBattlefield(player1, "Food");
    }

    @Test
    @DisplayName("Does not create Food without a Squirrel")
    void doesNotCreateFoodWithoutSquirrel() {
        setLibrary(new BakersbaneDuo(), new ScalesOfShale(), new ScalesOfShale(), new ScalesOfShale());

        castCacheGrab();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        harness.assertNotOnBattlefield(player1, "Food");
    }

    @Test
    void returnsLandWithoutCreatingFood() {
        Card land = new Forest();
        setLibrary(land, new ScalesOfShale(), new ScalesOfShale(), new ScalesOfShale());

        castCacheGrab();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(land);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(land).hasSize(4);
        harness.assertNotOnBattlefield(player1, "Food");
    }

    @Test
    void canDeclineFirstPermanentAndReturnSecond() {
        Card first = new Forest();
        Card second = new BakersbaneDuo();
        setLibrary(first, second, new ScalesOfShale(), new ScalesOfShale());

        castCacheGrab();
        harness.handleMayAbilityChosen(player1, false);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(second);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(first).doesNotContain(second);
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        harness.assertOnBattlefield(player1, "Food");
    }

    @Test
    void returningOnePermanentEndsTheChoice() {
        Card first = new Forest();
        Card second = new BakersbaneDuo();
        setLibrary(first, second, new Forest(), new Forest());

        castCacheGrab();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(first);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(second).hasSize(4);
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertNotOnBattlefield(player1, "Food");
    }

    @Test
    void decliningAllPermanentsStillCreatesFoodForControlledSquirrel() {
        harness.addToBattlefield(player1, new BakersbaneDuo());
        setLibrary(new Forest(), new Forest(), new ScalesOfShale(), new ScalesOfShale());

        castCacheGrab();
        harness.handleMayAbilityChosen(player1, false);
        harness.assertNotOnBattlefield(player1, "Food");
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(5);
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2);
        harness.assertOnBattlefield(player1, "Food");
    }

    @Test
    void opponentSquirrelDoesNotCreateFoodAndOnlyControllerMills() {
        harness.addToBattlefield(player2, new BakersbaneDuo());
        Card opponentCard = new Forest();
        harness.setLibrary(player2, List.of(opponentCard));
        setLibrary(new ScalesOfShale(), new ScalesOfShale(), new ScalesOfShale(), new ScalesOfShale());

        castCacheGrab();

        harness.assertNotOnBattlefield(player1, "Food");
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(opponentCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(5);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void millsOnlyFourAndCannotReturnPreviouslyGraveyardedSquirrel() {
        Card oldSquirrel = new BakersbaneDuo();
        Card fifthCard = new Forest();
        harness.setGraveyard(player1, List.of(oldSquirrel));
        setLibrary(new ScalesOfShale(), new ScalesOfShale(), new ScalesOfShale(), new ScalesOfShale(), fifthCard);

        castCacheGrab();

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(fifthCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(oldSquirrel).hasSize(6);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertNotOnBattlefield(player1, "Food");
    }

    @Test
    void canReturnSquirrelFromShortLibrary() {
        Card squirrel = new BakersbaneDuo();
        setLibrary(squirrel);

        castCacheGrab();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(squirrel);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
        harness.assertOnBattlefield(player1, "Food");
    }

    @Test
    void satisfyingBothSquirrelConditionsCreatesOnlyOneFood() {
        harness.addToBattlefield(player1, new BakersbaneDuo());
        Card returned = new BakersbaneDuo();
        setLibrary(returned, new ScalesOfShale(), new ScalesOfShale(), new ScalesOfShale());

        castCacheGrab();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(returned);
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2);
        harness.assertOnBattlefield(player1, "Food");
    }

    @Test
    void emptyLibraryStillCreatesOneFoodWhichCanBeSacrificedForLife() {
        harness.addToBattlefield(player1, new BakersbaneDuo());
        setLibrary();
        harness.setLife(player1, 10);

        castCacheGrab();

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2);
        harness.assertOnBattlefield(player1, "Food");
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 1, null, null);
        harness.assertNotOnBattlefield(player1, "Food");
        harness.assertLife(player1, 10);
        harness.passBothPriorities();
        harness.assertLife(player1, 13);
    }

    private void castCacheGrab() {
        harness.castFromHand(player1, new CacheGrab(), "{1}{G}");
        harness.passBothPriorities();
    }

    private void setLibrary(Card... cards) {
        harness.setLibrary(player1, List.of(cards));
    }
}

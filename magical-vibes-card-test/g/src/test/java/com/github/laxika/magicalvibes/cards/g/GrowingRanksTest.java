package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.c.CallOfTheConclave;
import com.github.laxika.magicalvibes.cards.e.EyesInTheSkies;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GrowingRanks.class, GrizzlyBears.class, CallOfTheConclave.class, EyesInTheSkies.class})
class GrowingRanksTest extends BaseCardTest {

    @Test
    @DisplayName("Populates during its controller's upkeep")
    void populatesDuringControllerUpkeep() {
        harness.addToBattlefield(player1, new GrowingRanks());
        harness.addToBattlefield(player1, creatureToken("Soldier Token"));

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(findPermanents(player1, "Soldier Token")).hasSize(2);
    }

    @Test
    @DisplayName("Does not populate during an opponent's upkeep")
    void doesNotPopulateDuringOpponentsUpkeep() {
        harness.addToBattlefield(player1, new GrowingRanks());
        harness.addToBattlefield(player1, creatureToken("Soldier Token"));

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Soldier Token")).hasSize(1);
    }

    @Test
    @DisplayName("Does nothing during its controller's upkeep without a creature token")
    void doesNothingWithoutCreatureToken() {
        harness.addToBattlefield(player1, new GrowingRanks());
        harness.addToBattlefield(player1, new GrizzlyBears());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(findPermanents(player1, "Grizzly Bears")).hasSize(1);
    }

    @Test
    @DisplayName("Lets its controller choose which creature token to copy")
    void choosesCreatureTokenToCopy() {
        harness.addToBattlefield(player1, new GrowingRanks());
        harness.addToBattlefield(player1, creatureToken("Soldier Token"));
        harness.addToBattlefield(player1, creatureToken("Saproling Token"));

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        Permanent soldier = findPermanent(player1, "Soldier Token");
        harness.handlePermanentChosen(player1, soldier.getId());

        assertThat(findPermanents(player1, "Soldier Token")).hasSize(2);
        assertThat(findPermanents(player1, "Saproling Token")).hasSize(1);
    }

    @Test
    @DisplayName("Does not copy an opponent's creature token")
    void doesNotCopyOpponentsToken() {
        harness.forceActivePlayer(player2);
        harness.castFromHand(player2, new CallOfTheConclave(), "{G}{W}");
        harness.passBothPriorities();
        harness.addToBattlefield(player1, new GrowingRanks());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Centaur")).isEmpty();
        assertThat(findPermanents(player2, "Centaur")).hasSize(1);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Copies the token's characteristics without its counters or tapped status")
    void copiesTokenWithoutCountersOrTappedStatus() {
        harness.castFromHand(player1, new CallOfTheConclave(), "{G}{W}");
        harness.passBothPriorities();
        harness.addToBattlefield(player1, new GrowingRanks());
        Permanent original = findPermanent(player1, "Centaur");

        advanceToUpkeep(player1);
        original.tap();
        original.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Centaur")).hasSize(2);
        Permanent copy = findPermanents(player1, "Centaur").stream()
                .filter(permanent -> !permanent.getId().equals(original.getId()))
                .findFirst().orElseThrow();
        assertThat(copy.getCard().isToken()).isTrue();
        assertThat(gqs.getEffectivePower(gd, copy)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, copy)).isEqualTo(3);
        assertThat(copy.getPlusOnePlusOneCounters()).isZero();
        assertThat(copy.isTapped()).isFalse();
        assertThat(original.getPlusOnePlusOneCounters()).isEqualTo(2);
        assertThat(original.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Can populate a token created in response to its upkeep trigger")
    void choosesTokenAtResolutionRatherThanTriggerTime() {
        harness.addToBattlefield(player1, new GrowingRanks());
        advanceToUpkeep(player1);
        assertThat(gd.stack).hasSize(1);

        harness.castFromHand(player1, new EyesInTheSkies(), "{3}{W}");
        harness.passBothPriorities();
        assertThat(findPermanents(player1, "Bird")).hasSize(2);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, findPermanent(player1, "Bird").getId());
        assertThat(findPermanents(player1, "Bird")).hasSize(3);
    }

    @Test
    @DisplayName("The upkeep trigger still populates after Growing Ranks leaves the battlefield")
    void triggerResolvesWithoutSource() {
        harness.castFromHand(player1, new CallOfTheConclave(), "{G}{W}");
        harness.passBothPriorities();
        Permanent ranks = harness.addToBattlefieldAndReturn(player1, new GrowingRanks());
        advanceToUpkeep(player1);
        gd.playerBattlefields.get(player1.getId()).remove(ranks);
        gd.playerGraveyards.get(player1.getId()).add(ranks.getCard());

        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Centaur")).hasSize(2);
        harness.assertNotOnBattlefield(player1, "Growing Ranks");
    }

    private static Card creatureToken(String name) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.CREATURE);
        card.setManaCost("");
        card.setColor(CardColor.GREEN);
        card.setPower(1);
        card.setToughness(1);
        card.setToken(true);
        return card;
    }
}

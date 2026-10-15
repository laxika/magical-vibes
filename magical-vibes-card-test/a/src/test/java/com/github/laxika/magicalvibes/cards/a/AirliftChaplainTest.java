package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.d.Disenchant;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AirliftChaplain.class, AmbushParatrooper.class, AeronautCavalry.class,
        Disenchant.class, Forest.class, Plains.class})
class AirliftChaplainTest extends BaseCardTest {

    @Test
    @DisplayName("ETB mills three and offers a milled Plains for the hand")
    void acceptsMilledPlains() {
        Plains plains = new Plains();
        setLibrary(plains, new Forest(), new Disenchant());

        Permanent chaplain = castAndResolveEtb();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).contains(plains);
        assertThat(chaplain.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("ETB offers a creature with mana value three or less")
    void acceptsLowManaValueCreature() {
        AmbushParatrooper bears = new AmbushParatrooper();
        setLibrary(bears, new AeronautCavalry(), new Forest());

        Permanent chaplain = castAndResolveEtb();

        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).contains(bears);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(Card::getName)
                .containsExactlyInAnyOrder("Aeronaut Cavalry", "Forest");
        assertThat(chaplain.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Declining the only eligible card puts a +1/+1 counter on Airlift Chaplain")
    void declinesEligibleCardAndGetsCounter() {
        Plains plains = new Plains();
        setLibrary(plains, new Forest(), new Disenchant());

        Permanent chaplain = castAndResolveEtb();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(plains);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(3);
        assertThat(chaplain.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("The counter is only put on the creature after all eligible offers are declined")
    void declinesAllEligibleCardsBeforeCounter() {
        setLibrary(new Plains(), new AmbushParatrooper(), new Forest());

        Permanent chaplain = castAndResolveEtb();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        assertThat(chaplain.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();

        harness.handleMayAbilityChosen(player1, false);

        assertThat(chaplain.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("No eligible milled card automatically puts a +1/+1 counter on Airlift Chaplain")
    void noEligibleCardGetsCounter() {
        setLibrary(new Forest(), new Disenchant(), new AeronautCavalry());

        Permanent chaplain = castAndResolveEtb();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(chaplain.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(3);
    }

    @Test
    @DisplayName("Accepting one eligible card leaves all other milled cards in the graveyard")
    void returnsOnlyOneOfMultipleEligibleCards() {
        Plains plains = new Plains();
        AmbushParatrooper creature = new AmbushParatrooper();
        setLibrary(plains, creature, new Forest());

        Permanent chaplain = castAndResolveEtb();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(plains);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(creature).hasSize(2);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(chaplain.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("A player can decline the first eligible card and return the second")
    void returnsSecondEligibleCard() {
        Plains plains = new Plains();
        AirliftChaplain milledChaplain = new AirliftChaplain();
        setLibrary(plains, milledChaplain, new Forest());

        Permanent chaplain = castAndResolveEtb();
        harness.handleMayAbilityChosen(player1, false);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(milledChaplain);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(plains).hasSize(2);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(chaplain.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("A short library is milled completely and still allows returning a card")
    void shortLibraryStillReturnsCard() {
        Plains plains = new Plains();
        setLibrary(plains);

        Permanent chaplain = castAndResolveEtb();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(plains);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(chaplain.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("An empty library still gives the counter")
    void emptyLibraryGetsCounter() {
        setLibrary();

        Permanent chaplain = castAndResolveEtb();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(chaplain.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Eligible cards already in the graveyard cannot be returned")
    void cannotReturnPreviouslyGraveyardedCard() {
        Plains oldPlains = new Plains();
        harness.setGraveyard(player1, List.of(oldPlains));
        setLibrary(new Forest(), new Disenchant(), new AeronautCavalry());

        Permanent chaplain = castAndResolveEtb();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(oldPlains).hasSize(4);
        assertThat(chaplain.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    private Permanent castAndResolveEtb() {
        harness.castFromHand(player1, new AirliftChaplain(), "{2}{W}");
        resolveAllTriggers();
        return findPermanent(player1, "Airlift Chaplain");
    }

    private void setLibrary(Card... cards) {
        harness.setLibrary(player1, List.of(cards));
    }
}

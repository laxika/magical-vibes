package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.c.CenoteScout;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({NicanzilCurrentConductor.class, CenoteScout.class, Forest.class})
class NicanzilCurrentConductorTest extends BaseCardTest {

    @Test
    @DisplayName("Exploring a land lets Nicanzil put a land from hand onto the battlefield tapped")
    void landExplorePutsLandFromHandTapped() {
        Permanent nicanzil = harness.addToBattlefieldAndReturn(player1, new NicanzilCurrentConductor());
        Card exploredCard = new Forest();
        prepareExplore(exploredCard);

        resolveExploreTrigger();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() == exploredCard && permanent.isTapped());
        assertThat(nicanzil.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Exploring a nonland puts a +1/+1 counter on Nicanzil")
    void nonlandExplorePutsCounterOnNicanzil() {
        Permanent nicanzil = harness.addToBattlefieldAndReturn(player1, new NicanzilCurrentConductor());
        Card exploredCard = new CenoteScout();
        prepareExplore(exploredCard);

        resolveExploreTrigger();
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();

        assertThat(nicanzil.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard() == exploredCard);
    }

    @Test
    @DisplayName("The land placement may be declined")
    void landPlacementCanBeDeclined() {
        Permanent nicanzil = harness.addToBattlefieldAndReturn(player1, new NicanzilCurrentConductor());
        Card land = new Forest();
        prepareExplore(land);

        resolveExploreTrigger();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(land);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard() == land);
        assertThat(nicanzil.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("A land already in hand may be put onto the battlefield instead of the explored land")
    void mayPutDifferentLandFromHand() {
        harness.addToBattlefield(player1, new NicanzilCurrentConductor());
        Card exploredLand = new Forest();
        Card originalLand = new Forest();
        prepareExplore(exploredLand);
        harness.setHand(player1, List.of(originalLand));

        resolveExploreTrigger();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(exploredLand);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() == originalLand && permanent.isTapped())
                .noneMatch(permanent -> permanent.getCard() == exploredLand);
    }

    @Test
    @DisplayName("Putting the explored nonland into the graveyard still puts a counter on Nicanzil")
    void graveyardChoiceStillTriggersCounter() {
        Permanent nicanzil = harness.addToBattlefieldAndReturn(player1, new NicanzilCurrentConductor());
        Card exploredCard = new CenoteScout();
        prepareExplore(exploredCard);

        resolveExploreTrigger();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(exploredCard);
        assertThat(nicanzil.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Exploring an empty library does not trigger either Nicanzil ability")
    void emptyLibraryDoesNotTriggerNicanzil() {
        Permanent nicanzil = harness.addToBattlefieldAndReturn(player1, new NicanzilCurrentConductor());
        harness.setLibrary(player1, List.of());
        harness.setHand(player1, List.of(new CenoteScout(), new Forest()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(nicanzil.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard() instanceof CenoteScout)
                .singleElement().satisfies(permanent ->
                        assertThat(permanent.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1));
    }

    @Test
    @DisplayName("An opponent's creature exploring does not trigger Nicanzil")
    void opponentExploreDoesNotTriggerNicanzil() {
        Permanent nicanzil = harness.addToBattlefieldAndReturn(player1, new NicanzilCurrentConductor());
        harness.forceActivePlayer(player2);
        harness.setLibrary(player2, List.of(new CenoteScout()));
        harness.setHand(player2, List.of(new CenoteScout()));
        harness.addMana(player2, ManaColor.GREEN, 1);

        harness.castCreature(player2, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, false);

        assertThat(nicanzil.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    private void prepareExplore(Card exploredCard) {
        harness.setLibrary(player1, List.of(exploredCard));
        harness.setHand(player1, List.of(new CenoteScout()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
    }

    private void resolveExploreTrigger() {
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

}

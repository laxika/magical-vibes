package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LavaAxe;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FrenziedGeistblaster.class, GrizzlyBears.class, Shock.class, Forest.class, LavaAxe.class})
class FrenziedGeistblasterTest extends BaseCardTest {

    @Test
    void discardingSeeksAnInstantOrSorceryWhenThresholdIsMet() {
        FrenziedGeistblaster blaster = new FrenziedGeistblaster();
        GrizzlyBears discard = new GrizzlyBears();
        Shock sought = new Shock();
        Forest notSought = new Forest();
        setUp(blaster, discard, 10, 9, List.of(sought, notSought));

        castBlaster();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.playerHands.get(player1.getId())).contains(sought);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(notSought);
    }

    @Test
    void decliningDoesNotDiscardOrSeek() {
        FrenziedGeistblaster blaster = new FrenziedGeistblaster();
        GrizzlyBears discard = new GrizzlyBears();
        Shock sought = new Shock();
        Forest notSought = new Forest();
        setUp(blaster, discard, 10, 9, List.of(sought, notSought));

        castBlaster();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(11).contains(discard);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(sought, notSought);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(9);
    }

    @Test
    void thresholdCountsOnlyInstantAndSorceryCards() {
        FrenziedGeistblaster blaster = new FrenziedGeistblaster();
        GrizzlyBears discard = new GrizzlyBears();
        Shock sought = new Shock();
        Forest notSought = new Forest();
        setUp(blaster, discard, 9, 9, List.of(sought, notSought));

        castBlaster();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).contains(discard);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(sought, notSought);
    }

    @Test
    void seekingCompletesDuringTheDiscardAbilityResolution() {
        Shock sought = new Shock();
        setUp(new FrenziedGeistblaster(), new GrizzlyBears(), 10, 9, List.of(sought));
        castBlaster();

        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).contains(sought);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void sorceriesCountTowardThresholdAndCanBeSought() {
        LavaAxe sought = new LavaAxe();
        setUp(new FrenziedGeistblaster(), new GrizzlyBears(), 10, 9, List.of(sought));
        castBlaster();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(sought);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    void discardingStillHappensWhenLibraryHasNoMatchingCard() {
        Forest land = new Forest();
        setUp(new FrenziedGeistblaster(), new GrizzlyBears(), 10, 10, List.of(land));
        castBlaster();

        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(10);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(land);
    }

    @Test
    void thresholdIsRecheckedWhenTheTriggerResolves() {
        GrizzlyBears discard = new GrizzlyBears();
        Shock sought = new Shock();
        setUp(new FrenziedGeistblaster(), discard, 10, 9, List.of(sought));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        assertThat(gd.stack).hasSize(1);

        harness.setGraveyard(player1, shocks(8));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).contains(discard);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(sought);
    }

    @Test
    void opponentsCardsDoNotCountTowardThreshold() {
        setUp(new FrenziedGeistblaster(), new GrizzlyBears(), 9, 9, List.of(new Shock()));
        harness.setGraveyard(player2, shocks(20));
        castBlaster();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void prowessBoostsForOwnNoncreatureSpellUntilEndOfTurn() {
        var blaster = harness.addToBattlefieldAndReturn(player1, new FrenziedGeistblaster());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, blaster)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, blaster)).isEqualTo(3);

        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(gqs.getEffectivePower(gd, blaster)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, blaster)).isEqualTo(2);
    }

    private void setUp(FrenziedGeistblaster blaster, GrizzlyBears discard, int handSpellCount,
                       int graveyardSpellCount, List<Card> library) {
        List<Card> hand = new ArrayList<>();
        hand.add(blaster);
        hand.add(discard);
        hand.addAll(shocks(handSpellCount));
        harness.setHand(player1, hand);
        harness.setGraveyard(player1, shocks(graveyardSpellCount));
        harness.setLibrary(player1, library);
    }

    private void castBlaster() {
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private List<Card> shocks(int count) {
        List<Card> cards = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            cards.add(new Shock());
        }
        return cards;
    }
}

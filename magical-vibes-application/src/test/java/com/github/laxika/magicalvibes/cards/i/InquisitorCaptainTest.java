package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.b.BeskirShieldmate;
import com.github.laxika.magicalvibes.cards.f.FearlessPup;
import com.github.laxika.magicalvibes.cards.l.LurkerInTheDeep;
import com.github.laxika.magicalvibes.cards.p.PoisonTheCup;
import com.github.laxika.magicalvibes.cards.s.StarnheimCourser;
import com.github.laxika.magicalvibes.cards.w.WeatheredRunestone;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({InquisitorCaptain.class, BeskirShieldmate.class, FearlessPup.class,
        WeatheredRunestone.class, LurkerInTheDeep.class, PoisonTheCup.class, StarnheimCourser.class})
class InquisitorCaptainTest extends BaseCardTest {

    @Test
    void seeksTwoEligibleCreaturesAndPutsTheChosenOneOntoBattlefield() {
        InquisitorCaptain captain = new InquisitorCaptain();
        FearlessPup soughtPup = new FearlessPup();
        BeskirShieldmate soughtShieldmate = new BeskirShieldmate();
        setUpThreshold(captain, 9, 9, List.of(soughtPup, soughtShieldmate));

        castCaptain();

        PendingInteraction.LibraryRevealChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(choice.validCardIds()).containsExactlyInAnyOrder(
                soughtPup.getId(), soughtShieldmate.getId());

        harness.handleMultipleCardsChosen(player1, List.of(soughtPup.getId()));

        assertThat(countPermanents(player1, "Fearless Pup")).isEqualTo(1);
        assertThat(countPermanents(player1, "Beskir Shieldmate")).isZero();
        assertThat(gd.playerDecks.get(player1.getId())).contains(soughtShieldmate);
    }

    @Test
    void doesNotSeekWithFewerThanTwentyEligibleCards() {
        InquisitorCaptain captain = new InquisitorCaptain();
        FearlessPup soughtPup = new FearlessPup();
        BeskirShieldmate soughtShieldmate = new BeskirShieldmate();
        setUpThreshold(captain, 8, 9, List.of(soughtPup, soughtShieldmate));

        castCaptain();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(countPermanents(player1, "Fearless Pup")).isZero();
        assertThat(gd.playerDecks.get(player1.getId()))
                .containsExactly(soughtPup, soughtShieldmate);
    }

    @Test
    void doesNotSeekWhenItEntersWithoutBeingCast() {
        InquisitorCaptain captain = new InquisitorCaptain();
        setUpThreshold(captain, 9, 9, List.of(new FearlessPup(), new BeskirShieldmate()));

        harness.enterBattlefieldAndReturn(player1, captain);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(countPermanents(player1, "Fearless Pup")).isZero();
    }

    @Test
    void rechecksThresholdWhenTheTriggerResolves() {
        FearlessPup pup = new FearlessPup();
        BeskirShieldmate shieldmate = new BeskirShieldmate();
        setUpThreshold(new InquisitorCaptain(), 9, 9, List.of(pup, shieldmate));
        castCaptainSpell();
        harness.passBothPriorities();
        assertThat(gd.stack).hasSize(1);

        harness.setGraveyard(player1, cards(8));
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(pup, shieldmate);
        assertThat(countPermanents(player1, "Fearless Pup")).isZero();
    }

    @Test
    void doesNotTriggerBelowThresholdEvenIfThresholdLaterIncreases() {
        setUpThreshold(new InquisitorCaptain(), 8, 9,
                List.of(new FearlessPup(), new BeskirShieldmate()));
        castCaptainSpell();
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.setGraveyard(player1, cards(10));
        resolveAllTriggers();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(2);
    }

    @Test
    void putsTheOnlyEligibleLibraryCardOntoBattlefield() {
        StarnheimCourser courser = new StarnheimCourser();
        InquisitorCaptain ineligibleCreature = new InquisitorCaptain();
        PoisonTheCup ineligibleSpell = new PoisonTheCup();
        setUpThreshold(new InquisitorCaptain(), 9, 10,
                List.of(ineligibleCreature, courser, ineligibleSpell));

        castCaptain();

        assertThat(gd.interaction.activeInteraction()).isNull();
        Permanent entered = findPermanent(player1, "Starnheim Courser");
        assertThat(entered.getCard().getId()).isEqualTo(courser.getId());
        assertThat(entered.isTapped()).isFalse();
        assertThat(entered.isSummoningSick()).isTrue();
        assertThat(gd.playerDecks.get(player1.getId()))
                .containsExactly(ineligibleCreature, ineligibleSpell);
    }

    @Test
    void doesNothingWhenThresholdIsMetButLibraryHasNoEligibleCards() {
        InquisitorCaptain ineligibleCreature = new InquisitorCaptain();
        PoisonTheCup ineligibleSpell = new PoisonTheCup();
        setUpThreshold(new InquisitorCaptain(), 10, 10,
                List.of(ineligibleCreature, ineligibleSpell));

        castCaptain();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId()))
                .containsExactly(ineligibleCreature, ineligibleSpell);
        assertThat(countPermanents(player1, "Inquisitor Captain")).isEqualTo(1);
    }

    @Test
    void doesNothingWithAnEmptyLibrary() {
        setUpThreshold(new InquisitorCaptain(), 10, 10, List.of());

        castCaptain();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(countPermanents(player1, "Inquisitor Captain")).isEqualTo(1);
    }

    @Test
    void doesNotCountOpponentsCardsOrBattlefieldCreatures() {
        setUpThreshold(new InquisitorCaptain(), 8, 9,
                List.of(new FearlessPup(), new BeskirShieldmate()));
        harness.setHand(player2, cards(20));
        harness.setGraveyard(player2, cards(20));
        harness.setLibrary(player2, cards(20));
        addCreatureReady(player1, new FearlessPup());

        castCaptain();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(2);
        assertThat(countPermanents(player1, "Fearless Pup")).isEqualTo(1);
    }

    @Test
    void doesNotCountNoncreaturesOrCreaturesWithManaValueAboveThree() {
        setUpThreshold(new InquisitorCaptain(), 8, 9,
                List.of(new FearlessPup(), new BeskirShieldmate(),
                        new InquisitorCaptain(), new PoisonTheCup()));

        castCaptain();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(4);
        assertThat(countPermanents(player1, "Fearless Pup")).isZero();
    }

    @Test
    void seeksAndPutsACreatureFromHandOntoBattlefieldDespiteWeatheredRunestone() {
        FearlessPup pup = new FearlessPup();
        BeskirShieldmate shieldmate = new BeskirShieldmate();
        setUpThreshold(new InquisitorCaptain(), 9, 9, List.of(pup, shieldmate));
        harness.addToBattlefield(player2, new WeatheredRunestone());

        castCaptain();

        PendingInteraction.LibraryRevealChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactlyInAnyOrder(pup.getId(), shieldmate.getId());
        harness.handleMultipleCardsChosen(player1, List.of(pup.getId()));

        assertThat(countPermanents(player1, "Fearless Pup")).isEqualTo(1);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(shieldmate);
    }

    @Test
    void triggersLurkerOnceForBothSoughtCards() {
        FearlessPup pup = new FearlessPup();
        BeskirShieldmate shieldmate = new BeskirShieldmate();
        setUpThreshold(new InquisitorCaptain(), 9, 9, List.of(pup, shieldmate));
        harness.addToBattlefield(player1, new LurkerInTheDeep());

        castCaptain();
        harness.handleMultipleCardsChosen(player1, List.of(pup.getId()));
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(Permanent::isManifested).toList()).hasSize(2);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(pup.getId()));
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(shieldmate);
    }

    @Test
    void soughtCardsGoToHandBeforeTheBattlefieldChoice() {
        FearlessPup pup = new FearlessPup();
        BeskirShieldmate shieldmate = new BeskirShieldmate();
        setUpThreshold(new InquisitorCaptain(), 9, 9, List.of(pup, shieldmate));

        castCaptain();

        assertThat(gd.playerHands.get(player1.getId())).contains(pup, shieldmate);
        harness.handleMultipleCardsChosen(player1, List.of(pup.getId()));
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(pup, shieldmate);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(pup.getId()));
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(shieldmate);
    }

    @Test
    void stillResolvesAfterCaptainIsDestroyedInResponse() {
        FearlessPup pup = new FearlessPup();
        BeskirShieldmate shieldmate = new BeskirShieldmate();
        setUpThreshold(new InquisitorCaptain(), 9, 9, List.of(pup, shieldmate));
        castCaptainSpell();
        harness.passBothPriorities();
        harness.setHand(player2, List.of(new PoisonTheCup()));
        harness.addMana(player2, ManaColor.BLACK, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, findPermanent(player1, "Inquisitor Captain").getId());
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Inquisitor Captain")).isZero();
        PendingInteraction.LibraryRevealChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactlyInAnyOrder(pup.getId(), shieldmate.getId());
        harness.handleMultipleCardsChosen(player1, List.of(shieldmate.getId()));
        assertThat(countPermanents(player1, "Beskir Shieldmate")).isEqualTo(1);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(pup);
    }

    private void setUpThreshold(InquisitorCaptain captain, int handCount, int graveyardCount,
                                List<Card> libraryCards) {
        List<Card> hand = new ArrayList<>();
        hand.add(captain);
        hand.addAll(cards(handCount));
        harness.setHand(player1, hand);
        harness.setGraveyard(player1, cards(graveyardCount));
        harness.setLibrary(player1, libraryCards);
    }

    private void castCaptain() {
        castCaptainSpell();
        resolveAllTriggers();
    }

    private void castCaptainSpell() {
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreature(player1, 0);
    }

    private List<Card> cards(int count) {
        List<Card> cards = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            cards.add(new FearlessPup());
        }
        return cards;
    }
}

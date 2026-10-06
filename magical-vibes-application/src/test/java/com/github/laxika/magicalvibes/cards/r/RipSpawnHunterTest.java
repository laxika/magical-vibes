package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.p.PossessedGoat;
import com.github.laxika.magicalvibes.cards.t.Tarmogoyf;
import com.github.laxika.magicalvibes.cards.u.UnidentifiedHovership;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RipSpawnHunter.class, GrizzlyBears.class, HillGiant.class, GiantSpider.class, Forest.class,
        PossessedGoat.class, UnidentifiedHovership.class, Tarmogoyf.class, RelentlessAssault.class})
class RipSpawnHunterTest extends BaseCardTest {

    @Test
    void tappedSurvivorRevealsPowerCountAndOffersDistinctPowerCards() {
        Permanent rip = harness.addToBattlefieldAndReturn(player1, new RipSpawnHunter());
        Card bears = new GrizzlyBears();
        Card hillGiant = new HillGiant();
        Card spider = new GiantSpider();
        Card nonCreature = new Forest();
        Card staysInLibrary = new Forest();
        rip.tap();
        harness.setLibrary(player1, List.of(bears, hillGiant, spider, nonCreature, staysInLibrary));

        advanceToPostcombatMain();
        harness.passBothPriorities();

        PendingInteraction.LibraryRevealChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.allCards()).containsExactly(bears, hillGiant, spider, nonCreature);
        assertThat(choice.validCardIds()).containsExactly(bears.getId(), hillGiant.getId(), spider.getId());

        harness.handleMultipleCardsChosen(player1, List.of(bears.getId(), hillGiant.getId()));

        assertThat(gd.playerHands.get(player1.getId())).contains(bears, hillGiant);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(spider, nonCreature, staysInLibrary);
    }

    @Test
    void duplicatePowerSelectionIsRejected() {
        Permanent rip = harness.addToBattlefieldAndReturn(player1, new RipSpawnHunter());
        Card firstBears = new GrizzlyBears();
        Card secondBears = new GrizzlyBears();
        Card hillGiant = new HillGiant();
        Card nonCreature = new Forest();
        rip.tap();
        harness.setLibrary(player1, List.of(firstBears, secondBears, hillGiant, nonCreature));

        advanceToPostcombatMain();
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(
                player1, List.of(firstBears.getId(), secondBears.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("different powers");
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class))
                .isNotNull();

        harness.handleMultipleCardsChosen(player1, List.of());
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(
                firstBears, secondBears, hillGiant, nonCreature);
    }

    @Test
    void untappedSurvivorDoesNotTrigger() {
        harness.addToBattlefieldAndReturn(player1, new RipSpawnHunter());
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new Forest()));

        advanceToPostcombatMain();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void vehicleAndCreatureWithDifferentPowersCanBothBeSelected() {
        Permanent rip = harness.addToBattlefieldAndReturn(player1, new RipSpawnHunter());
        Card goat = new PossessedGoat();
        Card vehicle = new UnidentifiedHovership();
        Card land = new Forest();
        rip.tap();
        harness.setLibrary(player1, List.of(goat, vehicle, land));

        advanceToPostcombatMain();
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(goat.getId(), vehicle.getId()));

        assertThat(gd.playerHands.get(player1.getId())).contains(goat, vehicle);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(land);
    }

    @Test
    void untappingBeforeResolutionPreventsReveal() {
        Permanent rip = harness.addToBattlefieldAndReturn(player1, new RipSpawnHunter());
        Card goat = new PossessedGoat();
        rip.tap();
        harness.setLibrary(player1, List.of(goat));

        advanceToPostcombatMain();
        assertThat(gd.stack).hasSize(1);
        rip.untap();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(goat);
    }

    @Test
    void revealCountUsesPowerAtResolution() {
        Permanent rip = harness.addToBattlefieldAndReturn(player1, new RipSpawnHunter());
        List<Card> library = List.of(new PossessedGoat(), new Forest(), new Forest(),
                new Forest(), new Forest(), new Forest());
        rip.tap();
        harness.setLibrary(player1, library);

        advanceToPostcombatMain();
        rip.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.passBothPriorities();

        PendingInteraction.LibraryRevealChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.allCards()).containsExactlyElementsOf(library.subList(0, 5));
        harness.handleMultipleCardsChosen(player1, List.of());
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(library.get(5));
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrderElementsOf(library);
    }

    @Test
    void noEligibleCardsAreBottomedWithoutAChoice() {
        Permanent rip = harness.addToBattlefieldAndReturn(player1, new RipSpawnHunter());
        List<Card> library = List.of(new Forest(), new Forest(), new Forest(), new Forest(), new Forest());
        rip.tap();
        harness.setLibrary(player1, library);

        advanceToPostcombatMain();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(library.get(4));
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrderElementsOf(library);
    }

    @Test
    void characteristicDefinedPowerMustBeUsedForDistinctness() {
        Permanent rip = harness.addToBattlefieldAndReturn(player1, new RipSpawnHunter());
        Card goyf = new Tarmogoyf();
        Card goat = new PossessedGoat();
        rip.tap();
        harness.setGraveyard(player2, List.of(new Forest()));
        harness.setLibrary(player1, List.of(goyf, goat));

        advanceToPostcombatMain();
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1,
                List.of(goyf.getId(), goat.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("different powers");
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class)).isNotNull();
        harness.handleMultipleCardsChosen(player1, List.of(goyf.getId()));
        assertThat(gd.playerHands.get(player1.getId())).contains(goyf);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(goat);
    }

    @Test
    void zeroPowerRevealsNothing() {
        Permanent rip = harness.addToBattlefieldAndReturn(player1, new RipSpawnHunter());
        Card goat = new PossessedGoat();
        rip.tap();
        harness.setLibrary(player1, List.of(goat));

        advanceToPostcombatMain();
        rip.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 4);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(goat);
    }

    @Test
    void opponentsTappedRipDoesNotTriggerOnYourTurn() {
        Permanent rip = harness.addToBattlefieldAndReturn(player2, new RipSpawnHunter());
        rip.tap();
        Card goat = new PossessedGoat();
        harness.setLibrary(player2, List.of(goat));

        advanceToPostcombatMain();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(goat);
    }

    @Test
    void additionalThirdMainPhaseDoesNotTriggerSurvivalAgain() {
        Permanent rip = harness.addToBattlefieldAndReturn(player1, new RipSpawnHunter());
        rip.tap();
        harness.setLibrary(player1, List.of(new Forest()));
        advanceToPostcombatMain();
        harness.passBothPriorities();

        harness.castFromHand(player1, new RelentlessAssault(), "{2}{R}{R}");
        harness.passBothPriorities();
        harness.passUntil(player1, TurnStep.BEGINNING_OF_COMBAT);
        harness.passUntilWithNoAttackers(player1, TurnStep.POSTCOMBAT_MAIN);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private void advanceToPostcombatMain() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.END_OF_COMBAT);
        harness.passUntil(player1, TurnStep.POSTCOMBAT_MAIN);
        assertThat(gd.currentStep).isEqualTo(TurnStep.POSTCOMBAT_MAIN);
    }
}

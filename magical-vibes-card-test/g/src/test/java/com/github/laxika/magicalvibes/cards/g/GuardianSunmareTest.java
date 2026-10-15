package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.b.BrightfieldGlider;
import com.github.laxika.magicalvibes.cards.l.LightshieldParry;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.r.RacersScoreboard;
import com.github.laxika.magicalvibes.cards.r.RidesEnd;
import com.github.laxika.magicalvibes.cards.t.TransitMage;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GuardianSunmare.class, BrightfieldGlider.class, Plains.class, LightshieldParry.class,
        GuidelightMatrix.class, RacersScoreboard.class, TransitMage.class, RidesEnd.class})
class GuardianSunmareTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking while saddled offers a nonland permanent with mana value 3 or less")
    void attacksWhileSaddledSearchesForMatchingPermanent() {
        Permanent sunmare = addCreatureReady(player1, new GuardianSunmare());
        sunmare.setSaddled(true);
        setLibrary(new BrightfieldGlider(), new Plains(), new GuardianSunmare(), new LightshieldParry());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).extracting(Card::getName)
                .containsExactly("Brightfield Glider");

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        harness.assertOnBattlefield(player1, "Brightfield Glider");
    }

    @Test
    @DisplayName("Attacking while not saddled does not search")
    void doesNotSearchWhenNotSaddled() {
        addCreatureReady(player1, new GuardianSunmare());
        setLibrary(new BrightfieldGlider());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
        assertThat(gd.playerDecks.get(player1.getId()))
                .anyMatch(card -> card.getName().equals("Brightfield Glider"));
    }

    @Test
    @DisplayName("The trigger checks saddled when attackers are declared")
    void checksSaddledAtDeclaration() {
        Permanent sunmare = addCreatureReady(player1, new GuardianSunmare());
        setLibrary(new BrightfieldGlider());

        declareAttackers(player1, List.of(0));
        sunmare.setSaddled(true);
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
    }

    @Test
    void canFailToFindEvenWithAnEligibleCard() {
        Permanent sunmare = addCreatureReady(player1, new GuardianSunmare());
        sunmare.setSaddled(true);
        BrightfieldGlider glider = new BrightfieldGlider();
        setLibrary(glider);

        declareAttackers(List.of(0));
        resolveAllTriggers();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNotNull();

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(-1));

        harness.assertNotOnBattlefield(player1, "Brightfield Glider");
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(glider);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
    }

    @Test
    void acceptsManaValueThreeAndRejectsManaValueFour() {
        Permanent sunmare = addCreatureReady(player1, new GuardianSunmare());
        sunmare.setSaddled(true);
        setLibrary(new TransitMage(), new RacersScoreboard());

        declareAttackers(List.of(0));
        resolveAllTriggers();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).extracting(Card::getName).containsExactly("Transit Mage");
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(-1));
    }

    @Test
    void putsANoncreaturePermanentOntoTheBattlefieldAndTriggersItsEnterAbility() {
        Permanent sunmare = addCreatureReady(player1, new GuardianSunmare());
        sunmare.setSaddled(true);
        Plains draw = new Plains();
        setLibrary(new GuidelightMatrix(), draw);
        harness.setHand(player1, List.of());

        declareAttackers(List.of(0));
        resolveAllTriggers();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNotNull();
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Guidelight Matrix");
        assertThat(findPermanent(player1, "Guidelight Matrix").isTapped()).isFalse();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(draw);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    void emptyLibraryCompletesWithoutAChoice() {
        Permanent sunmare = addCreatureReady(player1, new GuardianSunmare());
        sunmare.setSaddled(true);
        setLibrary();

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void searchResolvesAfterTheSaddledSourceLeavesTheBattlefield() {
        Permanent sunmare = addCreatureReady(player1, new GuardianSunmare());
        sunmare.setSaddled(true);
        setLibrary(new BrightfieldGlider());
        harness.setHand(player1, List.of(new RidesEnd()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> declareAttackers(List.of(0)));
        assertThat(gd.stack).hasSize(1);
        harness.castInstant(player1, 0, sunmare.getId());
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Guardian Sunmare");
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNotNull();
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));
        harness.assertOnBattlefield(player1, "Brightfield Glider");
    }

    @Test
    void saddleTapsAnotherCreatureAndEnablesTheSearch() {
        Permanent sunmare = addCreatureReady(player1, new GuardianSunmare());
        Permanent saddler = harness.addToBattlefieldAndReturn(player1, new GuardianSunmare());
        setLibrary(new BrightfieldGlider());

        harness.activateAbility(player1, 0, null, null);
        resolveAllTriggers();

        assertThat(saddler.isTapped()).isTrue();
        assertThat(sunmare.isTapped()).isFalse();
        assertThat(sunmare.isSaddled()).isTrue();
        declareAttackers(List.of(0));
        resolveAllTriggers();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNotNull();
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));
        harness.assertOnBattlefield(player1, "Brightfield Glider");
    }

    @Test
    void wardCountersAnOpponentsSpellWhenTheyDeclineToPayTwo() {
        Permanent sunmare = addCreatureReady(player1, new GuardianSunmare());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new RidesEnd()));
        harness.addMana(player2, ManaColor.WHITE, 7);

        harness.castInstant(player2, 0, sunmare.getId());
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player2, false);
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Guardian Sunmare");
        harness.assertInGraveyard(player2, "Ride's End");
    }

    @Test
    void payingTwoForWardLetsTheOpponentsSpellResolve() {
        Permanent sunmare = addCreatureReady(player1, new GuardianSunmare());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new RidesEnd()));
        harness.addMana(player2, ManaColor.WHITE, 7);

        harness.castInstant(player2, 0, sunmare.getId());
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player2, true);
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Guardian Sunmare");
        assertThat(gd.findExiledCard(sunmare.getCard().getId())).isNotNull();
        harness.assertInGraveyard(player2, "Ride's End");
    }
    private void setLibrary(Card... cards) {
        harness.setLibrary(player1, List.of(cards));
    }
}

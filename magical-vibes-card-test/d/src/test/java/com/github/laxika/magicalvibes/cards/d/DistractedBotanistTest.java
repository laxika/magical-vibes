package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.a.AinokTracker;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.z.Zombify;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AinokTracker.class, DistractedBotanist.class, GrizzlyBears.class, Shock.class, Zombify.class})
class DistractedBotanistTest extends BaseCardTest {

    @Test
    @DisplayName("A card that gained the ability doesn't trigger it when cast face down from hand")
    void faceDownCastDoesNotTriggerTheGrantedAbility() {
        grantToCardThenReturnItToHand();
        int lifeBefore = gd.getLife(player1.getId());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Ainok Tracker").isFaceDown()).isTrue();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertLife(player1, lifeBefore);
    }

    @Test
    @DisplayName("A card that gained the ability triggers it when cast face up from hand")
    void faceUpCastTriggersTheGrantedAbility() {
        grantToCardThenReturnItToHand();
        int lifeBefore = gd.getLife(player1.getId());
        harness.addMana(player1, ManaColor.RED, 6);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Ainok Tracker");
        harness.assertInHand(player1, "Shock");
        harness.assertLife(player1, lifeBefore + 1);
    }

    private AinokTracker grantToCardThenReturnItToHand() {
        AinokTracker tracker = new AinokTracker();
        harness.setGraveyard(player1, List.of(tracker));
        harness.setLibrary(player1, List.of(new Shock()));
        var botanist = harness.addToBattlefieldAndReturn(player1, new DistractedBotanist());
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castAndResolveInstant(player2, 0, botanist.getId());
        harness.passBothPriorities();

        gd.playerGraveyards.get(player1.getId()).remove(tracker);
        harness.setHand(player1, List.of(tracker));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        return tracker;
    }

    @Test
    @DisplayName("A permanent card in your graveyard draws a card and gains 1 life when it later enters")
    void permanentCardInGraveyardPerpetuallyDrawsAndGainsLifeWhenItEnters() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(bears));
        harness.setLibrary(player1, List.of(new Shock()));
        harness.addToBattlefield(player1, new DistractedBotanist());

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castAndResolveInstant(player2, 0, findPermanent(player1, "Distracted Botanist").getId());
        harness.passBothPriorities();

        int lifeBeforeReturn = gd.getLife(player1.getId());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new Zombify()));
        harness.addMana(player1, ManaColor.BLACK, 4);
        harness.castAndResolveSorcery(player1, 0, bears.getId());
        harness.passBothPriorities();

        harness.assertInHand(player1, "Shock");
        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBeforeReturn + 1);
    }

    @Test
    @DisplayName("Each death grants another, independent enter ability")
    void repeatedDeathTriggersGrantIndependentEnterAbilities() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(bears));
        harness.setLibrary(player1, List.of(new Shock(), new Shock(), new Shock()));
        harness.setHand(player2, List.of(new Shock(), new Shock()));
        harness.addMana(player2, ManaColor.RED, 2);
        for (int i = 0; i < 2; i++) {
            var botanist = harness.addToBattlefieldAndReturn(player1, new DistractedBotanist());
            harness.castAndResolveInstant(player2, 0, botanist.getId());
            harness.passBothPriorities();
        }

        int lifeBeforeReturn = gd.getLife(player1.getId());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new Zombify()));
        harness.addMana(player1, ManaColor.BLACK, 4);
        harness.castAndResolveSorcery(player1, 0, bears.getId());
        harness.passBothPriorities();
        if (!gd.stack.isEmpty()) {
            harness.passBothPriorities();
        }

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        harness.assertLife(player1, lifeBeforeReturn + 2);
    }

    @Test
    @DisplayName("Its own card in the graveyard gains the ability")
    void botanistGrantsTheEnterAbilityToItsOwnCard() {
        DistractedBotanist botanist = new DistractedBotanist();
        var permanent = harness.addToBattlefieldAndReturn(player1, botanist);
        harness.setLibrary(player1, List.of(new Shock(), new Shock()));
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castAndResolveInstant(player2, 0, permanent.getId());
        harness.passBothPriorities();

        int lifeBeforeReturn = gd.getLife(player1.getId());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new Zombify()));
        harness.addMana(player1, ManaColor.BLACK, 4);
        harness.castAndResolveSorcery(player1, 0, botanist.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Distracted Botanist");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        harness.assertLife(player1, lifeBeforeReturn + 1);
    }

    @Test
    @DisplayName("Cards in an opponent's graveyard gain nothing")
    void deathTriggerDoesNotGrantAbilitiesToOpponentsGraveyard() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(bears));
        harness.setLibrary(player2, List.of(new Shock(), new Shock()));
        var botanist = harness.addToBattlefieldAndReturn(player1, new DistractedBotanist());
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castAndResolveInstant(player2, 0, botanist.getId());
        harness.passBothPriorities();

        int lifeBeforeReturn = gd.getLife(player2.getId());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Zombify()));
        harness.addMana(player2, ManaColor.BLACK, 4);
        harness.castAndResolveSorcery(player2, 0, bears.getId());

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        harness.assertLife(player2, lifeBeforeReturn);
    }

    @Test
    @DisplayName("Cards put into the graveyard before the trigger resolves gain the ability")
    void grantIncludesPermanentCardsAddedBeforeDeathTriggerResolves() {
        GrizzlyBears bears = new GrizzlyBears();
        var botanist = harness.addToBattlefieldAndReturn(player1, new DistractedBotanist());
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castAndResolveInstant(player2, 0, botanist.getId());
        harness.setGraveyard(player1, List.of(bears));
        harness.passBothPriorities();

        harness.setLibrary(player1, List.of(new Shock(), new Shock()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new Zombify()));
        harness.addMana(player1, ManaColor.BLACK, 4);
        int lifeBeforeReturn = gd.getLife(player1.getId());
        harness.castAndResolveSorcery(player1, 0, bears.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        harness.assertLife(player1, lifeBeforeReturn + 1);
    }

    @Test
    @DisplayName("The ability stays with the card after it returns to the graveyard and enters again")
    void grantedAbilityPersistsAfterReturningToGraveyardAndEnteringAgain() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(bears));
        harness.setLibrary(player1, List.of(new Shock(), new Shock(), new Shock()));
        var botanist = harness.addToBattlefieldAndReturn(player1, new DistractedBotanist());
        harness.setHand(player2, List.of(new Shock(), new Shock()));
        harness.addMana(player2, ManaColor.RED, 2);
        harness.castAndResolveInstant(player2, 0, botanist.getId());
        harness.passBothPriorities();

        int lifeBeforeReturns = gd.getLife(player1.getId());
        for (int i = 0; i < 2; i++) {
            harness.forceActivePlayer(player1);
            harness.forceStep(TurnStep.PRECOMBAT_MAIN);
            harness.clearPriorityPassed();
            harness.setHand(player1, List.of(new Zombify()));
            harness.addMana(player1, ManaColor.BLACK, 4);
            harness.castAndResolveSorcery(player1, 0, bears.getId());
            harness.passBothPriorities();
            assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
            harness.assertLife(player1, lifeBeforeReturns + i + 1);
            if (i == 0) {
                harness.castAndResolveInstant(player2, 0,
                        findPermanent(player1, "Grizzly Bears").getId());
                harness.assertInGraveyard(player1, "Grizzly Bears");
            }
        }
    }
}

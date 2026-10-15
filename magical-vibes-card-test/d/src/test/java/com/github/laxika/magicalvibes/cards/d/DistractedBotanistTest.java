package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.z.Zombify;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DistractedBotanist.class, GrizzlyBears.class, Shock.class, Zombify.class})
class DistractedBotanistTest extends BaseCardTest {

    @Test
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

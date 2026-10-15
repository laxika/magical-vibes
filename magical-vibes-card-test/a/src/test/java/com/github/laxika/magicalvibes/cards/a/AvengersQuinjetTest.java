package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.b.BraveBrawler;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AvengersQuinjet.class, BraveBrawler.class, GrizzlyBears.class})
class AvengersQuinjetTest extends BaseCardTest {

    private static final String DEPLOY = "You may put a Hero creature card from your hand onto the battlefield";
    private static final String RECOVER = "Return target Hero creature card from your graveyard to your hand";

    @Test
    void entersAndPutsAValidHeroFromHandOntoTheBattlefield() {
        Card hero = new BraveBrawler();
        Card nonHero = new GrizzlyBears();
        harness.setHand(player1, List.of(new AvengersQuinjet(), hero, nonHero));
        addManaForQuinjet();

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.handleListChoice(player1, DEPLOY);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.HandCardChoice.class).validIndices())
                .containsExactly(0);
        harness.handleCardChosen(player1, 0);

        harness.assertOnBattlefield(player1, "Brave Brawler");
        harness.assertInHand(player1, "Grizzly Bears");
    }

    @Test
    void entersAndReturnsAValidHeroFromTheGraveyard() {
        Card hero = new BraveBrawler();
        Card nonHero = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(hero, nonHero));
        harness.castFromHand(player1, new AvengersQuinjet(), "{5}");
        harness.passBothPriorities();
        harness.handleListChoice(player1, RECOVER);

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactly(hero.getId());
        harness.handleMultipleCardsChosen(player1, List.of(hero.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Brave Brawler");
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    void attacksAndOffersTheSameModes() {
        Permanent quinjet = harness.addToBattlefieldAndReturn(player1, new AvengersQuinjet());
        quinjet.setSummoningSick(false);
        quinjet.setAnimatedUntilEndOfTurn(true);
        quinjet.setAnimatedPower(4);
        quinjet.setAnimatedToughness(4);
        Card hero = new BraveBrawler();
        harness.setHand(player1, List.of(hero));

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        gs.declareAttackers(gd, player1, List.of(0));
        harness.passBothPriorities();
        harness.handleListChoice(player1, DEPLOY);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);

        harness.assertOnBattlefield(player1, "Brave Brawler");
    }

    @Test
    void mayDeclinePuttingAHeroOntoTheBattlefield() {
        harness.setHand(player1, List.of(new AvengersQuinjet(), new BraveBrawler()));
        addManaForQuinjet();
        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.handleListChoice(player1, DEPLOY);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertInHand(player1, "Brave Brawler");
        harness.assertNotOnBattlefield(player1, "Brave Brawler");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void cannotChooseRecoveryWithoutAHeroInOwnGraveyard() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        harness.setGraveyard(player2, List.of(new BraveBrawler()));
        harness.castFromHand(player1, new AvengersQuinjet(), "{5}");
        harness.passBothPriorities();

        PendingInteraction.ColorChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(choice.options().stream()
                .filter(option -> !choice.disabledOptions().contains(option)).toList())
                .containsExactly(DEPLOY);
        harness.handleListChoice(player1, DEPLOY);
        harness.passBothPriorities();
        if (gd.interaction.isAwaitingInput()) {
            harness.handleMayAbilityChosen(player1, false);
        }
    }

    @Test
    void deploymentWithNoHeroInHandDoesNothing() {
        harness.setHand(player1, List.of(new AvengersQuinjet(), new GrizzlyBears()));
        addManaForQuinjet();
        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.handleListChoice(player1, DEPLOY);
        harness.passBothPriorities();
        if (gd.interaction.isAwaitingInput()) {
            harness.handleMayAbilityChosen(player1, true);
        }

        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void recoveryDoesNotChooseAnotherHeroWhenItsTargetLeavesTheGraveyard() {
        Card target = new BraveBrawler();
        Card otherHero = new BraveBrawler();
        harness.setGraveyard(player1, List.of(target, otherHero));
        harness.castFromHand(player1, new AvengersQuinjet(), "{5}");
        harness.passBothPriorities();
        harness.handleListChoice(player1, RECOVER);
        harness.handleMultipleCardsChosen(player1, List.of(target.getId()));

        harness.setGraveyard(player1, List.of(otherHero));
        harness.setExile(player1, List.of(target));
        harness.passBothPriorities();

        harness.assertNotInHand(player1, "Brave Brawler");
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(otherHero);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void crewThreeTapsCreaturesAndAllowsTheAttackRecoveryTrigger() {
        Permanent quinjet = harness.addToBattlefieldAndReturn(player1, new AvengersQuinjet());
        quinjet.setSummoningSick(false);
        Permanent firstCrew = addCreatureReady(player1, new BraveBrawler());
        Permanent secondCrew = addCreatureReady(player1, new BraveBrawler());
        Card hero = new BraveBrawler();
        harness.setGraveyard(player1, List.of(hero));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, quinjet)).isTrue();
        assertThat(firstCrew.isTapped()).isTrue();
        assertThat(secondCrew.isTapped()).isTrue();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        gs.declareAttackers(gd, player1, List.of(0));
        harness.passBothPriorities();
        harness.handleListChoice(player1, RECOVER);
        harness.handleMultipleCardsChosen(player1, List.of(hero.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Brave Brawler");
        harness.assertNotInGraveyard(player1, "Brave Brawler");
    }

    @Test
    void cannotCrewWithOnlyTwoPower() {
        harness.addToBattlefield(player1, new AvengersQuinjet());
        addCreatureReady(player1, new BraveBrawler());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough creature power to crew");
    }

    private void addManaForQuinjet() {
        harness.addMana(player1, ManaColor.COLORLESS, 5);
    }
}

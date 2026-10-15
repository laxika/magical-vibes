package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.c.CivilServant;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GirderGoons;
import com.github.laxika.magicalvibes.cards.m.Murder;
import com.github.laxika.magicalvibes.cards.w.WrathOfGod;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BodyLaunderer.class, BackstreetBruiser.class, Forest.class, CivilServant.class, GirderGoons.class, Murder.class, WrathOfGod.class})
class BodyLaundererTest extends BaseCardTest {

    @Test
    void anotherNontokenCreatureYouControlDiesAndBodyLaundererConnives() {
        Permanent bodyLaunderer = addCreatureReady(player1, new BodyLaunderer());
        Permanent civilServant = addCreatureReady(player1, new CivilServant());
        harness.setLibrary(player1, List.of(new CivilServant()));
        harness.setHand(player2, List.of(new Murder()));
        harness.addMana(player2, ManaColor.BLACK, 3);

        harness.castAndResolveInstant(player2, 0, civilServant.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        int civilServantIndex = gd.playerHands.get(player1.getId()).indexOf(
                gd.playerHands.get(player1.getId()).stream()
                        .filter(card -> card.getName().equals("Civil Servant"))
                        .findFirst()
                        .orElseThrow());
        harness.handleCardChosen(player1, civilServantIndex);

        assertThat(bodyLaunderer.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void deathTriggerReturnsAnotherNonRogueCreatureWithPowerAtMostBodyLaunderersPower() {
        BodyLaunderer bodyLaunderer = new BodyLaunderer();
        Card creature = new CivilServant();
        Card rogue = new BackstreetBruiser();
        Card nonCreature = new Forest();
        addCreatureReady(player1, bodyLaunderer);
        harness.setGraveyard(player1, List.of(creature, rogue, nonCreature));
        destroyBodyLaunderer();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactly(creature.getId());

        harness.handleMultipleCardsChosen(player1, List.of(creature.getId()));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Civil Servant");
        harness.assertInGraveyard(player1, "Body Launderer");
        harness.assertInGraveyard(player1, "Forest");
    }

    @Test
    void deathTriggerUsesBodyLaunderersLastKnownPower() {
        BodyLaunderer bodyLaunderer = new BodyLaunderer();
        Permanent bodyPermanent = addCreatureReady(player1, bodyLaunderer);
        bodyPermanent.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        GirderGoons creature = new GirderGoons();
        harness.setGraveyard(player1, List.of(creature));
        destroyBodyLaunderer();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactly(creature.getId());

        harness.handleMultipleCardsChosen(player1, List.of(creature.getId()));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Girder Goons");
    }

    @Test
    void discardingALandDoesNotAddACounter() {
        Permanent body = addCreatureReady(player1, new BodyLaunderer());
        Permanent ally = addCreatureReady(player1, new CivilServant());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest()));
        destroyWithMurder(ally);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 0);

        harness.assertInGraveyard(player1, "Forest");
        assertThat(body.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void opposingCreatureDeathDoesNotCauseConnive() {
        Permanent body = addCreatureReady(player1, new BodyLaunderer());
        Permanent opponent = addCreatureReady(player2, new CivilServant());
        CivilServant libraryCard = new CivilServant();
        harness.setLibrary(player1, List.of(libraryCard));
        destroyWithMurder(opponent);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(libraryCard);
        assertThat(body.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void deathOfARealRogueTokenDoesNotCauseConnive() {
        Permanent body = addCreatureReady(player1, new BodyLaunderer());
        Permanent goons = addCreatureReady(player1, new GirderGoons());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest(), new CivilServant()));
        destroyWithMurder(goons);
        harness.passBothPriorities();
        if (!(gd.interaction.activeInteraction() instanceof PendingInteraction.DiscardChoice)) {
            harness.passBothPriorities();
        }
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 0);
        if (!gd.stack.isEmpty()) {
            harness.passBothPriorities();
        }

        Permanent token = findPermanent(player1, "Rogue");
        destroyWithMurder(token);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(body.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.assertNotOnBattlefield(player1, "Rogue");
    }

    @Test
    void deathTriggerExcludesCreaturesWithGreaterPowerAndOpponentsGraveyard() {
        addCreatureReady(player1, new BodyLaunderer());
        CivilServant eligible = new CivilServant();
        GirderGoons tooLarge = new GirderGoons();
        CivilServant opponentsCard = new CivilServant();
        harness.setGraveyard(player1, List.of(eligible, tooLarge));
        harness.setGraveyard(player2, List.of(opponentsCard));
        destroyBodyLaunderer();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactly(eligible.getId());
        harness.handleMultipleCardsChosen(player1, List.of(eligible.getId()));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Civil Servant");
        harness.assertInGraveyard(player1, "Girder Goons");
        harness.assertInGraveyard(player2, "Civil Servant");
    }

    @Test
    void ownDeathDoesNotConniveWhenThereAreNoLegalReturnTargets() {
        addCreatureReady(player1, new BodyLaunderer());
        CivilServant libraryCard = new CivilServant();
        harness.setLibrary(player1, List.of(libraryCard));
        harness.setGraveyard(player1, List.of(new BackstreetBruiser(), new GirderGoons(), new Forest()));
        destroyBodyLaunderer();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(libraryCard);
        harness.assertInGraveyard(player1, "Body Launderer");
    }

    @Test
    void conniveStillDrawsAndDiscardsAfterBodyLaundererLeavesTheBattlefield() {
        Permanent body = addCreatureReady(player1, new BodyLaunderer());
        Permanent ally = addCreatureReady(player1, new CivilServant());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest()));
        destroyWithMurder(ally);
        destroyWithMurder(body);

        harness.handleMultipleCardsChosen(player1, List.of(ally.getCard().getId()));
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 0);
        harness.assertInGraveyard(player1, "Forest");
        harness.assertInGraveyard(player1, "Body Launderer");
        harness.assertOnBattlefield(player1, "Civil Servant");
    }

    @Test
    void simultaneousDeathOfBodyLaundererAndAnotherCreatureStillCausesConnive() {
        addCreatureReady(player1, new BodyLaunderer());
        Permanent ally = addCreatureReady(player1, new CivilServant());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest()));
        destroyBodyLaunderer();

        harness.handleMultipleCardsChosen(player1, List.of(ally.getCard().getId()));
        harness.passBothPriorities();
        if (!(gd.interaction.activeInteraction() instanceof PendingInteraction.DiscardChoice)) {
            harness.passBothPriorities();
        }

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 0);
        harness.assertInGraveyard(player1, "Forest");
        harness.assertInGraveyard(player1, "Body Launderer");
    }

    private void destroyWithMurder(Permanent target) {
        harness.setHand(player2, List.of(new Murder()));
        harness.addMana(player2, ManaColor.BLACK, 3);
        harness.castAndResolveInstant(player2, 0, target.getId());
    }

    private void destroyBodyLaunderer() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new WrathOfGod()));
        harness.addMana(player2, ManaColor.WHITE, 4);
        harness.castAndResolveSorcery(player2, 0, 0);
    }
}

package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.m.Murder;
import com.github.laxika.magicalvibes.cards.o.ObNixilisTheAdversary;
import com.github.laxika.magicalvibes.cards.s.SecurityRhox;
import com.github.laxika.magicalvibes.cards.u.UnleashTheInferno;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LordXanderTheCollector.class, Forest.class, SecurityRhox.class, Murder.class,
        ObNixilisTheAdversary.class, UnleashTheInferno.class})
class LordXanderTheCollectorTest extends BaseCardTest {

    @Test
    @DisplayName("ETB makes a target opponent discard half their hand rounded down")
    void entersAndMakesOpponentDiscardHalfTheirHand() {
        harness.setHand(player1, List.of(new LordXanderTheCollector()));
        harness.setHand(player2, List.of(
                new SecurityRhox(), new SecurityRhox(), new SecurityRhox(),
                new SecurityRhox(), new SecurityRhox()));
        addLordXanderMana();

        harness.castCreature(player1, 0, player2.getId());
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player2, 0);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).hasSize(3);
    }

    @Test
    @DisplayName("The ETB cannot target its controller")
    void entersCannotTargetItsController() {
        harness.setHand(player1, List.of(new LordXanderTheCollector()));
        addLordXanderMana();

        assertThatThrownBy(() -> harness.castCreature(player1, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an opponent");
    }

    @Test
    @DisplayName("Attacking mills half the defending player's library rounded down")
    void attackingMillsHalfDefendingLibraryRoundedDown() {
        addCreatureReady(player1, new LordXanderTheCollector());
        harness.setLibrary(player2, List.of(
                new SecurityRhox(), new SecurityRhox(), new SecurityRhox(),
                new SecurityRhox(), new SecurityRhox()));

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).hasSize(3);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Death makes a target opponent sacrifice half their nonland permanents rounded down")
    void deathMakesOpponentSacrificeHalfTheirNonlandPermanents() {
        Permanent lord = addCreatureReady(player1, new LordXanderTheCollector());
        List<Permanent> creatures = new ArrayList<>();
        harness.addToBattlefield(player2, new Forest());
        for (int i = 0; i < 5; i++) {
            creatures.add(harness.addToBattlefieldAndReturn(player2, new SecurityRhox()));
        }
        harness.setHand(player2, List.of(new Murder()));
        harness.addMana(player2, ManaColor.BLACK, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castAndResolveInstant(player2, 0, lord.getId());

        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player2.getId());
        assertThat(choice.maxCount()).isEqualTo(2);
        harness.handleMultiplePermanentsChosen(player2,
                List.of(creatures.get(0).getId(), creatures.get(1).getId()));

        assertThat(countPermanents(player2, "Security Rhox")).isEqualTo(3);
        assertThat(countPermanents(player2, "Forest")).isEqualTo(1);
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1})
    void entersDiscardsNothingFromAHandWithFewerThanTwoCards(int handSize) {
        harness.setHand(player1, List.of(new LordXanderTheCollector()));
        harness.setHand(player2, handSize == 0 ? List.of() : List.of(new SecurityRhox()));
        addLordXanderMana();

        harness.castCreature(player1, 0, player2.getId());
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player2.getId())).hasSize(handSize);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void entersCountsCardsInHandWhenTheTriggerResolves() {
        harness.setHand(player1, List.of(new LordXanderTheCollector()));
        harness.setHand(player2, List.of(new SecurityRhox()));
        addLordXanderMana();
        harness.castCreature(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.setHand(player2, List.of(new SecurityRhox(), new SecurityRhox(),
                new SecurityRhox(), new SecurityRhox()));

        resolveAllTriggers();
        harness.handleCardChosen(player2, 0);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).hasSize(2);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2);
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1})
    void attackingMillsNothingFromALibraryWithFewerThanTwoCards(int librarySize) {
        addCreatureReady(player1, new LordXanderTheCollector());
        harness.setLibrary(player2, librarySize == 0 ? List.of() : List.of(new SecurityRhox()));

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(gd.playerDecks.get(player2.getId())).hasSize(librarySize);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    @Test
    void attackingStillMillsAfterTheAttackedPlaneswalkerLeaves() {
        addCreatureReady(player1, new LordXanderTheCollector());
        Permanent planeswalker = harness.enterBattlefieldAndReturn(player2, new ObNixilisTheAdversary());
        harness.setLibrary(player2, List.of(new SecurityRhox(), new SecurityRhox(),
                new SecurityRhox(), new SecurityRhox(), new SecurityRhox(), new SecurityRhox()));
        harness.setHand(player2, List.of(new UnleashTheInferno()));
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        gs.declareAttackers(gd, player1, List.of(0), Map.of(0, planeswalker.getId()));

        harness.castAndResolveInstant(player2, 0, planeswalker.getId());
        harness.assertNotOnBattlefield(player2, "Ob Nixilis, the Adversary");
        resolveAllTriggers();

        assertThat(gd.playerDecks.get(player2.getId())).hasSize(3);
        assertThat(gd.playerGraveyards.get(player2.getId()).stream()
                .filter(card -> card instanceof SecurityRhox)).hasSize(3);
    }

    @Test
    void deathAllowsTheOpponentToChooseANoncreaturePermanent() {
        Permanent lord = addCreatureReady(player1, new LordXanderTheCollector());
        Permanent planeswalker = harness.enterBattlefieldAndReturn(player2, new ObNixilisTheAdversary());
        harness.addToBattlefield(player2, new SecurityRhox());
        harness.addToBattlefield(player2, new Forest());
        harness.setHand(player2, List.of(new Murder()));
        harness.addMana(player2, ManaColor.BLACK, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.castAndResolveInstant(player2, 0, lord.getId());
        harness.handlePermanentChosen(player1, player2.getId());
        resolveAllTriggers();

        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.maxCount()).isEqualTo(1);
        harness.handleMultiplePermanentsChosen(player2, List.of(planeswalker.getId()));

        harness.assertInGraveyard(player2, "Ob Nixilis, the Adversary");
        harness.assertOnBattlefield(player2, "Security Rhox");
        harness.assertOnBattlefield(player2, "Forest");
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1})
    void deathSacrificesNothingWithFewerThanTwoNonlandPermanents(int nonlandCount) {
        Permanent lord = addCreatureReady(player1, new LordXanderTheCollector());
        harness.addToBattlefield(player2, new Forest());
        if (nonlandCount == 1) {
            harness.addToBattlefield(player2, new SecurityRhox());
        }
        harness.setHand(player2, List.of(new Murder()));
        harness.addMana(player2, ManaColor.BLACK, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.castAndResolveInstant(player2, 0, lord.getId());
        harness.handlePermanentChosen(player1, player2.getId());
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player2.getId())).hasSize(nonlandCount + 1);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertOnBattlefield(player2, "Forest");
    }

    private void addLordXanderMana() {
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
    }
}

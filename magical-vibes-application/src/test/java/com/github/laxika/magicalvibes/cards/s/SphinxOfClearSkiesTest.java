package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.PendingPileSeparation;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SphinxOfClearSkies.class, Forest.class, Island.class, GiantGrowth.class, Shock.class})
class SphinxOfClearSkiesTest extends BaseCardTest {

    @Test
    void combatDamageRevealsCardsBasedOnDomainAndPutsOnePileInHand() {
        Card giantGrowth = new GiantGrowth();
        Card shock = new Shock();
        harness.setLibrary(player1, List.of(giantGrowth, shock));
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Island());
        Permanent sphinx = addCreatureReady(player1, new SphinxOfClearSkies());
        sphinx.setAttacking(true);

        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.hasPendingInteraction(PendingPileSeparation.class)).isTrue();
        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player2.getId());
        assertThat(choice.validCardIds()).containsExactlyInAnyOrder(giantGrowth.getId(), shock.getId());

        harness.handleMultipleCardsChosen(player2, List.of(giantGrowth.getId()));
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).contains(giantGrowth);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(shock);
    }

    @Test
    void wardCountersAnOpponentSpellUnlessTheyPayTwoMana() {
        Permanent sphinx = harness.addToBattlefieldAndReturn(player1, new SphinxOfClearSkies());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castAndResolveInstant(player2, 0, sphinx.getId());

        harness.assertInGraveyard(player2, "Shock");
        assertThat(sphinx.getMarkedDamage()).isZero();
    }

    @Test
    void wardAllowsTheSpellToResolveWhenOpponentPays() {
        Permanent sphinx = harness.addToBattlefieldAndReturn(player1, new SphinxOfClearSkies());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 3);

        harness.castAndResolveInstant(player2, 0, sphinx.getId());
        harness.handleMayAbilityChosen(player2, true);
        harness.passBothPriorities();

        assertThat(sphinx.getMarkedDamage()).isEqualTo(2);
        harness.assertInGraveyard(player2, "Shock");
    }

    @Test
    void wardDoesNotTriggerForTheControllersOwnSpell() {
        Permanent sphinx = harness.addToBattlefieldAndReturn(player1, new SphinxOfClearSkies());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, sphinx.getId());

        assertThat(sphinx.getMarkedDamage()).isEqualTo(2);
        harness.assertInGraveyard(player1, "Shock");
    }

    @Test
    void controllerCanChooseTheSecondPile() {
        Card first = new GiantGrowth();
        Card second = new Shock();
        harness.setLibrary(player1, List.of(first, second));
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Island());
        Permanent sphinx = addCreatureReady(player1, new SphinxOfClearSkies());
        sphinx.setAttacking(true);

        resolveCombat();
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player2, List.of(first.getId()));
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).contains(second).doesNotContain(first);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(first).doesNotContain(second);
    }

    @Test
    void duplicateLandTypesDoNotIncreaseDomainAndAnEmptyPileCanBeChosen() {
        Card first = new GiantGrowth();
        Card second = new Shock();
        harness.setLibrary(player1, List.of(first, second));
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player2, new Island());
        Permanent sphinx = addCreatureReady(player1, new SphinxOfClearSkies());
        sphinx.setAttacking(true);

        resolveCombat();
        harness.passBothPriorities();
        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactly(first.getId());
        harness.handleMultipleCardsChosen(player2, List.of());
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(first, second);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(first).doesNotContain(second);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(second);
    }

    @Test
    void zeroDomainRevealsNoCards() {
        Card card = new GiantGrowth();
        harness.setLibrary(player1, List.of(card));
        Permanent sphinx = addCreatureReady(player1, new SphinxOfClearSkies());
        sphinx.setAttacking(true);

        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.hasPendingInteraction(PendingPileSeparation.class)).isFalse();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(card);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(card);
    }

    @Test
    void domainUsesLandsAtResolutionAndRevealsOnlyAvailableCards() {
        Card card = new GiantGrowth();
        harness.setLibrary(player1, List.of(card));
        Permanent sphinx = addCreatureReady(player1, new SphinxOfClearSkies());
        sphinx.setAttacking(true);

        harness.forceStep(TurnStep.COMBAT_DAMAGE);
        harness.resolveCombatDamage();
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Island());
        harness.passBothPriorities();
        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactly(card.getId());
        harness.handleMultipleCardsChosen(player2, List.of());
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).contains(card);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(card);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }
}
